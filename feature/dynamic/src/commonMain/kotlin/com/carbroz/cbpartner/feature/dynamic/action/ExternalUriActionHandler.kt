package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

class ExternalUriActionHandler : DynamicActionHandler {
    override val type = "external_uri"
    override suspend fun handle(
        action: DynamicAction,
        navigation: DynamicNavigationState,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.ExternalUri {
        val value = action.payload["uri"] ?: error("Dynamic external URI is missing")
        val uri = when (value) {
            is DynamicValue.StringValue -> value.value
            is DynamicValue.ObjectValue -> {
                val reference = DynamicActionValueResolver.string(value.value["\$context"])
                    ?: error("Dynamic external URI reference is invalid")
                (DynamicActionValueResolver.resolvePath(context, reference) as? DynamicValue.StringValue)?.value
                    ?: error("Dynamic context value is missing: $reference")
            }
            else -> error("Dynamic external URI value is invalid")
        }
        return DynamicActionResult.ExternalUri(uri)
    }
}
