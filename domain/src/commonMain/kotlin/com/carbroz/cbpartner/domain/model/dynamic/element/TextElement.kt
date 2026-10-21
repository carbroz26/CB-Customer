package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan

data class TextElement(
    val id: String,
    val semanticRole: String? = null,
    val text: String = "",
    val spans: List<DynamicTextSpan> = emptyList(),
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val letterSpacing: Double? = null,
    val lineHeight: Double? = null,
    val color: String? = null,
    val textAlign: String? = null,
    val width: Double? = null,
    val maxWidth: Double? = null,
    val fillMaxWidth: Boolean = false,
    val padding: DynamicPadding? = null,
    val leadingContent: List<ElementContent> = emptyList(),
    val trailingContent: List<ElementContent> = emptyList(),
    val actions: Map<String, DynamicAction> = emptyMap(),
    override val weight: Double? = null,
    override val minHeight: Double? = null,
    override val spacing: DynamicSpacing? = null,
    val maxLines: Int? = null,
    val overflow: String? = null,
) : DynamicElement
