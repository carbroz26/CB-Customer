package com.carbroz.cbpartner.domain.model.dynamic.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

data class IconContent(
    override val id: String? = null,
    override val semanticRole: String? = null,
    val url: String = "",
    val fallbackUrl: String? = null,
    val size: Double? = null,
    val color: String? = null,
    override val spacing: DynamicSpacing? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
    val source: DynamicAssetSource? = null,
) : ElementContent
