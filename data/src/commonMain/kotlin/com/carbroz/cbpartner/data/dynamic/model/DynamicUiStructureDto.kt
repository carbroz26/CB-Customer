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

@Serializable
data class DynamicComponentDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<DynamicElementDto> = emptyList(),
    val sections: List<DynamicSectionDto> = emptyList(),
)

@Serializable
data class DynamicSectionDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<DynamicElementDto> = emptyList(),
    val groups: List<DynamicGroupDto> = emptyList(),
)

@Serializable
data class DynamicGroupDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val elements: List<DynamicElementDto> = emptyList(),
)
