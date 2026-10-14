package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.theme.DynamicTheme

data class DynamicScreen(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: FormTemplate,
    val theme: DynamicTheme?,
)