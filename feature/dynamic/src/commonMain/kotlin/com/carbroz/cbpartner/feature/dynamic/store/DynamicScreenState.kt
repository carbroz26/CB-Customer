package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Mutable state scoped to one Dynamic navigation entry.
 *
 * Runtime-wide retained values are intentionally not stored here; they live in
 * DynamicRuntimeState and are shared across Dynamic entries.
 */
internal class DynamicScreenState {
    var response: DynamicResponse? = null
    var bindings: Map<String, DynamicValue> = emptyMap()
    var validationMessage: String? = null
}
