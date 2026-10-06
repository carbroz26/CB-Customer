package com.carbroz.cbpartner.domain.model.dynamic

data class StackComponent(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalArrangement: DynamicArrangement = DynamicArrangement.Start,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val verticalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxWidth: Boolean = false,
    val elements: List<DynamicElement> = emptyList(),
    val sections: List<StackSection> = emptyList(),
)
