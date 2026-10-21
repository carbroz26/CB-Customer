package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

data class DividerElement(
    val id: String,
    val orientation: String = "horizontal",
    val width: Double? = null,
    val height: Double? = null,
    val thickness: Double = 1.0,
    val color: String? = null,
    override val spacing: DynamicSpacing? = null,
    val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
