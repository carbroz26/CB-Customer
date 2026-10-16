package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NavigateActionHandlerTest {
    private val handler = NavigateActionHandler()
    private val destination = DynamicDestination("screen", "template", "form_template", "/screen")

    @Test
    fun missingModeDefaultsToPushForExistingNavigation() = runTest {
        val result = handler.handle(DynamicAction("navigate", destination = destination), emptyMap(), emptyMap(), emptyMap())
        assertEquals(DynamicActionResult.NavigationOperation.Push, result.operation)
    }

    @Test
    fun supportedExplicitModesAreMapped() = runTest {
        for ((mode, expected) in listOf(
            "push" to DynamicActionResult.NavigationOperation.Push,
            "replace" to DynamicActionResult.NavigationOperation.Replace,
            "pop" to DynamicActionResult.NavigationOperation.Pop,
            "back" to DynamicActionResult.NavigationOperation.Pop,
        )) {
            val action = DynamicAction("navigate", mapOf("mode" to DynamicValue.StringValue(mode)),
                if (expected == DynamicActionResult.NavigationOperation.Pop) null else destination)
            assertEquals(expected, handler.handle(action, emptyMap(), emptyMap(), emptyMap()).operation)
        }
    }

    @Test
    fun explicitInvalidModesAreRejected() = runTest {
        for (mode in listOf(
            DynamicValue.StringValue("unknown"),
            DynamicValue.NullValue,
            DynamicValue.BooleanValue(true),
        )) {
            assertFailsWith<IllegalArgumentException> {
                handler.handle(DynamicAction("navigate", mapOf("mode" to mode), destination),
                    emptyMap(), emptyMap(), emptyMap())
            }
        }
    }

    @Test
    fun pushAndReplaceRequireDestination() = runTest {
        for (mode in listOf("push", "replace")) {
            assertFailsWith<IllegalArgumentException> {
                handler.handle(DynamicAction("navigate", mapOf("mode" to DynamicValue.StringValue(mode))),
                    emptyMap(), emptyMap(), emptyMap())
            }
        }
    }
}
