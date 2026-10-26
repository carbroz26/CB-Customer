package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

/** A single circular surface and its centered, explicitly authored image. */
data class IconBadgeElement(
    val id: String,
    val source: DynamicAssetSource,
    val size: Double,
    val imageSize: Double,
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
    val contentScale: String? = null,
    override val spacing: DynamicSpacing? = null,
) : DynamicElement

