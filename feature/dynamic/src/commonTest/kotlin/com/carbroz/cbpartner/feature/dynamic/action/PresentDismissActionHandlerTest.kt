package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PresentDismissActionHandlerTest {
    private val destination =
        DynamicDestination("dialog_screen", "dialog_template", "form_template", "/dialog")

    @Test
    fun presentReturnsLocalPresentationResultWithoutEffects() = runTest {
        val result = PresentActionHandler().handle(
            DynamicAction(
                "present",
                payload = mapOf("presentationId" to DynamicValue.StringValue("country_picker")),
            ),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )

        assertEquals("country_picker", result.presentationId)
        assertTrue(result.effects.isEmpty())
        assertTrue(result.stateChanges.isEmpty())
    }

    @Test
    fun presentRejectsMissingBlankExtraPayloadAndDestination() = runTest {
        val invalid = listOf(
            DynamicAction("present"),
            DynamicAction(
                "present",
                payload = mapOf("presentationId" to DynamicValue.StringValue("   ")),
            ),
            DynamicAction(
                "present",
                payload = mapOf(
                    "presentationId" to DynamicValue.StringValue("picker"),
                    "presentation" to DynamicValue.StringValue("dialog"),
                ),
            ),
            DynamicAction(
                "present",
                payload = mapOf("presentationId" to DynamicValue.StringValue("picker")),
                destination = destination,
            ),
        )

        invalid.forEach { action ->
            assertFailsWith<IllegalArgumentException> {
                PresentActionHandler().handle(action, emptyMap(), emptyMap(), emptyMap())
            }
        }
    }

    @Test
    fun dismissReturnsLocalResultWithoutEffects() = runTest {
        val result = DismissActionHandler().handle(
            DynamicAction("dismiss"),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )

        assertIs<DynamicActionResult.Dismiss>(result)
        assertTrue(result.effects.isEmpty())
        assertTrue(result.stateChanges.isEmpty())
    }

    @Test
    fun dismissRejectsPayloadAndDestination() = runTest {
        assertFailsWith<IllegalArgumentException> {
            DismissActionHandler().handle(
                DynamicAction(
                    "dismiss",
                    payload = mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            DismissActionHandler().handle(
                DynamicAction("dismiss", destination = destination),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
        }
    }
}
