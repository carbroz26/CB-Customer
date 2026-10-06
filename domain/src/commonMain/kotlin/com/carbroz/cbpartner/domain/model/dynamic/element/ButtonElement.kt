package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape

data class ButtonElement(
    override val id: String,
    override val semanticRole: String? = null,
    val text: String = "",
    val height: Double? = null,
    val fillMaxWidth: Boolean = false,
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val textColor: String? = null,
    val background: DynamicBackground? = null,
    val shape: DynamicShape? = null,
    override val leadingContent: List<ElementContent> = emptyList(),
    override val trailingContent: List<ElementContent> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement
