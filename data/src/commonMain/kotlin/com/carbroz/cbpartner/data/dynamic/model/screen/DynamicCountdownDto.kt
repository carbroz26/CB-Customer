package com.carbroz.cbpartner.data.dynamic.model.screen

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Backend-authored countdown using an existing retained state duration. */
@Serializable
data class DynamicCountdownDto(
    val id: String,
    val durationSeconds: JsonObject,
)
