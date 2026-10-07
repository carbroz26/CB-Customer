package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class RequestActionHandler(private val repository: DynamicRepository) : DynamicActionHandler {
    override val type = "request"
    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult {
        val endpoint = DynamicActionValueResolver.string(action.payload["endpoint"])
            ?: error("Dynamic request endpoint is missing")
        val method = DynamicActionValueResolver.string(action.payload["method"]) ?: "POST"
        val authentication = DynamicActionValueResolver.string(action.payload["authentication"]) ?: "NONE"
        val validate = DynamicActionValueResolver.boolean(action.payload["validate"]) ?: false
        val responseMode = DynamicActionValueResolver.string(action.payload["responseMode"]) ?: "destination"
        val body = action.payload["body"]?.let {
            DynamicActionValueResolver.resolve(it, bindings, context, state)
        }
        val response = repository.fetch(
            DynamicRequest(endpoint, method, authentication, validate, body, responseMode),
        ).getOrThrow()
        val stateChanges = extractRetainedState(action.payload["retain"], response.dataPayload)
        if (responseMode.equals("none", true)) {
            return DynamicActionResult.Navigate(navigation.currentDestination, stateChanges)
        }
        val destination = response.destination ?: error("Dynamic request did not provide data.destination")
        when (DynamicActionValueResolver.string(action.payload["mode"])?.lowercase()) {
            "push", "add" -> navigation.push(destination)
            else -> navigation.replace(destination)
        }
        return DynamicActionResult.Navigate(destination, stateChanges)
    }

    private fun extractRetainedState(
        retain: DynamicValue?,
        dataPayload: DynamicValue?,
    ): Map<String, DynamicValue> {
        val mappings = retain as? DynamicValue.ObjectValue ?: return emptyMap()
        val root = DynamicValue.ObjectValue(mapOf("data" to (dataPayload ?: DynamicValue.NullValue)))
        return mappings.value.mapNotNull { (key, pathValue) ->
            val path = DynamicActionValueResolver.string(pathValue) ?: return@mapNotNull null
            key to DynamicActionValueResolver.resolvePath(root, path)
        }.toMap()
    }
}
