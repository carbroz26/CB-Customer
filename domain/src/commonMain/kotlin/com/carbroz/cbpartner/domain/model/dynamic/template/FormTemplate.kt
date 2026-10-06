package com.carbroz.cbpartner.domain.model.dynamic

data class FormTemplate(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxSize: Boolean = false,
    val semanticRole: String? = null,
    val components: List<StackComponent> = emptyList(),
)
