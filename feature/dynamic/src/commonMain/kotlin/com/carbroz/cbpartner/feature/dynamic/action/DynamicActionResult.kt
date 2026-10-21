package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

sealed interface DynamicActionResult {
    val stateChanges: Map<String, DynamicValue>
    val effects: List<DynamicActionEffect>

    data object Completed : DynamicActionResult {
        override val stateChanges: Map<String, DynamicValue> = emptyMap()
        override val effects: List<DynamicActionEffect> = emptyList()
    }

    data class Navigate(
        val destination: DynamicDestination? = null,
        val operation: NavigationOperation = NavigationOperation.Push,
        override val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult {
        override val effects: List<DynamicActionEffect>
            get() = listOf(DynamicActionEffect.Navigate(destination, operation))
    }

    data class Present(
        val presentationId: String,
    ) : DynamicActionResult {
        override val stateChanges: Map<String, DynamicValue> = emptyMap()
        override val effects: List<DynamicActionEffect> = emptyList()
    }

    data object Dismiss : DynamicActionResult {
        override val stateChanges: Map<String, DynamicValue> = emptyMap()
        override val effects: List<DynamicActionEffect> = emptyList()
    }

    data class ExternalUri(
        val uri: String,
        override val stateChanges: Map<String, DynamicValue> = emptyMap(),
    ) : DynamicActionResult {
        override val effects: List<DynamicActionEffect>
            get() = listOf(DynamicActionEffect.ExternalUri(uri))
    }

    data class StateChanged(
        val changes: Map<String, DynamicValue>,
    ) : DynamicActionResult {
        override val stateChanges: Map<String, DynamicValue>
            get() = changes
        override val effects: List<DynamicActionEffect> = emptyList()
    }

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

    data class ExternalUri(val uri: String) : DynamicActionEffect
}
