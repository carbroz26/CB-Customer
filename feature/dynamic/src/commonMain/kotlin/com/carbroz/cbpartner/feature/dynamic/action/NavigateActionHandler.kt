package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

class NavigateActionHandler : DynamicActionHandler {
    override val type = "navigate"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.Navigate {
        val destination = action.destination ?: error("Dynamic navigation destination is missing")
        val operation = when (DynamicActionValueResolver.string(
                DynamicActionValueResolver.resolve(action.payload["mode"] ?: DynamicValue.NullValue, bindings, context, state),
            )?.lowercase()) {
            "push", "add" -> DynamicActionResult.NavigationOperation.Push
            else -> DynamicActionResult.NavigationOperation.Replace
        }
        return DynamicActionResult.Navigate(destination, operation)
    }
}
