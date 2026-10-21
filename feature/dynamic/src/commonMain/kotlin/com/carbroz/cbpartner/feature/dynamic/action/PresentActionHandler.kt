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
    ): DynamicActionResult.Present {
        require(action.destination == null) { "Dynamic present does not accept a destination" }
        require(action.payload.keys == setOf("presentationId")) {
            "Dynamic present requires only payload.presentationId"
        }
        val presentationId = (action.payload["presentationId"] as? DynamicValue.StringValue)
            ?.value
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: throw IllegalArgumentException("Dynamic present presentationId is missing")
        return DynamicActionResult.Present(presentationId)
    }
}
