package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable

@Serializable
data class DynamicActionsDto(
    val onClick: DynamicActionDto? = null,
    val onLongClick: DynamicActionDto? = null,
    val onValueChange: DynamicActionDto? = null,
    val onFocus: DynamicActionDto? = null,
    val onSubmit: DynamicActionDto? = null,
)
