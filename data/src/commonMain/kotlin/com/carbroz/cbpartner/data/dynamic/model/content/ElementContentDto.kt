package com.carbroz.cbpartner.data.dynamic.model.content

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ElementContentDto(
    val id: String? = null,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val actions: DynamicActionsDto? = null,
)
