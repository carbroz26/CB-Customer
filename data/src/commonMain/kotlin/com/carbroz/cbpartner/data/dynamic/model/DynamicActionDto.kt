package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
    val destination: DynamicDestinationDto? = null,
)

@Serializable
data class DynamicBindingDto(val key: String)
