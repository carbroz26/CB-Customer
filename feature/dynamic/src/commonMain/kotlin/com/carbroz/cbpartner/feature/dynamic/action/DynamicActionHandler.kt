package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

interface DynamicActionHandler {
    val type: String

    suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult

    /** Optional diagnostic context keeps existing handlers compatible. */
    suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        traceContext: TraceContext?,
    ): DynamicActionResult = handle(action, bindings, context, state)
}
