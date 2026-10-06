package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicInputPresentation(
    val type: String,
    val count: Int? = null,
    val spacing: Double? = null,
    val segmentWidth: Double? = null,
    val segmentHeight: Double? = null,
)
