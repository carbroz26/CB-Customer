package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Reserved action type. Cross-element behavior will be defined in a future backend contract.
 * Never execute payload.actions or any other nested action.
 */
class SequenceActionHandler : DynamicActionHandler {
    override val type: String = "sequence"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult = DynamicActionResult.Completed
}
