
package com.carbroz.cbpartner.domain.model.dynamic.section

import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement


/**
 * Common domain contract for dynamic UI components.
 *
 * Concrete component types define their own properties while exposing
 * their child sections through a shared domain contract.
 */
interface DynamicSection {
    val elements: List<DynamicElement>
    val groups: List<DynamicGroup>
}
