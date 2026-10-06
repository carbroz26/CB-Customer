package com.carbroz.cbpartner.data.dynamic.model.theme

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DynamicThemeDto(
    val theme: String? = null,
    val statusBar: String? = null,
    val properties: JsonObject = JsonObject(emptyMap()),
)