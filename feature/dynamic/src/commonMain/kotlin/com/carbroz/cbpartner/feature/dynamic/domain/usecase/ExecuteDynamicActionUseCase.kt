package com.carbroz.cbpartner.feature.dynamic.domain.usecase

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed interface DynamicActionResult {
    data class Navigate(val destination: DynamicDestination) : DynamicActionResult
    data class ExternalUri(val uri: String) : DynamicActionResult
}

class ExecuteDynamicActionUseCase(
    private val repository: DynamicRepository,
) {
    suspend operator fun invoke(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
    ): Result<DynamicActionResult> = runCatching {
        when (action.type) {
            "request" -> executeRequest(action, bindings, context)
            "navigate" -> executeNavigate(action)
            "external_uri" -> executeExternalUri(action, context)
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
        val body = action.payload["body"]
            ?.resolve(bindings, context)
            ?.let { Json.encodeToString(JsonElement.serializer(), it) }

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

        val next = response.nextScreen
            ?: error("Dynamic request did not provide nextScreen")

        return DynamicActionResult.Navigate(
            DynamicDestination(
                screenId = next.screenId,
                templateId = next.templateId,
                templateType = next.templateType,
                endpoint = next.endpoint,
                method = next.method,
                authentication = next.authentication,
            ),
        )
    }

    private fun executeNavigate(action: DynamicAction): DynamicActionResult.Navigate {
        val screenId = string(action.payload["screenId"])
            ?: error("Dynamic navigation screenId is missing")
        val endpoint = string(action.payload["endpoint"])
            ?: error("Dynamic navigation endpoint is missing")

        return DynamicActionResult.Navigate(
            DynamicDestination(
                screenId = screenId,
                templateId = string(action.payload["templateId"]),
                templateType = string(action.payload["templateType"]),
                endpoint = endpoint,
                method = string(action.payload["method"]) ?: "GET",
                authentication = string(action.payload["authentication"]),
            ),
        )
    }

    private fun executeExternalUri(
        action: DynamicAction,
        context: Map<String, String>,
    ): DynamicActionResult.ExternalUri {
        val uri = action.payload["uri"]?.resolve(context = context)
            ?.let { (it as? JsonPrimitive)?.content }
            ?: error("Dynamic external URI is missing")
        return DynamicActionResult.ExternalUri(uri)
    }

    private fun DynamicValue.resolve(
        bindings: Map<String, String> = emptyMap(),
        context: Map<String, String> = emptyMap(),
    ): JsonElement = when (this) {
        is DynamicValue.ObjectValue -> {
            val binding = (value["${'$'}binding"] as? DynamicValue.StringValue)?.value
            val contextRef = (value["${'$'}context"] as? DynamicValue.StringValue)?.value
            when {
                binding != null -> JsonPrimitive(bindings[binding].orEmpty())
                contextRef != null -> JsonPrimitive(context[contextRef].orEmpty())
                else -> JsonObject(value.mapValues { (_, child) -> child.resolve(bindings, context) })
            }
        }
        is DynamicValue.ArrayValue -> JsonArray(value.map { it.resolve(bindings, context) })
        is DynamicValue.StringValue -> JsonPrimitive(value)
        is DynamicValue.NumberValue -> JsonPrimitive(value)
        is DynamicValue.BooleanValue -> JsonPrimitive(value)
        DynamicValue.NullValue -> JsonNull
    }

    private fun string(value: DynamicValue?): String? =
        (value as? DynamicValue.StringValue)?.value
}
