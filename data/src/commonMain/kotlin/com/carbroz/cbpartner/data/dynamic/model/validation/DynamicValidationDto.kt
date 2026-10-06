package com.carbroz.cbpartner.data.dynamic.model.validation

import kotlinx.serialization.Serializable

@Serializable
data class DynamicValidationDto(
    val required: Boolean? = null,
    val pattern: String? = null,
    val message: String? = null,
)