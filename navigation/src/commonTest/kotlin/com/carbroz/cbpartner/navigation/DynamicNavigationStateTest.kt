package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicNavigationStateTest {
    @Test
    fun pushCreatesIndependentEntriesEvenForSameDestination() {
        val destination = destination("same")
        val state = DynamicNavigationState(destination)
        state.currentEntry.bindings = mapOf("value" to DynamicValue.StringValue("first"))
        state.push(destination)
        state.currentEntry.bindings = mapOf("value" to DynamicValue.StringValue("second"))

        assertEquals(2, state.backStack.size)
        assertEquals(
            DynamicValue.StringValue("second"),
            state.currentEntry.bindings["value"],
        )
        assertTrue(state.pop())
        assertEquals(
            DynamicValue.StringValue("first"),
            state.currentEntry.bindings["value"],
        )
    }

    @Test
    fun popRestoresPreviousEntryWithoutReplacingItsState() {
        val state = DynamicNavigationState(destination("root"))
        state.currentEntry.response = DynamicResponseFixture.response("root")
        state.push(destination("second"))

        assertTrue(state.pop())
        assertEquals("root", state.currentEntry.response?.dataPayload?.let { "root" })
        assertFalse(state.pop())
    }

    private fun destination(id: String) = DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}

private object DynamicResponseFixture {
    fun response(id: String) = com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse(
        200, "SUCCESS", "Loaded", null, id,
    )
}
