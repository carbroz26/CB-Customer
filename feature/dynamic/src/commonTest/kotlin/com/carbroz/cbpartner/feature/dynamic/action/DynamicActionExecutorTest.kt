package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DynamicActionExecutorTest {
    @Test
    fun requestHandlerReceivesExactExecutionTrace() = runTest {
        var observed: com.carbroz.cbpartner.core.diagnostics.TraceContext? = null
        val handler = object : DynamicActionHandler {
            override val type = "request"
            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ): DynamicActionResult = error("Legacy handler path should not run")

            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
                traceContext: com.carbroz.cbpartner.core.diagnostics.TraceContext?,
            ): DynamicActionResult {
                observed = traceContext
                return DynamicActionResult.Completed
            }
        }
        val trace = com.carbroz.cbpartner.core.diagnostics.TraceContext("session", "action", "parent")
        DynamicActionExecutor(listOf(handler))
            .execute(DynamicAction("request"), emptyMap(), emptyMap(), emptyMap(), trace)
            .getOrThrow()
        assertEquals(trace, observed)
    }


    @Test
    fun sequenceIsInertEvenWhenLegacyPayloadContainsActions() = runTest {
        var invoked = false
        val other = object : DynamicActionHandler {
            override val type = "external_uri"
            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ): DynamicActionResult {
                invoked = true
                return DynamicActionResult.ExternalUri("https://example.com")
            }
        }
        val executor = DynamicActionExecutor(listOf(SequenceActionHandler(), other))
        val sequence = DynamicAction(
            type = "sequence",
            payload = mapOf(
                "actions" to DynamicValue.ArrayValue(
                    listOf(DynamicValue.ObjectValue(mapOf("type" to DynamicValue.StringValue("external_uri")))),
                ),
            ),
        )
        assertIs<DynamicActionResult.Completed>(
            executor.execute(sequence, emptyMap(), emptyMap(), emptyMap()).getOrThrow(),
        )
        assertEquals(false, invoked)
    }

    @Test
    fun ordinaryActionDispatchesToItsOwnHandler() = runTest {
        val handler = object : DynamicActionHandler {
            override val type = "state"
            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ): DynamicActionResult = DynamicActionResult.StateChanged(
                mapOf("ready" to DynamicValue.BooleanValue(true)),
            )
        }
        val result = DynamicActionExecutor(listOf(handler))
            .execute(DynamicAction("state"), emptyMap(), emptyMap(), emptyMap())
            .getOrThrow()
        assertEquals(
            DynamicValue.BooleanValue(true),
            assertIs<DynamicActionResult.StateChanged>(result).changes["ready"],
        )
    }

    @Test
    fun duplicateHandlersAreRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicActionExecutor(listOf(SequenceActionHandler(), SequenceActionHandler()))
        }
    }

    @Test
    fun unknownActionReturnsFailure() = runTest {
        val result = DynamicActionExecutor(listOf(SequenceActionHandler()))
            .execute(DynamicAction("unknown"), emptyMap(), emptyMap(), emptyMap())
        assertTrue(result.isFailure)
    }

    @Test
    fun cancellationPropagates() = runTest {
        val handler = object : DynamicActionHandler {
            override val type = "cancel"
            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ): DynamicActionResult = throw CancellationException("cancelled")
        }
        assertFailsWith<CancellationException> {
            DynamicActionExecutor(listOf(handler))
                .execute(DynamicAction("cancel"), emptyMap(), emptyMap(), emptyMap())
        }
    }

    @Test
    fun topLevelBackIsUnsupportedAndIsNotAliasedToDismiss() = runTest {
        val result = DynamicActionExecutor(listOf(DismissActionHandler()))
            .execute(DynamicAction("back"), emptyMap(), emptyMap(), emptyMap())

        assertTrue(result.isFailure)
    }

}
