package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

sealed interface DynamicActionResult {
    data object Completed : DynamicActionResult

    data class Navigate(
        val destination: DynamicDestination? = null,
        val operation: NavigationOperation = NavigationOperation.Replace,
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult

    data class ExternalUri(
        val uri: String,
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult

    data class StateChanged(
        val changes: Map<String, DynamicValue>,
    ) : DynamicActionResult

    enum class NavigationOperation {
        Push,
        Replace,
        Pop,
    }
}
