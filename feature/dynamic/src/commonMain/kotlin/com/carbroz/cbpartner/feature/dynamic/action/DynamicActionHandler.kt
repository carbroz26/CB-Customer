package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue

class DynamicActionHandler {
    fun destination(action: DynamicAction): DynamicDestination {
        val screenId = string(action.payload["screenId"])
            ?: error("Dynamic navigation screenId is missing")
        val endpoint = string(action.payload["endpoint"])
            ?: error("Dynamic navigation endpoint is missing")

        return DynamicDestination(
            screenId = screenId,
            templateId = string(action.payload["templateId"]),
            templateType = string(action.payload["templateType"]),
            endpoint = endpoint,
            method = string(action.payload["method"]) ?: "GET",
            authentication = string(action.payload["authentication"]),
        )
    }

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

    private fun string(value: DynamicValue?): String? =
        (value as? DynamicValue.StringValue)?.value
}
