package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.RequestActionHandler
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runCurrent
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicStoreTest {

    @Test
    fun actionAndNavigationEffectShareTraceAndScreenLoadRetainsParent() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val sessionId = "session-from-bootstrap"
        val parentId = "previous-action"
        val source = DynamicNavigationEntry(destination("source"), 1L, sessionId, parentId)
        val target = destination("target")
        var submitted: DynamicRequest? = null
        var deliveredTrace: com.carbroz.cbpartner.core.diagnostics.TraceContext? = null
        var deliveredEffects: List<DynamicActionEffect>? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/submit") {
                    submitted = request
                    return Result.success(
                        DynamicResponse(200, "OK", "Done", null, null, destination = target),
                    )
                }
                return Result.success(DynamicResponse(200, "OK", "Loaded", null, null))
            }
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = source,
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
            deliverCorrelatedEffects = { trace, effects ->
                deliveredTrace = trace
                deliveredEffects = effects
            },
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "request",
                    mapOf(
                        "endpoint" to DynamicValue.StringValue("/submit"),
                        "responseMode" to DynamicValue.StringValue("destination"),
                    ),
                ),
            ),
        )
        advanceUntilIdle()
        val request = requireNotNull(submitted)
        val trace = requireNotNull(deliveredTrace)
        assertEquals(sessionId, trace.sessionId)
        assertEquals(parentId, trace.parentActionId)
        assertEquals(trace.actionId, request.actionId)
        assertEquals(sessionId, request.sessionId)
        assertEquals(parentId, request.parentActionId)
        assertIs<DynamicActionEffect.Navigate>(requireNotNull(deliveredEffects).single())
        store.close()
    }

    @Test
    fun typedBindingIsUsedByRequest() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var body: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                body = request.body
                return Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, "request"))
            }
        }
        val store = store(
            DynamicNavigationEntry(destination("current")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.ValueChanged("count", DynamicValue.NumberValue(2.0)))
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/submit"),
                        "responseMode" to DynamicValue.StringValue("none"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf("count" to ref("\$binding", "count")),
                        ),
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        val requestBody = assertIs<DynamicValue.ObjectValue>(body)
        assertEquals(DynamicValue.NumberValue(2.0), requestBody.value["count"])
        store.close()
    }

    @Test
    fun explicitResponseRetentionBecomesRuntimeState() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val next = destination("next")
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(
                    DynamicResponse(
                        200,
                        "SUCCESS",
                        "OK",
                        null,
                        "response",
                        dataPayload = DynamicValue.ObjectValue(
                            mapOf("referenceId" to DynamicValue.StringValue("ref-1")),
                        ),
                        destination = next,
                    ),
                )
        }
        val runtimeState = DynamicRuntimeState()
        val store = store(
            DynamicNavigationEntry(destination("current")),
            repository,
            runtimeState,
            dispatcher,
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()

        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/next"),
                        "responseMode" to DynamicValue.StringValue("destination"),
                        "retain" to DynamicValue.ObjectValue(
                            mapOf("referenceId" to DynamicValue.StringValue("data.referenceId")),
                        ),
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        assertEquals(
            DynamicValue.StringValue("ref-1"),
            runtimeState.values.value["referenceId"],
        )
        store.close()
    }

    @Test
    fun recreatedStoreRestoresBindingsFromSameNavigationEntry() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "test"))
        }
        val entry = DynamicNavigationEntry(destination("same"))
        val runtimeState = DynamicRuntimeState()
        val first = store(entry, repository, runtimeState, dispatcher)
        first.accept(DynamicIntent.Load)
        advanceUntilIdle()
        first.accept(DynamicIntent.ValueChanged("phone", DynamicValue.StringValue("987654")))
        first.close()

        val recreated = store(entry, repository, runtimeState, dispatcher)
        recreated.accept(DynamicIntent.Load)
        advanceUntilIdle()
        assertEquals(
            DynamicValue.StringValue("987654"),
            assertIs<DynamicState.Success>(recreated.state.value).screen.bindings["phone"],
        )
        recreated.close()
    }

    @Test
    fun screenStateIsIndependentPerEntry() = runTest {
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "test"))
        }
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val first = store(
            DynamicNavigationEntry(destination("first")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )
        val second = store(
            DynamicNavigationEntry(destination("second")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )

        first.accept(DynamicIntent.Load)
        second.accept(DynamicIntent.Load)
        advanceUntilIdle()
        first.accept(DynamicIntent.ValueChanged("firstValue", DynamicValue.StringValue("one")))
        second.accept(DynamicIntent.ValueChanged("secondValue", DynamicValue.StringValue("two")))

        assertEquals(
            DynamicValue.StringValue("one"),
            assertIs<DynamicState.Success>(first.state.value).screen.bindings["firstValue"],
        )
        assertEquals(
            DynamicValue.StringValue("two"),
            assertIs<DynamicState.Success>(second.state.value).screen.bindings["secondValue"],
        )

        first.close()
        second.close()
    }


    @Test
    fun resolvedTrueValidationWithoutLoadedFormPreventsRequest() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var requestCalls = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/submit") requestCalls++
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "test"))
            }
        }
        val store = store(
            DynamicNavigationEntry(destination("current")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "responseMode" to DynamicValue.StringValue("none"),
                "validate" to ref("\$context", "mustValidate"),
            ),
        )))
        advanceUntilIdle()
        assertEquals(0, requestCalls)
        store.close()
    }

    @Test
    fun failedRequestPreservesLoadedFormStateAndBindings() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/submit") Result.failure(IllegalStateException("private-server-detail"))
                else Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "test"))
        }
        val store = store(
            DynamicNavigationEntry(destination("current")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.ValueChanged("phone", DynamicValue.StringValue("123456")))
        val loadedResponse = assertIs<DynamicState.Success>(store.state.value).screen.response
        store.accept(DynamicIntent.Action(DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "responseMode" to DynamicValue.StringValue("none"),
            ),
        )))
        advanceUntilIdle()
        val current = assertIs<DynamicState.Success>(store.state.value)
        assertEquals(DynamicValue.StringValue("123456"), current.screen.bindings["phone"])
        assertEquals(loadedResponse, current.screen.response)
        assertEquals("Unable to complete action", current.screen.validationMessage)
        store.close()
    }

    @Test
    fun resolvedTrueValidationWithoutDocumentRejectsBeforeRequest() = runTest {
        var submitCalls = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/submit") submitCalls++
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, null))
            }
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("current")),
            runtimeState = DynamicRuntimeState(),
            contextProvider = { mapOf("mustValidate" to DynamicValue.BooleanValue(true)) },
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction(
            "request",
            mapOf("endpoint" to DynamicValue.StringValue("/submit"),
                "validate" to ref("\$context", "mustValidate")),
        )))
        advanceUntilIdle()
        assertEquals(0, submitCalls)
        assertNotNull(assertIs<DynamicState.Success>(store.state.value).screen.validationMessage)
        store.close()
    }

    @Test
    fun retainedStateIsCommittedBeforeRequestNavigationEffect() = runTest {
        val target = destination("next")
        val state = DynamicRuntimeState()
        var observed: DynamicValue? = null
        var delivered: List<DynamicActionEffect>? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(200, "SUCCESS", "OK", null, null,
                    dataPayload = DynamicValue.ObjectValue(
                        mapOf("reference" to DynamicValue.StringValue("ref-2"))),
                    destination = target))
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("current")),
            runtimeState = state,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            deliverEffects = { effects ->
                observed = state.values.value["reference"]
                delivered = effects
            },
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("destination"),
            "retain" to DynamicValue.ObjectValue(mapOf(
                "reference" to DynamicValue.StringValue("data.reference"))),
        ))))
        advanceUntilIdle()
        assertEquals(DynamicValue.StringValue("ref-2"), observed)
        val effect = assertIs<DynamicActionEffect.Navigate>(requireNotNull(delivered).single())
        assertEquals(target, effect.destination)
        store.close()
    }


    @Test
    fun completedActionHandsOffOneBatchWithOneEffect() = runTest {
        // Current concrete action result variants expose at most one effect.
        // Do not mistake this test for proof of multi-effect sequence delivery.
        val delivered = mutableListOf<List<DynamicActionEffect>>()
        val target = destination("target")
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(
                    DynamicResponse(
                        200, "OK", "Loaded", null, null,
                        destination = if (request.endpoint == "/submit") target else null,
                    ),
                )
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            deliverEffects = { delivered += it },
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("destination"),
        ))))
        advanceUntilIdle()

        assertEquals(1, delivered.size)
        assertEquals(target, assertIs<DynamicActionEffect.Navigate>(delivered.single().single()).destination)
        store.close()
    }

    @Test
    fun cancelledInFlightActionCannotCommitOrDeliverEffects() = runTest {
        val gate = CompletableDeferred<Unit>()
        val submitted = CompletableDeferred<Unit>()
        var delivered = 0
        val runtimeState = DynamicRuntimeState()
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/submit") {
                    submitted.complete(Unit)
                    gate.await()
                    return Result.success(
                        DynamicResponse(200, "OK", "Done", null, null, destination = destination("next")),
                    )
                }
                return Result.success(DynamicResponse(200, "OK", "Loaded", null, null))
            }
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = runtimeState,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            deliverEffects = { delivered++ },
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("destination"),
        ))))
        submitted.await()
        store.close()
        gate.complete(Unit)
        runCurrent()

        assertEquals(0, delivered)
        assertTrue(runtimeState.values.value.isEmpty())
    }

    @Test
    fun acceptedEffectBatchRemainsWithIndependentSinkAfterSourceStoreClose() = runTest {
        val accepted = mutableListOf<List<DynamicActionEffect>>()
        val target = destination("next")
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(
                    DynamicResponse(
                        200, "OK", "Done", null, null,
                        destination = if (request.endpoint == "/submit") target else null,
                    ),
                )
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            deliverEffects = { effects -> accepted += effects.toList() },
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("destination"),
        ))))
        advanceUntilIdle()
        assertEquals(1, accepted.size)
        store.close()

        assertEquals(target, assertIs<DynamicActionEffect.Navigate>(accepted.single().single()).destination)
    }

    private fun store(
        entry: DynamicNavigationEntry,
        repository: DynamicRepository,
        runtimeState: DynamicRuntimeState,
        dispatcher: CoroutineDispatcher,
    ) = DynamicStore(
        loadDynamicScreen = LoadDynamicScreenUseCase(repository),
        actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
        validator = DynamicFormValidator(),
        entry = entry,
        runtimeState = runtimeState,
        scope = CoroutineScope(dispatcher),
    )

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")

    private fun ref(type: String, value: String) =
        DynamicValue.ObjectValue(mapOf(type to DynamicValue.StringValue(value)))
}
