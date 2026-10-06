package com.carbroz.cbpartner.data.dynamic.model.element

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicBindingDto
import com.carbroz.cbpartner.data.dynamic.model.validation.DynamicValidationDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TextElementDto(
    val id: String,
    val type: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val validation: DynamicValidationDto? = null,
    val binding: DynamicBindingDto? = null,
    val actions: DynamicActionsDto? = null,
)