package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class NavigateActionHandler : DynamicActionHandler {
    override val type = "navigate"
    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.Navigate {
        val destination = action.destination ?: error("Dynamic navigation destination is missing")
        when (DynamicActionValueResolver.string(action.payload["mode"])?.lowercase()) {
            "push", "add" -> navigation.push(destination)
            else -> navigation.replace(destination)
        }
        return DynamicActionResult.Navigate(destination)
    }
}
