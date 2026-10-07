package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicReferenceResolver

internal object DynamicActionValueResolver {
    fun resolve(
        value: DynamicValue,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValue = DynamicReferenceResolver.resolve(value, bindings, context, state)

    fun string(value: DynamicValue?): String? =
        DynamicReferenceResolver.string(value)

    fun boolean(value: DynamicValue?): Boolean? =
        DynamicReferenceResolver.boolean(value)

    fun asString(value: DynamicValue?): String =
        string(value).orEmpty()

    fun resolvePath(root: Map<String, DynamicValue>, path: String): DynamicValue =
        DynamicReferenceResolver.resolvePath(root, path)
}
