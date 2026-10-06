package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

data class IconElement(
    override val id: String,
    override val semanticRole: String? = null,
    val name: String,
    val size: Double? = null,
    val color: String? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement