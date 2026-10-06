package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicScreen(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: FormTemplate,
    val theme: DynamicTheme?,
)
