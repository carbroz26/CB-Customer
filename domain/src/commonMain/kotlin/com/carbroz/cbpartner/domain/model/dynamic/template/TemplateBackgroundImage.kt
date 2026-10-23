package com.carbroz.cbpartner.domain.model.dynamic.template

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource

/**
 * Backend-defined background image belonging to the complete Template surface.
 * Theme gradients remain independent; this does not own screen- or feature-specific data.
 */
data class TemplateBackgroundImage(
    val source: DynamicAssetSource,
    val contentScale: String,
    val alignment: String,
    val fallbackUrl: String? = null,
)
