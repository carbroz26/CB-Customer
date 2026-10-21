package com.carbroz.cbpartner.domain.model.dynamic.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

data class ImageContent(
    override val id: String? = null,
    override val semanticRole: String? = null,
    val source: DynamicAssetSource,
    val fallbackUrl: String? = null,
    val width: Double? = null,
    val height: Double? = null,
    val contentScale: String? = null,
    override val spacing: DynamicSpacing? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : ElementContent
