package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination

@Composable
fun DynamicNavigationHost(
    state: DynamicNavigationState,
    content: @Composable (DynamicDestination) -> Unit,
) {
    NavDisplay(
        backStack = state.backStack,
        onBack = { state.pop() },
        entryProvider = { destination ->
            NavEntry(destination) {
                content(destination)
            }
        },
    )
}
