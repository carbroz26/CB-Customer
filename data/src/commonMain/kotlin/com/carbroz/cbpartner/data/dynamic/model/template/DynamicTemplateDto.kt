package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicTemplateDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val components: List<DynamicComponentDto> = emptyList(),
)
