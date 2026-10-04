package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class StateActionHandler : DynamicActionHandler {
    override val type: String = "state"

    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult.StateChanged {
        val key = DynamicActionValueResolver.string(action.payload["key"])
            ?: error("Dynamic state key is missing")
        val value = action.payload["value"]?.let {
            DynamicActionValueResolver.resolve(it, bindings, context, previousResponse)
        }
        return DynamicActionResult.StateChanged(key, DynamicActionValueResolver.asString(value))
    }
}
