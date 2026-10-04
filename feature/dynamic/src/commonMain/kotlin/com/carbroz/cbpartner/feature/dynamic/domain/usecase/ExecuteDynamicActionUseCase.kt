package com.carbroz.cbpartner.feature.dynamic.domain.usecase

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionHandler

sealed interface DynamicActionResult {
    data class Navigate(val destination: DynamicDestination) : DynamicActionResult
    data class ExternalUri(val uri: String) : DynamicActionResult
}

class ExecuteDynamicActionUseCase(
    private val repository: DynamicRepository,
    private val actionHandler: DynamicActionHandler = DynamicActionHandler(),
) {
    suspend operator fun invoke(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
    ): Result<DynamicActionResult> = runCatching {
        when (action.type) {
            "request" -> executeRequest(action, bindings, context)
            "navigate" -> DynamicActionResult.Navigate(actionHandler.destination(action))
            "external_uri" -> DynamicActionResult.ExternalUri(actionHandler.externalUri(action, context))
            else -> error("Unsupported dynamic action: ${action.type}")
        }
    }

    private suspend fun executeRequest(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
    ): DynamicActionResult {
        val endpoint = string(action.payload["endpoint"])
            ?: error("Dynamic request endpoint is missing")
        val method = string(action.payload["method"]) ?: "POST"
        val authentication = string(action.payload["authentication"])
        val body = action.payload["body"]?.resolve(bindings, context)

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

        return DynamicActionResult.Navigate(response.requireNextDestination())
    }

    private fun DynamicResponse.requireNextDestination(): DynamicDestination {
        val next = nextScreen ?: error("Dynamic request did not provide nextScreen")
        return DynamicDestination(
            screenId = next.screenId,
            templateId = next.templateId,
            templateType = next.templateType,
            endpoint = next.endpoint,
            method = next.method,
            authentication = next.authentication,
        )
    }

    private fun DynamicValue.resolve(
        bindings: Map<String, String>,
        context: Map<String, String>,
    ): DynamicValue = when (this) {
        is DynamicValue.ObjectValue -> {
            val binding = (value["${'$'}binding"] as? DynamicValue.StringValue)?.value
            val contextRef = (value["${'$'}context"] as? DynamicValue.StringValue)?.value
            val literal = value["${'$'}literal"]

            when {
                binding != null -> DynamicValue.StringValue(bindings[binding].orEmpty())
                contextRef != null -> DynamicValue.StringValue(context[contextRef].orEmpty())
                literal != null -> literal.resolve(bindings, context)
                else -> DynamicValue.ObjectValue(
                    value.mapValues { (_, child) -> child.resolve(bindings, context) },
                )
            }
        }

        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(
            value.map { it.resolve(bindings, context) },
        )

        else -> this
    }

    private fun string(value: DynamicValue?): String? =
        (value as? DynamicValue.StringValue)?.value
}
