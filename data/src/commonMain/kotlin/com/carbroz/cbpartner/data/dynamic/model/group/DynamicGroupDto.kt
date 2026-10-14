package com.carbroz.cbpartner.data.dynamic.model.group

import com.carbroz.cbpartner.data.dynamic.model.element.DynamicElementDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicGroupDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<DynamicElementDto> = emptyList(),
)
