package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicNavigationStateTest {
    private val root = destination("root")
    private val second = destination("second")
    private val replacement = destination("replacement")

    @Test
    fun setRoot_replaces_entire_back_stack() {
        val state = DynamicNavigationState(root)
        state.push(second)

        state.setRoot(replacement)

        assertEquals(listOf(replacement), state.backStack.toList())
    }

    @Test
    fun push_preserves_complete_destination_history() {
        val state = DynamicNavigationState(root)

        state.push(second)

        assertEquals(listOf(root, second), state.backStack.toList())
    }

    @Test
    fun replace_changes_only_current_destination() {
        val state = DynamicNavigationState(root)
        state.push(second)

        state.replace(replacement)

        assertEquals(listOf(root, replacement), state.backStack.toList())
    }

    @Test
    fun pop_restores_previous_destination() {
        val state = DynamicNavigationState(root)
        state.push(second)

        assertTrue(state.pop())
        assertEquals(listOf(root), state.backStack.toList())
        assertFalse(state.pop())
    }

    private fun destination(screenId: String) = DynamicDestination(
        screenId = screenId,
        templateId = "tpl_P900${screenId.take(1).uppercase()}",
        templateType = "stack_template",
        endpoint = "/api/v1/partner/screen/$screenId",
    )
}
