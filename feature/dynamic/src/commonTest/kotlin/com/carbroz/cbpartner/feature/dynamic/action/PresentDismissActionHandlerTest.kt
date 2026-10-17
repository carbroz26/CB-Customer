package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class PresentDismissActionHandlerTest {
    private val destination = DynamicDestination("dialog_screen", "dialog_template", "form_template", "/dialog")

    @Test
    fun presentEmitsPresentationEffect() = runTest {
        val result = PresentActionHandler().handle(
            DynamicAction("present", destination = destination),
            emptyMap(), emptyMap(), emptyMap(),
        )
        assertEquals(listOf(DynamicActionEffect.Present(destination)), result.effects)
        assertEquals(emptyMap(), result.stateChanges)
    }

    @Test
    fun presentRequiresDestination() = runTest {
        assertFailsWith<IllegalStateException> {
            PresentActionHandler().handle(DynamicAction("present"), emptyMap(), emptyMap(), emptyMap())
        }
    }

    @Test
    fun dismissOnlyProducesDismissEffect() = runTest {
        val result = DismissActionHandler().handle(
            DynamicAction("dismiss"), emptyMap(), emptyMap(), emptyMap(),
        )
        assertIs<DynamicActionResult.Dismiss>(result)
        assertEquals(listOf(DynamicActionEffect.Dismiss), result.effects)
        assertEquals(emptyMap(), result.stateChanges)
    }
}
