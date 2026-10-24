package com.carbroz.cbpartner.data.dynamic.model.validation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement

@Serializable
data class DynamicValidationDto(
    val required: Boolean? = null,
    val pattern: String? = null,
    val message: String? = null,
    val requiredMessage: String? = null,
    val lengthMessages: List<DynamicValidationLengthMessageDto> = emptyList(),
    val clearOnEdit: Boolean = false,
    // Reject legacy named rules rather than silently dropping them under ignoreUnknownKeys.
    val validator: JsonElement? = null,
    val equals: DynamicValidationEqualsDto? = null,
)

/** One typed, field-agnostic equality comparison. */
@Serializable
data class DynamicValidationEqualsDto(
    val value: JsonObject,
    val expected: JsonObject,
)

/** Inclusive length bounds for choosing copy after the validator fails. */
@Serializable
data class DynamicValidationLengthMessageDto(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val message: String,
)

