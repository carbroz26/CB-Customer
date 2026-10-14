package com.carbroz.cbpartner.data.dynamic.model.section

import com.carbroz.cbpartner.data.dynamic.model.group.StackGroupDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class StackSectionDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<JsonObject> = emptyList(),
    val groups: List<StackGroupDto> = emptyList(),
)