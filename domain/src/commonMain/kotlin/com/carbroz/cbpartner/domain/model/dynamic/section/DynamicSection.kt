package com.carbroz.cbpartner.domain.model.dynamic.section

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup

/**
 * Common domain contract for dynamic UI sections.
 *
 * Concrete section types expose their child groups through a shared
 * domain contract while supporting directly owned elements.
 */
interface DynamicSection {
    val elements: List<DynamicElement>
    val groups: List<DynamicGroup>
    val weight: Double?
        get() = null
}
