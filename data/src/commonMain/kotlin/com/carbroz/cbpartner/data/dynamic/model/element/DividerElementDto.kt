package com.carbroz.cbpartner.data.dynamic.model.element

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DividerElementDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val actions: DynamicActionsDto? = null,
)
