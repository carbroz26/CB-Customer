package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicActionDto(
    val type: String,
    val payload: JsonObject = JsonObject(emptyMap()),
    val destination: DynamicDestinationDto? = null,
)
