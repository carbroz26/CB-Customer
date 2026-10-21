package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.template.DynamicTemplate
import com.carbroz.cbpartner.domain.model.dynamic.theme.DynamicTheme
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicScreen(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: DynamicTemplate,
    val theme: DynamicTheme?,
    val initialBindings: Map<String, DynamicValue> = emptyMap(),
    val presentations: List<DynamicPresentation> = emptyList(),
)
