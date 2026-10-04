package com.carbroz.cbpartner.domain.model

data class DynamicDestination(
    val screenId: String,
    val templateId: String,
    val templateType: String,
    val endpoint: String,
)
