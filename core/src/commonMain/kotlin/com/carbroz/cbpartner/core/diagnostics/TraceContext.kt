package com.carbroz.cbpartner.core.diagnostics

/**
 * Carries locally generated diagnostic identifiers across execution boundaries.
 *
 * These identifiers are for diagnostics only. They must never contain
 * credentials, personal information, request bodies, or backend-supplied values.
 *
 * A requestId is created separately for each HTTP execution.
 */
data class TraceContext(
    val sessionId: String,
    val actionId: String? = null,
    val parentActionId: String? = null,
)
