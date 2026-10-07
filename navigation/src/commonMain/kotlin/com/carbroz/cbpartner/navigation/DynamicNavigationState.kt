package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.carbroz.cbpartner.domain.model.DynamicDestination

class DynamicNavigationState(
    initialDestination: DynamicDestination,
) {
    internal val backStack: SnapshotStateList<DynamicNavigationEntry> =
        mutableStateListOf(DynamicNavigationEntry(initialDestination))

    val currentEntry: DynamicNavigationEntry
        get() = backStack.last()

    val currentDestination: DynamicDestination
        get() = currentEntry.destination

    fun setRoot(destination: DynamicDestination) {
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

    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
