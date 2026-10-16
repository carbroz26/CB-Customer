package com.carbroz.cbpartner.data.dynamic.model.template

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TemplateSlotDto(
    val properties: JsonObject = JsonObject(emptyMap()),
    val components: List<JsonObject> = emptyList(),
)
