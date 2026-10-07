package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

class ExternalUriActionHandler : DynamicActionHandler {
    override val type = "external_uri"

    override suspend fun handle(
        action: DynamicAction,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicActionResult.ExternalUri {
        val value = action.payload["uri"] ?: error("Dynamic external URI is missing")
        val uri = (DynamicActionValueResolver.resolve(value, bindings, context, state) as? DynamicValue.StringValue)?.value
            ?: error("Dynamic external URI value is invalid")
        return DynamicActionResult.ExternalUri(uri)
    }
}
