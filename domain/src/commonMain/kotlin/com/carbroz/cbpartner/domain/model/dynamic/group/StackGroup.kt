package com.carbroz.cbpartner.domain.model.dynamic

data class StackGroup(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalArrangement: DynamicArrangement = DynamicArrangement.Start,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val verticalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val height: Double? = null,
    val fillMaxWidth: Boolean = false,
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
    val shape: DynamicShape? = null,
    val elements: List<DynamicElement> = emptyList(),
)
