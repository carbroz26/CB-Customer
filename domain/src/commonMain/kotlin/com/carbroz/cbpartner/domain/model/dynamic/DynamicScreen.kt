package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.dynamic.template.DynamicTemplate
import com.carbroz.cbpartner.domain.model.dynamic.theme.DynamicTheme

data class DynamicScreen(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: DynamicTemplate,
    val theme: DynamicTheme?,
)