package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

/**
 * Common marker contract for elements supported by the Dynamic renderer registry.
 *
 * Backend type keys remain in the data-mapping boundary, not in domain models.
 * Parent-owned layout metadata is exposed here so Stack renderers can consume it
 * without introducing element-specific layout wrappers.
 */
interface DynamicElement {
    val weight: Double?
        get() = null
    val minWidth: Double?
        get() = null
    val minHeight: Double?
        get() = null
    val spacing: DynamicSpacing?
        get() = null
}
