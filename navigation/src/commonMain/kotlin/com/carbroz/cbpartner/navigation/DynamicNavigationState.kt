package com.carbroz.cbpartner.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination

class DynamicNavigationState(
    initialDestination: DynamicDestination,
) {
    val backStack: SnapshotStateList<DynamicDestination> =
        mutableStateListOf(initialDestination)

    fun setRoot(destination: DynamicDestination) {
        backStack.clear()
        backStack.add(destination)
    }

    fun push(destination: DynamicDestination) {
        backStack.add(destination)
    }

    fun replace(destination: DynamicDestination) {
        if (backStack.isEmpty()) {
            backStack.add(destination)
        } else {
            backStack[backStack.lastIndex] = destination
        }
    }

    fun pop(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}
