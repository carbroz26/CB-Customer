package com.carbroz.cbpartner.domain.model.dynamic.validation

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicValidation(
    val required: Boolean?,
    val pattern: String?,
    val message: String?,
    val validator: DynamicValidatorSpec? = null,
)

data class DynamicValidatorSpec(
    val key: String,
    val arguments: Map<String, DynamicValue> = emptyMap(),
)
