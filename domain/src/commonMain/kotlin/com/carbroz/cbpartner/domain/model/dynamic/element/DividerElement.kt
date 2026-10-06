package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent

data class DividerElement(
    override val id: String,
    override val semanticRole: String? = null,
    val orientation: String = "horizontal",
    val width: Double? = null,
    val height: Double? = null,
    val thickness: Double = 1.0,
    val color: String? = null,
    override val leadingContent: List<ElementContent> = emptyList(),
    override val trailingContent: List<ElementContent> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
