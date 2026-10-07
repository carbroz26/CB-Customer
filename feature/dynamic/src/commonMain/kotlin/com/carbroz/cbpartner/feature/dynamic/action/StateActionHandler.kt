package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

class StateActionHandler : DynamicActionHandler {
    override val type = "state"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.StateChanged {
        val key = DynamicActionValueResolver.string(action.payload["key"])
            ?: error("Dynamic state key is missing")
        val value = action.payload["value"]
            ?.let { DynamicActionValueResolver.resolve(it, bindings, context, state) }
            ?: DynamicValue.NullValue
        return DynamicActionResult.StateChanged(mapOf(key to value))
    }
}
