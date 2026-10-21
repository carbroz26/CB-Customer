package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

@Composable
fun DynamicNavigationHost(
    state: DynamicNavigationState,
    content: @Composable (DynamicNavigationEntry) -> Unit,
) {
    NavDisplay(
        backStack = state.backStack,
        onBack = { state.pop() },
        entryProvider = { entry ->
            NavEntry(entry) {
                content(entry)
            }
        },
    )
}
