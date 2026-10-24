package com.carbroz.cbpartner.data.dynamic.model.screen

import com.carbroz.cbpartner.data.dynamic.model.presentation.DynamicPresentationDto
import com.carbroz.cbpartner.data.dynamic.model.theme.DynamicThemeDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicScreenDto(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: JsonObject,
    val theme: DynamicThemeDto? = null,
    val initialBindings: JsonObject = JsonObject(emptyMap()),
    val presentations: List<DynamicPresentationDto> = emptyList(),
    val countdowns: List<DynamicCountdownDto> = emptyList(),
)
