package com.carbroz.cbpartner.domain.model.dynamic.validation

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicValidation(
    val required: Boolean?,
    val pattern: String?,
    val message: String?,
    val requiredMessage: String? = null,
    val lengthMessages: List<DynamicValidationLengthMessage> = emptyList(),
    val clearOnEdit: Boolean = false,
    val equals: DynamicValidationEquality? = null,
)

/** Backend-authored equality between two typed Dynamic references. */
data class DynamicValidationEquality(
    val value: DynamicValue,
    val expected: DynamicValue,
)

data class DynamicValidationLengthMessage(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val message: String,
)

