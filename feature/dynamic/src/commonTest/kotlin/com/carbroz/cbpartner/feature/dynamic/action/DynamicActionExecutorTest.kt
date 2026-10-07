package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DynamicActionExecutorTest {
    @Test
    fun sequenceUsesSameExecutorForNestedActions() = runTest {
        val executor = DynamicActionExecutor(
            listOf(object : DynamicActionHandler {
                override val type = "state"
                override suspend fun handle(
                    action: DynamicAction,
                    bindings: Map<String, DynamicValue>,
                    context: Map<String, DynamicValue>,
                    state: Map<String, DynamicValue>,
                ): DynamicActionResult = DynamicActionResult.StateChanged(
                    mapOf(
                        (action.payload["key"] as DynamicValue.StringValue).value to
                            (action.payload["value"] ?: DynamicValue.NullValue),
                    ),
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
            bindings = emptyMap(),
            context = emptyMap(),
            state = emptyMap(),
        )
        val stateChanged = assertIs<DynamicActionResult.StateChanged>(result.getOrThrow())
        assertEquals(DynamicValue.StringValue("nested"), stateChanged.changes["step"])
    }

    @Test
    fun duplicateHandlersAreRejected() {
        val handler = object : DynamicActionHandler {
            override val type = "state"
            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ) = DynamicActionResult.Completed
        }
        assertFailsWith<IllegalStateException> {
            DynamicActionExecutor(listOf(handler, handler))
        }
    }
}


    @Test
    fun sequencePreservesStateWhenFinalActionCompletes() = runTest {
        val executor = DynamicActionExecutor(
            listOf(
                object : DynamicActionHandler {
                    override val type = "state"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ) = DynamicActionResult.StateChanged(
                        mapOf("reference" to DynamicValue.StringValue("retained")),
                    )
                },
                object : DynamicActionHandler {
                    override val type = "noop"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ) = DynamicActionResult.Completed
                },
            ),
        )

        val result = executor.execute(
            DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(
                        listOf(
                            DynamicValue.ObjectValue(
                                mapOf("type" to DynamicValue.StringValue("state")),
                            ),
                            DynamicValue.ObjectValue(
                                mapOf("type" to DynamicValue.StringValue("noop")),
                            ),
                        ),
                    ),
                ),
            ),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        ).getOrThrow()

        val changes = assertIs<DynamicActionResult.StateChanged>(result).changes
        assertEquals(DynamicValue.StringValue("retained"), changes["reference"])
    }

    @Test
    fun sequencePreservesStateWithExternalUri() = runTest {
        val executor = DynamicActionExecutor(
            listOf(
                object : DynamicActionHandler {
                    override val type = "state"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ) = DynamicActionResult.StateChanged(
                        mapOf("reference" to DynamicValue.StringValue("retained")),
                    )
                },
                object : DynamicActionHandler {
                    override val type = "external_uri"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ) = DynamicActionResult.ExternalUri("https://example.com")
                },
            ),
        )

        val result = executor.execute(
            DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(
                        listOf(
                            DynamicValue.ObjectValue(
                                mapOf("type" to DynamicValue.StringValue("state")),
                            ),
                            DynamicValue.ObjectValue(
                                mapOf("type" to DynamicValue.StringValue("external_uri")),
                            ),
                        ),
                    ),
                ),
            ),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        ).getOrThrow()

        val external = assertIs<DynamicActionResult.ExternalUri>(result)
        assertEquals(
            DynamicValue.StringValue("retained"),
            external.stateChanges["reference"],
        )
    }
