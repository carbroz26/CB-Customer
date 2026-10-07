package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

sealed interface DynamicActionResult {
    data object Completed : DynamicActionResult
    data class Navigate(
        val destination: DynamicDestination,
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult
    data class ExternalUri(val uri: String) : DynamicActionResult
    data class StateChanged(val key: String, val value: DynamicValue) : DynamicActionResult
}
