package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: DynamicScreen?,
    val traceId: String?,
    val dataPayload: DynamicValue? = null,
    val destination: DynamicDestination? = null,
)