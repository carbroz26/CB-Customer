package com.carbroz.cbpartner.domain.model.dynamic.component

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding

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