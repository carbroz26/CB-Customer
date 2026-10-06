package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicElementDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val validation: DynamicValidationDto? = null,
    val binding: DynamicBindingDto? = null,
    val actions: DynamicActionsDto? = null,
)
