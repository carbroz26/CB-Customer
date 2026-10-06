package com.carbroz.cbpartner.domain.model.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicAction(
    val type: String,
    val payload: Map<String, DynamicValue> = emptyMap(),
    val destination: DynamicDestination? = null,
)