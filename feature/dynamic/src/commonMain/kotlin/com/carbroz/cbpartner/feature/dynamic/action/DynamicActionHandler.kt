package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue

class DynamicActionHandler {
    fun destination(action: DynamicAction): DynamicDestination =
        action.destination ?: error("Dynamic navigation destination is missing")

    fun externalUri(action: DynamicAction, context: Map<String, String>): String {
        val value = action.payload["uri"] ?: error("Dynamic external URI is missing")
        return when (value) {
            is DynamicValue.StringValue -> value.value
            is DynamicValue.ObjectValue -> {
                val reference = (value.value["${'$'}context"] as? DynamicValue.StringValue)?.value
                    ?: error("Dynamic external URI reference is invalid")
                context[reference] ?: error("Dynamic context value is missing: $reference")
            }
            else -> error("Dynamic external URI value is invalid")
        }
    }
}
