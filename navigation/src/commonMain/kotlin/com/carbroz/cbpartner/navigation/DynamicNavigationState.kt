package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.carbroz.cbpartner.domain.model.DynamicDestination

class DynamicNavigationState(
    initialDestination: DynamicDestination,
    private val diagnosticSessionId: String? = null,
) {
    private var nextEntryId = 0L

    private fun newEntry(destination: DynamicDestination, parentActionId: String? = null): DynamicNavigationEntry =
        DynamicNavigationEntry(destination, ++nextEntryId, diagnosticSessionId, parentActionId)

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

    fun push(destination: DynamicDestination, parentActionId: String? = null) {
        backStack.add(newEntry(destination, parentActionId))
    }

    fun replace(destination: DynamicDestination, parentActionId: String? = null) {
        if (backStack.isEmpty()) backStack.add(newEntry(destination, parentActionId))
        else backStack[backStack.lastIndex] = newEntry(destination, parentActionId)
    }

    fun present(destination: DynamicDestination, parentActionId: String? = null): Boolean {
        if (presentationEntry != null) return false
        presentationEntry = newEntry(destination, parentActionId)
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
