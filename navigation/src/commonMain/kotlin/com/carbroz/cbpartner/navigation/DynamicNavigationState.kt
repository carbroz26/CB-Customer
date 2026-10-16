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
    private var nextEntryId = 0L

    private fun newEntry(destination: DynamicDestination): DynamicNavigationEntry =
        DynamicNavigationEntry(destination, ++nextEntryId)

    internal val backStack: SnapshotStateList<DynamicNavigationEntry> =
        mutableStateListOf(newEntry(initialDestination))

    var presentationEntry: DynamicNavigationEntry? by mutableStateOf(null)
        private set

    val currentEntry: DynamicNavigationEntry
        get() = backStack.last()

    val currentDestination: DynamicDestination
        get() = currentEntry.destination

    fun setRoot(destination: DynamicDestination) {
        presentationEntry = null
        backStack.clear()
        backStack.add(newEntry(destination))
    }

    fun push(destination: DynamicDestination) {
        backStack.add(newEntry(destination))
    }

    fun replace(destination: DynamicDestination) {
        if (backStack.isEmpty()) backStack.add(newEntry(destination))
        else backStack[backStack.lastIndex] = newEntry(destination)
    }

    fun present(destination: DynamicDestination): Boolean {
        if (presentationEntry != null) return false
        presentationEntry = newEntry(destination)
        return true
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
