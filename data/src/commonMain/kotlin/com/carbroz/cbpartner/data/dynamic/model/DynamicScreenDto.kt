package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable

@Serializable
data class DynamicScreenDto(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: DynamicTemplateDto,
    val theme: DynamicThemeDto? = null,
)

@Serializable
data class DynamicDestinationDto(
    val screenId: String,
    val templateId: String,
    val templateType: String,
    val endpoint: String,
)
