package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
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
                object : DynamicActionHandler {
                    override val type = "after"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ) = DynamicActionResult.StateChanged(
                        mapOf("afterEffect" to DynamicValue.BooleanValue(true)),
                    )
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
                            DynamicValue.ObjectValue(
                                mapOf("type" to DynamicValue.StringValue("after")),
                            ),
                        ),
                    ),
                ),
            ),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        ).getOrThrow()

        val sequence = assertIs<DynamicActionResult.Sequence>(result)
        assertEquals(
            DynamicValue.StringValue("retained"),
            sequence.stateChanges["reference"],
        )
        assertEquals(DynamicValue.BooleanValue(true), sequence.stateChanges["afterEffect"])
        assertEquals(
            listOf(DynamicActionEffect.ExternalUri("https://example.com")),
            sequence.effects,
        )
    }

    @Test
    fun nestedSequencePreservesNavigationAndExternalEffectsInOrder() = runTest {
        val destination = DynamicDestination("screen", "template", "form_template", "/screen")
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
                        mapOf("nestedState" to DynamicValue.StringValue("retained")),
                    )
                },
                object : DynamicActionHandler {
                    override val type = "navigate"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ) = DynamicActionResult.Navigate(
                        destination,
                        DynamicActionResult.NavigationOperation.Push,
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

        val nested = DynamicValue.ObjectValue(
            mapOf(
                "type" to DynamicValue.StringValue("sequence"),
                "payload" to DynamicValue.ObjectValue(
                    mapOf(
                        "actions" to DynamicValue.ArrayValue(
                            listOf(
                                DynamicValue.ObjectValue(
                                    mapOf("type" to DynamicValue.StringValue("state")),
                                ),
                                DynamicValue.ObjectValue(
                                    mapOf("type" to DynamicValue.StringValue("external_uri")),
                                ),
                                DynamicValue.ObjectValue(
                                    mapOf("type" to DynamicValue.StringValue("noop")),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        val result = executor.execute(
            DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(
                        listOf(
                            DynamicValue.ObjectValue(
                                mapOf("type" to DynamicValue.StringValue("navigate")),
                            ),
                            nested,
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

        val sequence = assertIs<DynamicActionResult.Sequence>(result)
        assertEquals(
            DynamicValue.StringValue("retained"),
            sequence.stateChanges["nestedState"],
        )
        assertEquals(
            listOf(
                DynamicActionEffect.Navigate(
                    destination,
                    DynamicActionResult.NavigationOperation.Push,
                ),
                DynamicActionEffect.ExternalUri("https://example.com"),
            ),
            sequence.effects,
        )
    }

    @Test
    fun sequenceStateChangesSupportNestedStatePaths() = runTest {
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
                        mapOf("flow.reference.id" to DynamicValue.StringValue("ref-1")),
                    )
                },
                object : DynamicActionHandler {
                    override val type = "read"
                    override suspend fun handle(
                        action: DynamicAction,
                        bindings: Map<String, DynamicValue>,
                        context: Map<String, DynamicValue>,
                        state: Map<String, DynamicValue>,
                    ): DynamicActionResult {
                        return if (
                            state["flow"] ==
                            DynamicValue.ObjectValue(
                                mapOf(
                                    "reference" to DynamicValue.ObjectValue(
                                        mapOf("id" to DynamicValue.StringValue("ref-1")),
                                    ),
                                ),
                            )
                        ) {
                            DynamicActionResult.Completed
                        } else {
                            error("Nested sequence state was not materialized")
                        }
                    }
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
                                mapOf("type" to DynamicValue.StringValue("read")),
                            ),
                        ),
                    ),
                ),
            ),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )

        val stateChanged = assertIs<DynamicActionResult.StateChanged>(result.getOrThrow())
        assertEquals(
            DynamicValue.StringValue("ref-1"),
            stateChanged.changes["flow.reference.id"],
        )
    }

    @Test
    fun sequenceDepthIsBounded() = runTest {
        val nested = generateSequenceAction(33)
        val result = DynamicActionExecutor(emptyList()).execute(
            nested,
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )

        assertFailsWith<IllegalStateException> { result.getOrThrow() }
    }

    private fun generateSequenceAction(depth: Int): DynamicAction =
        if (depth == 0) {
            DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(emptyList()),
                ),
            )
        } else {
            DynamicAction(
                type = "sequence",
                payload = mapOf(
                    "actions" to DynamicValue.ArrayValue(
                        listOf(
                            DynamicValue.ObjectValue(
                                mapOf(
                                    "type" to DynamicValue.StringValue("sequence"),
                                    "payload" to DynamicValue.ObjectValue(
                                        generateSequenceAction(depth - 1).payload,
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
}
