package com.carbroz.cbpartner.domain.model.dynamic.theme

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicTheme(
    val theme: String?,
    val statusBar: String?,
    val properties: Map<String, DynamicValue>,
)