package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent

data class ImageElement(
    override val id: String,
    override val semanticRole: String? = null,
    val url: String = "",
    val width: Double? = null,
    val height: Double? = null,
    val maxWidth: Double? = null,
    val fillMaxWidth: Boolean = false,
    val contentScale: String = "fit",
    override val leadingContent: List<ElementContent> = emptyList(),
    override val trailingContent: List<ElementContent> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
