package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class DynamicActionExecutor(
    handlers: List<DynamicActionHandler>,
) {
    private val handlersByType = handlers.associateBy { it.type }

    suspend fun execute(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): Result<DynamicActionResult> = runCatching {
        val handler = handlersByType[action.type.lowercase()]
            ?: handlersByType[if (action.type.lowercase() == "back") "dismiss" else action.type.lowercase()]
            ?: error("Unsupported dynamic action: ${action.type}")
        handler.handle(action, navigation, bindings, context, previousResponse)
    }
}
