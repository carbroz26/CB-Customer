package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.carbroz.cbpartner.domain.model.DynamicDestination

class DynamicNavigationState(
    initialDestination: DynamicDestination,
) {
    internal val backStack: SnapshotStateList<DynamicNavigationEntry> =
        mutableStateListOf(DynamicNavigationEntry(initialDestination))

    var presentationEntry: DynamicNavigationEntry? by mutableStateOf(null)
        private set

    val currentEntry: DynamicNavigationEntry
        get() = backStack.last()

    val currentDestination: DynamicDestination
        get() = currentEntry.destination

    fun setRoot(destination: DynamicDestination) {
        presentationEntry = null
        backStack.clear()
        backStack.add(DynamicNavigationEntry(destination))
    }

    fun push(destination: DynamicDestination) {
        backStack.add(DynamicNavigationEntry(destination))
    }

    fun replace(destination: DynamicDestination) {
        if (backStack.isEmpty()) backStack.add(DynamicNavigationEntry(destination))
        else backStack[backStack.lastIndex] = DynamicNavigationEntry(destination)
    }

    fun present(destination: DynamicDestination) {
        check(presentationEntry == null) {
            "A Dynamic presentation is already active"
        }
        presentationEntry = DynamicNavigationEntry(destination)
    }

    fun dismissPresentation(): Boolean {
        if (presentationEntry == null) return false
        presentationEntry = null
        return true
    }

    fun pop(): Boolean {
        if (presentationEntry != null) return dismissPresentation()
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
