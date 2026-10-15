package com.carbroz.cbpartner.data.dynamic.model.screen

import kotlinx.serialization.json.JsonObject
import com.carbroz.cbpartner.data.dynamic.model.theme.DynamicThemeDto
import kotlinx.serialization.Serializable

@Serializable
data class DynamicScreenDto(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: JsonObject,
    val theme: DynamicThemeDto? = null,
)