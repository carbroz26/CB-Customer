package com.carbroz.cbpartner.domain.model.dynamic.template

import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent

/**
 * Common domain contract for dynamic UI templates.
 *
 * Concrete template types define their own properties while sharing
 * a common contract for rendering and parent-child composition.
 */
interface DynamicTemplate {
    val components: List<DynamicComponent>
}