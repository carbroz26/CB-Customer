package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

class PresentActionHandler : DynamicActionHandler {
    override val type = "present"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.Navigate {
        val destination = action.destination ?: error("Dynamic present destination is missing")
        return DynamicActionResult.Navigate(
            destination = destination,
            operation = DynamicActionResult.NavigationOperation.Push,
        )
    }
}
