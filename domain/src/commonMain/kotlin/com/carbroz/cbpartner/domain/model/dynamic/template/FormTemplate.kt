package com.carbroz.cbpartner.domain.model.dynamic.template

import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding

data class FormTemplate(
    val id: String,
    val type: String = "form_template",
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalArrangement: DynamicArrangement = DynamicArrangement.Start,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val verticalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxSize: Boolean = false,
    val semanticRole: String? = null,
    override val components: List<DynamicComponent> = emptyList(),
) : DynamicTemplate
