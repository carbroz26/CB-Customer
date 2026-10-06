package com.carbroz.cbpartner.data.dynamic.model

import kotlinx.serialization.Serializable

@Serializable
data class DynamicDestinationDto(
    val screenId: String,
    val templateId: String,
    val templateType: String,
    val endpoint: String,
)
