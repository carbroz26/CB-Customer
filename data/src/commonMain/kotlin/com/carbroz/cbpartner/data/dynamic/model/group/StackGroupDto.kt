package com.carbroz.cbpartner.data.dynamic.model.group

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class StackGroupDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<JsonObject> = emptyList(),
)