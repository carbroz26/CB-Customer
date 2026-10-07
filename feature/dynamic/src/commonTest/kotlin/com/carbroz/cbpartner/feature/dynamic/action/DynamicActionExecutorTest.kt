package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DynamicActionExecutorTest {
    @Test
    fun sequenceUsesSameExecutorForNestedActions() = runTest {
        val navigation = DynamicNavigationState(destination("current"))
        val executor = DynamicActionExecutor(
            listOf(object : DynamicActionHandler {
                override val type = "state"
                override suspend fun handle(
                    action: DynamicAction,
                    navigation: DynamicNavigationState,
                    bindings: Map<String, DynamicValue>,
                    context: Map<String, DynamicValue>,
                    state: Map<String, DynamicValue>,
                ): DynamicActionResult = DynamicActionResult.StateChanged(
                    key = (action.payload["key"] as DynamicValue.StringValue).value,
                    value = action.payload["value"] ?: DynamicValue.NullValue,
                )
            }),
        )

        val result = executor.execute(
            action = DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(
                        listOf(
                            DynamicValue.ObjectValue(
                                mapOf(
                                    "type" to DynamicValue.StringValue("state"),
                                    "payload" to DynamicValue.ObjectValue(
                                        mapOf(
                                            "key" to DynamicValue.StringValue("step"),
                                            "value" to DynamicValue.StringValue("nested"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            navigation = navigation,
            bindings = emptyMap(),
            context = emptyMap(),
            state = emptyMap(),
        )
        val stateChanged = assertIs<DynamicActionResult.StateChanged>(result.getOrThrow())
        assertEquals("step", stateChanged.key)
        assertEquals(DynamicValue.StringValue("nested"), stateChanged.value)
    }

    @Test
    fun duplicateHandlersAreRejected() {
        val handler = object : DynamicActionHandler {
            override val type = "state"
            override suspend fun handle(
                action: DynamicAction,
                navigation: DynamicNavigationState,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ) = DynamicActionResult.Completed
        }
        assertFailsWith<IllegalStateException> {
            DynamicActionExecutor(listOf(handler, handler))
        }
    }

    private fun destination(id: String) = DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}
