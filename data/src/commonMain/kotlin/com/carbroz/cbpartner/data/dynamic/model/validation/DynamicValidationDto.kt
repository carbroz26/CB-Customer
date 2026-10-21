package com.carbroz.cbpartner.data.dynamic.model.validation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicValidationDto(
    val required: Boolean? = null,
    val pattern: String? = null,
    val message: String? = null,
    val validator: DynamicValidatorDto? = null,
)

@Serializable
data class DynamicValidatorDto(
    val key: String,
    val arguments: JsonObject = JsonObject(emptyMap()),
)
