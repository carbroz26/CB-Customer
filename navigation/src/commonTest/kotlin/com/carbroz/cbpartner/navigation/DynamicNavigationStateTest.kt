package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DynamicNavigationStateTest {
    @Test
    fun entrySnapshotIsDiscardedWithRemovedNavigationEntry() {
        val state = DynamicNavigationState(destination("root"))
        val first = state.currentEntry
        first.updateScreenResponse(
            com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse(
                200,
                "OK",
                "Loaded",
                null,
                null,
            ),
        )
        state.replace(destination("replacement"))
        assertTrue(first !== state.currentEntry)
        assertEquals(null, state.currentEntry.screenResponse)
        assertEquals(null, state.currentEntry.validationMessage)
    }

    @Test
    fun pushAndPopPreserveParentActionOnExistingEntries() {
        val state = DynamicNavigationState(destination("root"), "session-id")
        val root = state.currentEntry
        assertEquals("session-id", root.diagnosticSessionId)
        assertEquals(null, root.parentActionId)

        state.push(destination("next"), "action-one")
        assertEquals("action-one", state.currentEntry.parentActionId)
        assertEquals("session-id", state.currentEntry.diagnosticSessionId)

        state.push(destination("third"), "action-two")
        assertEquals("action-two", state.currentEntry.parentActionId)
        assertTrue(state.pop())
        assertEquals("action-one", state.currentEntry.parentActionId)
        assertTrue(state.pop())
        assertSame(root, state.currentEntry)
        assertEquals(null, root.parentActionId)
    }

    @Test
    fun replaceStoresParentWithoutChangingUnderlyingEntry() {
        val state = DynamicNavigationState(destination("root"), "session-id")
        val root = state.currentEntry
        state.push(destination("child"), "first-action")
        state.replace(destination("replacement"), "replace-action")

        assertEquals("replace-action", state.currentEntry.parentActionId)
        assertSame(root, state.backStack.first())
    }

    @Test
    fun pushCreatesIndependentEntriesEvenForSameDestination() {
        val destination = destination("same")
        val state = DynamicNavigationState(destination)
        val first = state.currentEntry

        state.push(destination)
        val second = state.currentEntry

        assertTrue(first !== second)
        assertEquals(2, state.backStack.size)
        assertFalse(first === second)
        assertTrue(first.entryId != second.entryId)
    }

    @Test
    fun popRestoresThePreviousNavigationEntryIdentity() {
        val state = DynamicNavigationState(destination("root"))
        val first = state.currentEntry
        state.push(destination("second"))

        assertTrue(state.pop())
        assertSame(first, state.currentEntry)
        assertEquals(first.entryId, state.currentEntry.entryId)
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
        assertEquals(2, state.backStack.size)
    }

    @Test
    fun bindingsFollowEntryMembershipAcrossPushAndPop() {
        val state = DynamicNavigationState(destination("root"))
        val root = state.currentEntry
        root.updateBindings(mapOf("phone" to DynamicValue.StringValue("123")))

        state.push(destination("root"))
        val other = state.currentEntry
        assertTrue(other.bindings.isEmpty())
        other.updateBindings(mapOf("phone" to DynamicValue.StringValue("456")))

        assertTrue(state.pop())
        assertSame(root, state.currentEntry)
        assertEquals(DynamicValue.StringValue("123"), root.bindings["phone"])
        assertEquals(DynamicValue.StringValue("456"), other.bindings["phone"])
    }

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}
