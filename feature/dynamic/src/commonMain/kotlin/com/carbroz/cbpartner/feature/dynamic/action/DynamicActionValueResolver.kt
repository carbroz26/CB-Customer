package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

internal object DynamicActionValueResolver {
    fun resolve(
        value: DynamicValue,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValue = when (value) {
        is DynamicValue.ObjectValue -> {
            val binding = string(value.value["\$binding"])
            val contextRef = string(value.value["\$context"])
            val stateRef = string(value.value["\$state"])
            val literal = value.value["\$literal"]
            when {
                binding != null -> bindings[binding] ?: DynamicValue.NullValue
                contextRef != null -> resolvePath(context, contextRef)
                stateRef != null -> resolvePath(state, stateRef)
                literal != null -> resolve(literal, bindings, context, state)
                else -> DynamicValue.ObjectValue(value.value.mapValues { (_, child) ->
                    resolve(child, bindings, context, state)
                })
            }
        }
        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(value.value.map {
            resolve(it, bindings, context, state)
        })
        else -> value
    }

    fun string(value: DynamicValue?): String? = (value as? DynamicValue.StringValue)?.value
    fun boolean(value: DynamicValue?): Boolean? = (value as? DynamicValue.BooleanValue)?.value
    fun asString(value: DynamicValue?): String = string(value).orEmpty()

    fun resolvePath(root: Map<String, DynamicValue>, path: String): DynamicValue {
        val segments = path.split('.').filter(String::isNotBlank)
        if (segments.isEmpty()) return DynamicValue.NullValue
        var current: DynamicValue = root[segments.first()] ?: return DynamicValue.NullValue
        for (segment in segments.drop(1)) {
            current = when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> return DynamicValue.NullValue
            }
        }
        return current
    }

    fun resolvePath(root: DynamicValue, path: String): DynamicValue {
        val segments = path.split('.').filter(String::isNotBlank)
        var current = root
        for (segment in segments) {
            current = when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> return DynamicValue.NullValue
            }
        }
        return current
    }
}
