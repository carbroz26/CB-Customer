package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicReferenceResolver

class NavigateActionHandler : DynamicActionHandler {
    override val type = "navigate"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.Navigate {
        val rawMode = action.payload["mode"]
        val mode = if (rawMode == null) {
            "push"
        } else {
            DynamicReferenceResolver.string(
                DynamicReferenceResolver.resolve(rawMode, bindings, context, state),
            )?.lowercase() ?: throw IllegalArgumentException("Invalid dynamic navigation mode")
        }
        val operation = when (mode) {
            "push" -> DynamicActionResult.NavigationOperation.Push
            "replace" -> DynamicActionResult.NavigationOperation.Replace
            "pop", "back" -> DynamicActionResult.NavigationOperation.Pop
            else -> throw IllegalArgumentException("Unsupported dynamic navigation mode")
        }
        val destination = action.destination
        if (operation != DynamicActionResult.NavigationOperation.Pop) {
            requireNotNull(destination) { "Dynamic navigation destination is missing" }
        }
        return DynamicActionResult.Navigate(destination, operation)
    }
}
