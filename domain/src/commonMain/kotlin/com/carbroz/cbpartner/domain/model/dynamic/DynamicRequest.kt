package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicRequest(
    val endpoint: String,
    val method: String = "GET",
    val authentication: String = "NONE",
    val validate: Boolean = false,
    val body: DynamicValue? = null,
    val responseMode: String = "destination",
    /** Diagnostic-only identifiers. Never sent as headers or included in request JSON. */
    val sessionId: String? = null,
    val actionId: String? = null,
    val parentActionId: String? = null,
)
