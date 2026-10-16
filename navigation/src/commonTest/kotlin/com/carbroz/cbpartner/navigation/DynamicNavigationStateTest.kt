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
    fun replaceAndPresentationStoreParentWithoutChangingUnderlyingEntry() {
        val state = DynamicNavigationState(destination("root"), "session-id")
        val root = state.currentEntry
        state.push(destination("child"), "first-action")
        state.replace(destination("replacement"), "replace-action")
        assertEquals("replace-action", state.currentEntry.parentActionId)
        assertSame(root, state.backStack.first())

        assertTrue(state.present(destination("modal"), "modal-action"))
        assertEquals("modal-action", state.presentationEntry?.parentActionId)
        assertEquals("session-id", state.presentationEntry?.diagnosticSessionId)
        assertFalse(state.present(destination("other"), "unused-action"))
        assertEquals("modal-action", state.presentationEntry?.parentActionId)
        assertTrue(state.dismissPresentation())
        assertEquals("replace-action", state.currentEntry.parentActionId)
    }


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
        assertTrue(state.backStack.size == 2)
    }

    @Test
    fun presentationIsDistinctAndDismissRestoresUnderlyingEntry() {
        val state = DynamicNavigationState(destination("root"))
        val underlying = state.currentEntry

        state.present(destination("modal"))
        val presentation = state.presentationEntry

        assertTrue(presentation != null)
        assertTrue(presentation !== underlying)
        assertSame(underlying, state.currentEntry)
        assertTrue(state.dismissPresentation())
        assertEquals(null, state.presentationEntry)
        assertSame(underlying, state.currentEntry)
    }

    @Test
    fun secondActivePresentationIsRejected() {
        val state = DynamicNavigationState(destination("root"))
        state.present(destination("firstModal"))

        assertFalse(state.present(destination("secondModal")))
        assertEquals("firstModal", state.presentationEntry?.destination?.screenId)
    }

    @Test
    fun backDismissesPresentationBeforePoppingNavigationStack() {
        val state = DynamicNavigationState(destination("root"))
        state.push(destination("next"))
        val current = state.currentEntry
        state.present(destination("modal"))

        assertTrue(state.pop())
        assertEquals(null, state.presentationEntry)
        assertSame(current, state.currentEntry)
        assertTrue(state.pop())
        assertEquals("root", state.currentDestination.screenId)
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


    @Test
    fun dismissPresentationWithoutModalDoesNotPopNonRootStack() {
        val state = DynamicNavigationState(destination("root"))
        state.push(destination("second"))
        val current = state.currentEntry

        assertFalse(state.dismissPresentation())
        assertSame(current, state.currentEntry)
        assertEquals(2, state.backStack.size)
    }

    @Test
    fun dismissPresentationWithoutModalDoesNotPopRoot() {
        val state = DynamicNavigationState(destination("root"))
        val root = state.currentEntry

        assertFalse(state.dismissPresentation())
        assertSame(root, state.currentEntry)
        assertEquals(1, state.backStack.size)
    }

    @Test
    fun dismissPresentationWithModalKeepsUnderlyingStack() {
        val state = DynamicNavigationState(destination("root"))
        state.push(destination("second"))
        val current = state.currentEntry
        assertTrue(state.present(destination("modal")))

        assertTrue(state.dismissPresentation())
        assertEquals(null, state.presentationEntry)
        assertSame(current, state.currentEntry)
        assertEquals(2, state.backStack.size)
    }

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}
