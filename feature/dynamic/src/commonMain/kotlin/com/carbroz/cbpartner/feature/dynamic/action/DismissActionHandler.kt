package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class DismissActionHandler : DynamicActionHandler {
    override val type: String = "dismiss"

    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicActionResult {
        navigation.pop()
        return DynamicActionResult.Completed
    }
}
