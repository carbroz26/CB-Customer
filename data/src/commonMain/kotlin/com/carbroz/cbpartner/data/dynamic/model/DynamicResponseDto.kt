package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicResponseDto(
    val status: Int,
    val code: String,
    val message: String,
    val data: JsonElement? = null,
    val traceId: String? = null,
)

@Serializable
data class DynamicScreenDto(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: DynamicTemplateDto,
    val theme: DynamicThemeDto? = null,
    val nextScreen: DynamicDestinationDto? = null,
)

@Serializable
data class DynamicDestinationDto(
    val screenId: String,
    val templateId: String?,
    val templateType: String?,
    val endpoint: String,
    val method: String = "GET",
    val authentication: String? = null,
)

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

@Serializable
data class DynamicElementDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val validation: DynamicValidationDto? = null,
    val binding: DynamicBindingDto? = null,
    val actions: DynamicActionsDto? = null,
)

@Serializable
data class DynamicValidationDto(
    val required: Boolean? = null,
    val pattern: String? = null,
    val message: String? = null,
)

@Serializable
data class DynamicBindingDto(
    val key: String,
)

@Serializable
data class DynamicActionsDto(
    val onClick: DynamicActionDto? = null,
    val onLongClick: DynamicActionDto? = null,
    val onValueChange: DynamicActionDto? = null,
    val onFocus: DynamicActionDto? = null,
    val onSubmit: DynamicActionDto? = null,
)

@Serializable
data class DynamicActionDto(
    val type: String,
    val payload: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class DynamicThemeDto(
    val theme: String? = null,
    val statusBar: String? = null,
    val properties: JsonObject = JsonObject(emptyMap()),
)
