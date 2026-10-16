package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.CancellationException

class DynamicActionExecutor(
    handlers: List<DynamicActionHandler>,
) {
    private val handlersByType: Map<String, DynamicActionHandler> = buildMap {
        handlers.forEach { handler ->
            val key = handler.type.trim().lowercase()
            check(put(key, handler) == null) {
                "Duplicate dynamic action handler: " + handler.type
            }
        }
    }

    suspend fun execute(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        traceContext: TraceContext? = null,
    ): Result<DynamicActionResult> = runCatching {
        val normalizedType = action.type.trim().lowercase()
        val handler = handlersByType[if (normalizedType == "back") "dismiss" else normalizedType]
            ?: error("Unsupported dynamic action: " + action.type)
        handler.handle(action, bindings, context, state, traceContext)
    }.onFailure { error ->
        if (error is CancellationException) throw error
    }
}
