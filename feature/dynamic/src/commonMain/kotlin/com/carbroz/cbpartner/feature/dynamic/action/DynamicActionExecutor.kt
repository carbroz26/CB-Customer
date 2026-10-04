package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class DynamicActionExecutor(
    private val repository: DynamicRepository,
    private val navigation: DynamicNavigationState,
) {
    suspend fun execute(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): Result<DynamicActionResult> = runCatching {
        when (action.type.lowercase()) {
            "navigate" -> navigate(action)
            "request" -> request(action, bindings, context, previousResponse)
            "external_uri" -> externalUri(action, context)
            "dismiss", "back" -> {
                navigation.pop()
                DynamicActionResult.Completed
            }
            "present" -> present(action)
            "state" -> state(action, bindings, context, previousResponse)
            "sequence" -> sequence(action, bindings, context, previousResponse)
            else -> error("Unsupported dynamic action: ${action.type}")
        }
    }

    private fun navigate(action: DynamicAction): DynamicActionResult.Navigate {
        val destination = action.destination ?: error("Dynamic navigation destination is missing")
        applyNavigation(action, destination)
        return DynamicActionResult.Navigate(destination)
    }

    private suspend fun request(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult {
        val endpoint = string(action.payload["endpoint"])
            ?: error("Dynamic request endpoint is missing")
        val method = string(action.payload["method"]) ?: "POST"
        val authentication = string(action.payload["authentication"]) ?: "NONE"
        val body = action.payload["body"]?.resolve(bindings, context, previousResponse)

        val response = repository.fetch(
            destination = DynamicDestination(
                screenId = "action:$endpoint",
                templateId = "action",
                templateType = "request",
                endpoint = endpoint,
            ),
            body = body,
            method = method,
            authentication = authentication,
        ).getOrThrow()

        return when (string(action.payload["responseMode"])?.lowercase()) {
            "none" -> DynamicActionResult.Completed
            else -> {
                val destination = response.destination
                    ?: error("Dynamic request did not provide data.destination")
                applyNavigation(action, destination)
                DynamicActionResult.Navigate(destination, response)
            }
        }
    }

    private fun externalUri(
        action: DynamicAction,
        context: Map<String, String>,
    ): DynamicActionResult.ExternalUri {
        val value = action.payload["uri"] ?: error("Dynamic external URI is missing")
        val uri = when (value) {
            is DynamicValue.StringValue -> value.value
            is DynamicValue.ObjectValue -> {
                val reference = string(value.value["${'$'}context"])
                    ?: error("Dynamic external URI reference is invalid")
                context[reference] ?: error("Dynamic context value is missing: $reference")
            }
            else -> error("Dynamic external URI value is invalid")
        }
        return DynamicActionResult.ExternalUri(uri)
    }

    private fun present(action: DynamicAction): DynamicActionResult.Navigate {
        val destination = action.destination ?: error("Dynamic present destination is missing")
        navigation.push(destination)
        return DynamicActionResult.Navigate(destination)
    }

    private fun state(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult.StateChanged {
        val key = string(action.payload["key"]) ?: error("Dynamic state key is missing")
        val value = action.payload["value"]
            ?.resolve(bindings, context, previousResponse)
            ?.asString()
            ?: ""
        return DynamicActionResult.StateChanged(key, value)
    }

    private suspend fun sequence(
        action: DynamicAction,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult {
        val actions = action.payload["actions"] as? DynamicValue.ArrayValue
            ?: error("Dynamic sequence actions are missing")
        var last: DynamicActionResult = DynamicActionResult.Completed
        actions.value.forEach { value ->
            val objectValue = value as? DynamicValue.ObjectValue
                ?: error("Dynamic sequence action is invalid")
            val type = string(objectValue.value["type"])
                ?: error("Dynamic sequence action type is missing")
            val nested = DynamicAction(type = type, payload = objectValue.value - "type")
            last = execute(nested, bindings, context, previousResponse).getOrThrow()
        }
        return last
    }

    private fun applyNavigation(action: DynamicAction, destination: DynamicDestination) {
        when (string(action.payload["mode"])?.lowercase()) {
            "push", "add", "present" -> navigation.push(destination)
            "replace" -> navigation.replace(destination)
            else -> navigation.replace(destination)
        }
    }

    private fun DynamicValue.resolve(
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicValue = when (this) {
        is DynamicValue.ObjectValue -> {
            val binding = string(value["${'$'}binding"])
            val contextRef = string(value["${'$'}context"])
            val responseRef = string(value["${'$'}response"])
            val literal = value["${'$'}literal"]
            when {
                binding != null -> DynamicValue.StringValue(bindings[binding].orEmpty())
                contextRef != null -> DynamicValue.StringValue(context[contextRef].orEmpty())
                responseRef != null -> previousResponse?.resolvePath(responseRef) ?: DynamicValue.NullValue
                literal != null -> literal.resolve(bindings, context, previousResponse)
                else -> DynamicValue.ObjectValue(value.mapValues { (_, child) -> child.resolve(bindings, context, previousResponse) })
            }
        }
        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(value.map { it.resolve(bindings, context, previousResponse) })
        else -> this
    }

    private fun DynamicValue.resolvePath(path: String): DynamicValue =
        path.split('.').fold(this) { current, segment ->
            when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> DynamicValue.NullValue
            }
        }

    private fun DynamicValue.asString(): String? = (this as? DynamicValue.StringValue)?.value

    private fun string(value: DynamicValue?): String? = (value as? DynamicValue.StringValue)?.value
}
