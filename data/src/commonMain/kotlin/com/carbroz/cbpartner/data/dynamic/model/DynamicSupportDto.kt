package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicValidationDto(
    val required: Boolean? = null,
    val pattern: String? = null,
    val message: String? = null,
)

@Serializable
data class DynamicThemeDto(
    val theme: String? = null,
    val statusBar: String? = null,
    val properties: JsonObject = JsonObject(emptyMap()),
)
