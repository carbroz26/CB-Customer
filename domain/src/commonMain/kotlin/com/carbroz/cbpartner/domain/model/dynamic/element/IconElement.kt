package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding

data class IconElement(
    val id: String,
    val semanticRole: String? = null,
    val url: String = "",
    val fallbackUrl: String? = null,
    val size: Double? = null,
    val color: String? = null,
    val padding: DynamicPadding? = null,
    val leadingContent: List<ElementContent> = emptyList(),
    val trailingContent: List<ElementContent> = emptyList(),
    val actions: Map<String, DynamicAction> = emptyMap(),
    val source: DynamicAssetSource? = null,
    override val minWidth: Double? = null,
    override val minHeight: Double? = null,
    val visibleWhenSelected: Boolean? = null,
    val reserveSpaceWhenHidden: Boolean? = null,
) : DynamicElement
