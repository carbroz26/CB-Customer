package com.carbroz.cbpartner.data.dynamic.model.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DynamicResponseDto(
    val status: Int,
    val code: String,
    val message: String,
    val data: JsonElement? = null,
    val traceId: String? = null,
)