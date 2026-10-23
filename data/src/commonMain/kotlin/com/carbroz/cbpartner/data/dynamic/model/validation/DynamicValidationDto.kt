package com.carbroz.cbpartner.data.dynamic.model.validation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicValidationDto(
    val required: Boolean? = null,
    val pattern: String? = null,
    val message: String? = null,
    val requiredMessage: String? = null,
    val lengthMessages: List<DynamicValidationLengthMessageDto> = emptyList(),
    val validator: DynamicValidatorDto? = null,
)

/** Inclusive length bounds for choosing copy after the validator fails. */
@Serializable
data class DynamicValidationLengthMessageDto(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val message: String,
)

@Serializable
data class DynamicValidatorDto(
    val key: String,
    val arguments: JsonObject = JsonObject(emptyMap()),
)
