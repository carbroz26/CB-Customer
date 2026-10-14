package com.carbroz.cbpartner.domain.model.dynamic.group

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement

/**
 * Common domain contract for dynamic groups.
 */
interface DynamicGroup {
    val elements: List<DynamicElement>
}
