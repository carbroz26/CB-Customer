package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue

internal object DynamicActionValueResolver {
    fun resolve(
        value: DynamicValue,
        bindings: Map<String, String>,
        context: Map<String, String>,
        previousResponse: DynamicValue?,
    ): DynamicValue = when (value) {
        is DynamicValue.ObjectValue -> {
            val binding = string(value.value["${'$'}binding"])
            val contextRef = string(value.value["${'$'}context"])
            val responseRef = string(value.value["${'$'}response"])
            val literal = value.value["${'$'}literal"]
            when {
                binding != null -> DynamicValue.StringValue(bindings[binding].orEmpty())
                contextRef != null -> DynamicValue.StringValue(context[contextRef].orEmpty())
                responseRef != null -> previousResponse?.resolvePath(responseRef) ?: DynamicValue.NullValue
                literal != null -> resolve(literal, bindings, context, previousResponse)
                else -> DynamicValue.ObjectValue(value.value.mapValues { (_, child) -> resolve(child, bindings, context, previousResponse) })
            }
        }
        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(value.value.map { resolve(it, bindings, context, previousResponse) })
        else -> value
    }

    private fun DynamicValue.resolvePath(path: String): DynamicValue =
        path.split('.').fold(this) { current, segment ->
            when (current) {
                is DynamicValue.ObjectValue -> current.value[segment] ?: DynamicValue.NullValue
                else -> DynamicValue.NullValue
            }
        }

    fun string(value: DynamicValue?): String? = (value as? DynamicValue.StringValue)?.value
    fun asString(value: DynamicValue?): String = string(value).orEmpty()
}
