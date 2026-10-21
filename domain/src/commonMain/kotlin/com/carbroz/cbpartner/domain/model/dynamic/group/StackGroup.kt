package com.carbroz.cbpartner.domain.model.dynamic.group

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicFocusPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSelectedStyle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation

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
    override val elements: List<DynamicElement> = emptyList(),
    val minHeight: Double? = null,
    val selectedStyle: DynamicSelectedStyle? = null,
    val focusPresentation: DynamicFocusPresentation? = null,
    val validationPresentation: DynamicValidationPresentation? = null,
) : DynamicGroup
