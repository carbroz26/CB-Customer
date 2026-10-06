package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicActionExecutorTest {
    @Test
    fun sequenceUsesTheSameExecutorForNestedActions() = kotlinx.coroutines.test.runTest {
        val destination = DynamicDestination(
            screenId = "current",
            templateId = "template",
            templateType = "stack_template",
            endpoint = "/current",
        )
        val navigation = DynamicNavigationState(destination)
        val executor = DynamicActionExecutor(
            handlers = listOf(
                object : DynamicActionHandler {
                    override val type: String = "state"

                    override suspend fun handle(
                        action: DynamicAction,
                        navigation: DynamicNavigationState,
                        bindings: Map<String, String>,
                        context: Map<String, String>,
                        previousResponse: DynamicValue?,
                    ): DynamicActionResult =
                        DynamicActionResult.StateChanged(
                            key = (action.payload["key"] as DynamicValue.StringValue).value,
                            value = (action.payload["value"] as DynamicValue.StringValue).value,
                        )
                },
            ),
        )

        val result = executor.execute(
            action = DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(
                        listOf(
                            DynamicValue.ObjectValue(
                                mapOf(
                                    "type" to DynamicValue.StringValue("sequence"),
                                    "payload" to DynamicValue.ObjectValue(
                                        mapOf(
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
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            navigation = navigation,
            bindings = emptyMap(),
            context = emptyMap(),
            previousResponse = null,
        ).getOrThrow()

        val stateChanged = assertIs<DynamicActionResult.StateChanged>(result)
        assertEquals("step", stateChanged.key)
        assertEquals("nested", stateChanged.value)
    }
}
