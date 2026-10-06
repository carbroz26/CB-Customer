package com.carbroz.cbpartner.domain.model.dynamic.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

data class IconContent(
    override val id: String? = null,
    override val semanticRole: String? = null,
    val name: String,
    val size: Double? = null,
    val color: String? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : ElementContent
