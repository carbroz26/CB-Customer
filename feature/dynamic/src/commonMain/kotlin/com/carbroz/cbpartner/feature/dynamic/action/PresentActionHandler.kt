package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class PresentActionHandler : DynamicActionHandler {
    override val type: String = "present"

    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult.Navigate {
        val destination = action.destination ?: error("Dynamic present destination is missing")
        navigation.push(destination)
        return DynamicActionResult.Navigate(destination)
    }
}
