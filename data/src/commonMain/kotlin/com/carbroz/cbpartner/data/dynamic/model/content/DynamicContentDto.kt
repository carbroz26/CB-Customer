package com.carbroz.cbpartner.data.dynamic.model.content

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DynamicContentDto(
    val type: JsonElement? = null,
    val id: JsonElement? = null,
    val properties: JsonElement? = null,
    val actions: JsonElement? = null,
)
