package com.carbroz.cbpartner.data.dynamic.model.section

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ListSectionDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val groups: List<JsonObject> = emptyList(),
    val emptyGroup: JsonObject? = null,
)
