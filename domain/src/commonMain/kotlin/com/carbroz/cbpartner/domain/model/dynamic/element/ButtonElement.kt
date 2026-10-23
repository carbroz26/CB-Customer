package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicButtonLoading
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape

data class ButtonElement(
    val id: String,
    val semanticRole: String? = null,
    val text: String = "",
    val width: Double? = null,
    val maxWidth: Double? = null,
    val height: Double? = null,
    val fillMaxWidth: Boolean = false,
    val fontSize: Double? = null,
    val lineHeight: Double? = null,
    val fontWeight: Int? = null,
    val textColor: String? = null,
    val background: DynamicBackground? = null,
    val shape: DynamicShape? = null,
    val padding: DynamicPadding? = null,
    val leadingContent: List<ElementContent> = emptyList(),
    val trailingContent: List<ElementContent> = emptyList(),
    val actions: Map<String, DynamicAction> = emptyMap(),
    val loading: DynamicButtonLoading? = null,
) : DynamicElement
