package com.carbroz.cbpartner.data.dynamic.model.template

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class FormTemplateDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val components: List<JsonObject> = emptyList(),
)