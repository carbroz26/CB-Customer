package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan

data class TextElement(
    override val id: String,
    override val semanticRole: String? = null,
    val text: String = "",
    val spans: List<DynamicTextSpan> = emptyList(),
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val letterSpacing: Double? = null,
    val lineHeight: Double? = null,
    val color: String? = null,
    val textAlign: String = "center",
    val fillMaxWidth: Boolean = false,
    override val leadingContent: List<ElementContent> = emptyList(),
    override val trailingContent: List<ElementContent> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
