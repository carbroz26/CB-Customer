package com.carbroz.cbpartner.data.dynamic.model.section

import com.carbroz.cbpartner.data.dynamic.model.element.DynamicElementDto
import com.carbroz.cbpartner.data.dynamic.model.group.DynamicGroupDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicSectionDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<DynamicElementDto> = emptyList(),
    val groups: List<DynamicGroupDto> = emptyList(),
)
