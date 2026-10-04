package com.carbroz.cbpartner.feature.dynamic.domain.usecase

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionHandler

sealed interface DynamicActionResult {
    data class Completed(val response: DynamicResponse) : DynamicActionResult
    data class Navigate(
        val destination: DynamicDestination,
        val response: DynamicResponse? = null,
    ) : DynamicActionResult
    data class ExternalUri(val uri: String) : DynamicActionResult
}

class ExecuteDynamicActionUseCase(
    private val repository: DynamicRepository,
    private val actionHandler: DynamicActionHandler,
) {
    suspend operator fun invoke(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue? = null,
    ): Result<DynamicActionResult> = runCatching {
        when (action.type) {
            "request" -> executeRequest(action, bindings, context, previousResponse)
            "navigate" -> DynamicActionResult.Navigate(actionHandler.destination(action))
            "external_uri" -> DynamicActionResult.ExternalUri(actionHandler.externalUri(action, context))
            else -> error("Unsupported dynamic action: ${action.type}")
        }
    }

    private suspend fun executeRequest(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult {
        val endpoint = string(action.payload["endpoint"])
            ?: error("Dynamic request endpoint is missing")
        val method = string(action.payload["method"]) ?: "POST"
        val authentication = string(action.payload["authentication"])
        val body = action.payload["body"]?.resolve(bindings, context, previousResponse)

        val response = repository.fetch(
            destination = DynamicDestination(
                screenId = "action:$endpoint",
                templateId = null,
                templateType = null,
                endpoint = endpoint,
                method = method,
                authentication = authentication,
            ),
            body = body,
        ).getOrThrow()

        return when (string(action.payload["responseMode"])?.lowercase()) {
            "none" -> DynamicActionResult.Completed(response)
            else -> DynamicActionResult.Navigate(
                destination = response.requireNextDestination(),
                response = response,
            )
        }
    }

    private fun DynamicResponse.requireNextDestination(): DynamicDestination =
        data?.nextScreen ?: error("Dynamic request did not provide data.nextScreen")

    private fun DynamicValue.resolve(
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicValue = when (this) {
        is DynamicValue.ObjectValue -> {
            val binding = (value["${'$'}binding"] as? DynamicValue.StringValue)?.value
            val contextRef = (value["${'$'}context"] as? DynamicValue.StringValue)?.value
            val responseRef = (value["${'$'}response"] as? DynamicValue.StringValue)?.value
            val literal = value["${'$'}literal"]

            when {
                binding != null -> DynamicValue.StringValue(bindings[binding].orEmpty())
                contextRef != null -> DynamicValue.StringValue(context[contextRef].orEmpty())
                responseRef != null -> previousResponse?.resolvePath(responseRef) ?: DynamicValue.NullValue
                literal != null -> literal.resolve(bindings, context, previousResponse)
                else -> DynamicValue.ObjectValue(
                    value.mapValues { (_, child) -> child.resolve(bindings, context, previousResponse) },
                )
            }
        }

        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(
            value.map { it.resolve(bindings, context, previousResponse) },
        )

        else -> this
    }

    private fun DynamicValue.resolvePath(path: String): DynamicValue {
        if (path.isBlank()) return this
        return path.split('.').fold(this) { current, segment ->
            when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> DynamicValue.NullValue
            }
        }
    }

    private fun string(value: DynamicValue?): String? =
        (value as? DynamicValue.StringValue)?.value
}
