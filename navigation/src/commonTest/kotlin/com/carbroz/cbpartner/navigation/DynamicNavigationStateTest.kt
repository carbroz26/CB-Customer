package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DynamicNavigationStateTest {
    @Test
    fun pushCreatesIndependentEntriesEvenForSameDestination() {
        val destination = destination("same")
        val state = DynamicNavigationState(destination)
        val first = state.currentEntry

        state.push(destination)
        val second = state.currentEntry

        assertTrue(first !== second)
        assertTrue(state.backStack.size == 2)
        assertFalse(first === second)
    }

    @Test
    fun popRestoresThePreviousNavigationEntryIdentity() {
        val state = DynamicNavigationState(destination("root"))
        val first = state.currentEntry
        state.push(destination("second"))

        assertTrue(state.pop())
        assertSame(first, state.currentEntry)
        assertFalse(state.pop())
    }

    @Test
    fun replaceReplacesOnlyTheCurrentEntry() {
        val state = DynamicNavigationState(destination("root"))
        val first = state.currentEntry

        state.push(destination("second"))
        state.replace(destination("replacement"))

        assertSame(first, state.backStack.first())
        assertEquals("replacement", state.currentDestination.screenId)
        assertTrue(state.backStack.size == 2)
    }

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}
