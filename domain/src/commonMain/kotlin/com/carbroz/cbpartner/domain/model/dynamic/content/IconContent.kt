package com.carbroz.cbpartner.domain.model.dynamic.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding

data class IconContent(
    override val id: String? = null,
    override val semanticRole: String? = null,
    val url: String,
    val fallbackUrl: String? = null,
    val size: Double? = null,
    val color: String? = null,
    override val padding: DynamicPadding? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : ElementContent
