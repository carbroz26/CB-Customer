package com.carbroz.cbpartner.data.dynamic.model.screen

import com.carbroz.cbpartner.data.dynamic.model.template.DynamicTemplateDto
import com.carbroz.cbpartner.data.dynamic.model.theme.DynamicThemeDto
import kotlinx.serialization.Serializable

@Serializable
data class DynamicScreenDto(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: DynamicTemplateDto,
    val theme: DynamicThemeDto? = null,
)
