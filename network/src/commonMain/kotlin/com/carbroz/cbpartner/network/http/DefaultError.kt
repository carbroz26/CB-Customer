package com.carbroz.cbpartner.network.http

import kotlinx.serialization.Serializable

@Serializable
data class DefaultError(
    val message: String,
)
