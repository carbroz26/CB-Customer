package com.carbroz.cbpartner.data.dynamic.model.component

import com.carbroz.cbpartner.data.dynamic.model.element.DynamicElementDto
import com.carbroz.cbpartner.data.dynamic.model.section.DynamicSectionDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicComponentDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<DynamicElementDto> = emptyList(),
    val sections: List<DynamicSectionDto> = emptyList(),
)
