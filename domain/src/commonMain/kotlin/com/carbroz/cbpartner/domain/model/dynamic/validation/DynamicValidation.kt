package com.carbroz.cbpartner.domain.model.dynamic.validation

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicValidation(
    val required: Boolean?,
    val pattern: String?,
    val message: String?,
    val validator: DynamicValidatorSpec? = null,
    val requiredMessage: String? = null,
    val lengthMessages: List<DynamicValidationLengthMessage> = emptyList(),
    val clearOnEdit: Boolean = false,
)

data class DynamicValidationLengthMessage(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val message: String,
)

data class DynamicValidatorSpec(
    val key: String,
    val arguments: Map<String, DynamicValue> = emptyMap(),
)
