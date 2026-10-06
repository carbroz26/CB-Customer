package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent

data class IconElement(
    override val id: String,
    override val semanticRole: String? = null,
    val name: String,
    val size: Double? = null,
    val color: String? = null,
    override val leadingContent: List<ElementContent> = emptyList(),
    override val trailingContent: List<ElementContent> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
