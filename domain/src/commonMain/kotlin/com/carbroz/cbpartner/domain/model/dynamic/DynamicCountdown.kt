package com.carbroz.cbpartner.domain.model.dynamic

/** Duration comes from a retained backend response value; no timing policy is owned by UI. */
data class DynamicCountdown(
    val id: String,
    val durationStatePath: String,
)
