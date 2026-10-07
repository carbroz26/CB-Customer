package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Pure Dynamic reference resolution.
 *
 * This class only resolves the four frozen reference namespaces:
 * $binding, $context, $state and $literal.
 */
internal object DynamicReferenceResolver {
    fun resolve(
        value: DynamicValue,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValue = when (value) {
        is DynamicValue.ObjectValue -> {
            check("$response" !in value.value) {
                "The $response reference is no longer supported; retain response data into RuntimeState and use $state"
            }
            val binding = string(value.value["$binding"])
            val contextRef = string(value.value["$context"])
            val stateRef = string(value.value["$state"])
            val literal = value.value["$literal"]
            when {
                binding != null -> bindings[binding] ?: DynamicValue.NullValue
                contextRef != null -> resolvePath(context, contextRef)
                stateRef != null -> resolvePath(state, stateRef)
                literal != null -> resolve(literal, bindings, context, state)
                else -> DynamicValue.ObjectValue(
                    value.value.mapValues { (_, child) ->
                        resolve(child, bindings, context, state)
                    },
                )
            }
        }

        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(
            value.value.map { child ->
                resolve(child, bindings, context, state)
            },
        )

        else -> value
    }

    fun string(value: DynamicValue?): String? =
        (value as? DynamicValue.StringValue)?.value

    fun boolean(value: DynamicValue?): Boolean? =
        (value as? DynamicValue.BooleanValue)?.value

    fun resolvePath(root: Map<String, DynamicValue>, path: String): DynamicValue {
        val segments = path.split('.').filter(String::isNotBlank)
        if (segments.isEmpty()) return DynamicValue.NullValue
        root[path]?.let { return it }

        var current: DynamicValue = root[segments.first()] ?: return DynamicValue.NullValue
        for (segment in segments.drop(1)) {
            current = when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> return DynamicValue.NullValue
            }
        }
        return current
    }
}
