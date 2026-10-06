package com.carbroz.cbpartner.domain.model.dynamic

data class StackSection(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxWidth: Boolean = false,
    val elements: List<DynamicElement> = emptyList(),
    val groups: List<StackGroup> = emptyList(),
)
