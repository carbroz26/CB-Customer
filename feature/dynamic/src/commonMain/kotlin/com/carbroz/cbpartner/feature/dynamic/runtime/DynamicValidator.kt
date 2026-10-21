package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Validates one Dynamic binding value using already-resolved validator arguments.
 *
 * Product-specific validators are registered outside the generic Dynamic runtime.
 */
interface DynamicValidator {
    val key: String

    fun isValid(
        value: DynamicValue,
        arguments: Map<String, DynamicValue>,
    ): Boolean
}
