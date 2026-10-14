
package com.carbroz.cbpartner.domain.model.dynamic.group

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement

/**
 * Common domain contract for dynamic UI components.
 *
 * Concrete component types define their own properties while exposing
 * their child sections through a shared domain contract.
 */
interface DynamicGroup {
    val elements: List<DynamicElement>
}