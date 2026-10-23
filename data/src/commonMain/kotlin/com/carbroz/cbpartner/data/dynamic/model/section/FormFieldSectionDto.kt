package com.carbroz.cbpartner.data.dynamic.model.section

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Concrete form-field Section wire contract; not a generic layout property bag. */
@Serializable
data class FormFieldSectionDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<JsonObject> = emptyList(),
    val groups: List<JsonObject> = emptyList(),
)
