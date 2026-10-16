package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

/**
 * Identity of one item in the Dynamic navigation back stack.
 *
 * Input bindings survive temporary NavEntry composition disposal while this entry remains in the stack.
 */
class DynamicNavigationEntry(
    val destination: DynamicDestination,
    val entryId: Long = 0L,
    val diagnosticSessionId: String? = null,
    val parentActionId: String? = null,
) {
    var bindings: Map<String, DynamicValue> = emptyMap()
        private set

    fun updateBindings(values: Map<String, DynamicValue>) {
        bindings = values
    }
}
