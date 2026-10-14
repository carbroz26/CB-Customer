package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

sealed interface DynamicActionResult {
    data object Completed : DynamicActionResult

    data class Navigate(
        val destination: DynamicDestination? = null,
        val operation: NavigationOperation = NavigationOperation.Push,
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult

    data class Present(
        val destination: DynamicDestination,
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult

    data class Dismiss(
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult

    data class ExternalUri(
        val uri: String,
        val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult

    data class StateChanged(
        val changes: Map<String, DynamicValue>,
    ) : DynamicActionResult

    /**
     * Result of a sequence. State changes are reduced once, while effects retain
     * execution order and are delivered independently by the Store.
     */
    data class Sequence(
        val stateChanges: Map<String, DynamicValue>,
        val effects: List<DynamicActionEffect>,
    ) : DynamicActionResult

    enum class NavigationOperation {
        Push,
        Replace,
        Pop,
    }
}

sealed interface DynamicActionEffect {
    data class Navigate(
        val destination: DynamicDestination?,
        val operation: DynamicActionResult.NavigationOperation,
    ) : DynamicActionEffect

    data class Present(val destination: DynamicDestination) : DynamicActionEffect

    data object Dismiss : DynamicActionEffect

    data class ExternalUri(val uri: String) : DynamicActionEffect
}
