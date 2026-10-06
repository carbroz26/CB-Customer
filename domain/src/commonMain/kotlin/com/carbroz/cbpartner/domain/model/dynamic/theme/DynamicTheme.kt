package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicTheme(
    val theme: String?,
    val statusBar: String?,
    val properties: Map<String, DynamicValue>,
)
