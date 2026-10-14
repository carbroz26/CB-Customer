package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.window.Dialog

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

    state.presentationEntry?.let { presentation ->
        Dialog(onDismissRequest = { state.dismissPresentation() }) {
            content(presentation)
        }
    }
}
