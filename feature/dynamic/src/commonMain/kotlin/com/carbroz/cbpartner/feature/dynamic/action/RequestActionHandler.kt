package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository

class RequestActionHandler(
    private val repository: DynamicRepository,
) : DynamicActionHandler {
    override val type = "request"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult {
        val endpoint = resolveString(action.payload["endpoint"], bindings, context, state)
            ?: error("Dynamic request endpoint is missing")
        val method = resolveString(action.payload["method"], bindings, context, state) ?: "POST"
        val authentication = resolveString(action.payload["authentication"], bindings, context, state) ?: "NONE"
        val validate = resolveBoolean(action.payload["validate"], bindings, context, state) ?: false
        val responseMode = resolveString(action.payload["responseMode"], bindings, context, state) ?: "destination"
        val body = action.payload["body"]?.let { DynamicActionValueResolver.resolve(it, bindings, context, state) }

        val response = repository.fetch(
            DynamicRequest(endpoint, method, authentication, validate, body, responseMode),
        ).getOrThrow()

        val stateChanges = extractRetainedState(action.payload["retain"], response.dataPayload)
        if (responseMode.equals("none", true)) {
            return if (stateChanges.isEmpty()) DynamicActionResult.Completed
            else DynamicActionResult.StateChanged(stateChanges)
        }

        val destination = response.destination ?: error("Dynamic request did not provide data.destination")
        val operation = when (resolveString(action.payload["mode"], bindings, context, state)?.lowercase()) {
            "replace" -> DynamicActionResult.NavigationOperation.Replace
            "pop", "back" -> DynamicActionResult.NavigationOperation.Pop
            else -> DynamicActionResult.NavigationOperation.Push
        }
        return DynamicActionResult.Navigate(destination, operation, stateChanges)
    }

    private fun resolve(
        value: DynamicValue?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValue = value?.let { DynamicActionValueResolver.resolve(it, bindings, context, state) } ?: DynamicValue.NullValue

    private fun resolveString(
        value: DynamicValue?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): String? = DynamicActionValueResolver.string(resolve(value, bindings, context, state))

    private fun resolveBoolean(
        value: DynamicValue?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): Boolean? = DynamicActionValueResolver.boolean(resolve(value, bindings, context, state))

    private fun extractRetainedState(
        retain: DynamicValue?,
        dataPayload: DynamicValue?,
    ): Map<String, DynamicValue> {
        val mappings = retain as? DynamicValue.ObjectValue ?: return emptyMap()
        val root = DynamicValue.ObjectValue(
            mapOf("data" to (dataPayload ?: DynamicValue.NullValue)),
        )
        return mappings.value.mapNotNull { (key, pathValue) ->
            val path = DynamicActionValueResolver.string(pathValue) ?: return@mapNotNull null
            key to DynamicActionValueResolver.resolvePath(root.value, path)
        }.toMap()
    }
}
