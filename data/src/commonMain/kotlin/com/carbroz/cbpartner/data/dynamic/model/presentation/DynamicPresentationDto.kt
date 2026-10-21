package com.carbroz.cbpartner.data.dynamic.model.presentation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicPresentationDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val initialBindings: JsonObject = JsonObject(emptyMap()),
    val components: List<JsonObject> = emptyList(),
)
