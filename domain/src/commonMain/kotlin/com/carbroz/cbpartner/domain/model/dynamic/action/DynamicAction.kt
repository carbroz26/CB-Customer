package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.DynamicDestination

data class DynamicAction(
    val type: String,
    val payload: Map<String, DynamicValue> = emptyMap(),
    val destination: DynamicDestination? = null,
)
