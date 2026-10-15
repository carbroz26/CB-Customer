package com.carbroz.cbpartner.data.dynamic.model.component

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class StackComponentDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<JsonObject> = emptyList(),
    val sections: List<JsonObject> = emptyList(),
)