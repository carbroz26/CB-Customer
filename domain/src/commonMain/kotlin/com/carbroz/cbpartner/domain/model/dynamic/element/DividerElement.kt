package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

data class DividerElement(
    override val id: String,
    override val semanticRole: String? = null,
    val orientation: String = "horizontal",
    val width: Double? = null,
    val height: Double? = null,
    val thickness: Double = 1.0,
    val color: String? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement