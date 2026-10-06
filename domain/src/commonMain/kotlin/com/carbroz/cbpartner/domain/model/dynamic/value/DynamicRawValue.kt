package com.carbroz.cbpartner.domain.model.dynamic

sealed interface DynamicValue {
    data class StringValue(val value: String) : DynamicValue
    data class NumberValue(val value: Double) : DynamicValue
    data class BooleanValue(val value: Boolean) : DynamicValue
    data class ObjectValue(val value: Map<String, DynamicValue>) : DynamicValue
    data class ArrayValue(val value: List<DynamicValue>) : DynamicValue
    data object NullValue : DynamicValue
}
