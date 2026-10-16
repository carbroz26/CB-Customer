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
            commitActionResult = { _, effects, trace ->
                deliveredTrace = trace
                deliveredEffects = effects
                true
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
            commitActionResult = { changes, effects, _ ->
                state.setAll(changes)
                observed = state.values.value["reference"]
                delivered = effects
                true
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
            commitActionResult = { _, effects, _ -> delivered += effects; true },
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
            commitActionResult = { _, _, _ -> delivered++; true },
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
            commitActionResult = { _, effects, _ -> accepted += effects.toList(); true },
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


    @Test
    fun successfulRequestUsesOneNonSuspendingCommitForStateAndEffect() = runTest {
        val target = destination("after-submit")
        val state = DynamicRuntimeState()
        var commitCount = 0
        var acceptedEffect: DynamicActionEffect? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(
                    200, "OK", "Done", null, null,
                    dataPayload = DynamicValue.ObjectValue(
                        mapOf("reference" to DynamicValue.StringValue("saved"))),
                    destination = if (request.endpoint == "/submit") target else null,
                ))
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = state,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            commitActionResult = { changes, effects, _ ->
                commitCount++
                assertEquals(DynamicValue.StringValue("saved"), changes["reference"])
                state.setAll(changes)
                acceptedEffect = effects.single()
                true
            },
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("destination"),
            "retain" to DynamicValue.ObjectValue(
                mapOf("reference" to DynamicValue.StringValue("data.reference"))),
        ))))
        advanceUntilIdle()
        assertEquals(1, commitCount)
        assertEquals(DynamicValue.StringValue("saved"), state.values.value["reference"])
        assertEquals(target, assertIs<DynamicActionEffect.Navigate>(acceptedEffect).destination)
        store.close()
    }

    @Test
    fun rejectedCommitReportsSafeFailureWithoutMutatingRuntimeState() = runTest {
        val state = DynamicRuntimeState()
        var attempts = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(200, "OK", "Done", null, null,
                    destination = destination("next")))
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = state,
            scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            commitActionResult = { _, _, _ -> attempts++; false },
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("destination"),
        ))))
        advanceUntilIdle()
        assertEquals(1, attempts)
        assertEquals("Unable to complete action",
            assertIs<DynamicState.Success>(store.state.value).screen.validationMessage)
        assertTrue(state.values.value.isEmpty())
        store.close()
    }

    @Test
    fun entrySnapshotRestoresResponseBindingsAndSafeMessageWithoutAnotherGet() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var fetches = 0
        val response = DynamicResponse(200, "SUCCESS", "Loaded", null, "trace")
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                fetches++
                return Result.success(response)
            }
        }
        val entry = DynamicNavigationEntry(destination("same"))
        val runtime = DynamicRuntimeState()
        val first = store(entry, repository, runtime, dispatcher)
        first.accept(DynamicIntent.Load)
        advanceUntilIdle()
        first.accept(DynamicIntent.ValueChanged("phone", DynamicValue.StringValue("123")))
        first.accept(DynamicIntent.Action(DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/submit"),
            "responseMode" to DynamicValue.StringValue("none"),
            "validate" to DynamicValue.BooleanValue(true),
        ))))
        advanceUntilIdle()
        val saved = assertIs<DynamicState.Success>(first.state.value).screen
        assertNotNull(saved.validationMessage)
        assertEquals(1, fetches)
        first.close()

        val second = store(entry, repository, runtime, dispatcher)
        second.accept(DynamicIntent.Load)
        advanceUntilIdle()
        val restored = assertIs<DynamicState.Success>(second.state.value).screen
        assertEquals(1, fetches)
        assertEquals(response, restored.response)
        assertEquals(saved.bindings, restored.bindings)
        assertEquals(saved.validationMessage, restored.validationMessage)
        second.accept(DynamicIntent.ClearMessage)
        second.close()
        val third = store(entry, repository, runtime, dispatcher)
        third.accept(DynamicIntent.Load)
        assertEquals(null, assertIs<DynamicState.Success>(third.state.value).screen.validationMessage)
        assertEquals(1, fetches)
        third.close()
    }

    @Test
    fun newEntryDoesNotInheritResponseSnapshotOrValidationMessage() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var fetches = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                fetches++
                return Result.success(DynamicResponse(200, "SUCCESS", request.endpoint, null, null))
            }
        }
        val navigation = com.carbroz.cbpartner.navigation.DynamicNavigationState(destination("same"))
        val firstEntry = navigation.currentEntry
        val first = store(firstEntry, repository, DynamicRuntimeState(), dispatcher)
        first.accept(DynamicIntent.Load)
        advanceUntilIdle()
        first.close()
        navigation.push(destination("same"))
        val second = store(navigation.currentEntry, repository, DynamicRuntimeState(), dispatcher)
        second.accept(DynamicIntent.Load)
        advanceUntilIdle()
        assertEquals(2, fetches)
        assertNotNull(firstEntry.screenResponse)
        assertNotNull(navigation.currentEntry.screenResponse)
        assertTrue(firstEntry !== navigation.currentEntry)
        second.close()
    }

    @Test
    fun failedScreenLoadDoesNotExposeServerErrorMessage() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.failure(IllegalStateException("private-server-token-and-internal-url"))
        }
        val entry = DynamicNavigationEntry(destination("failed"))
        val store = store(entry, repository, DynamicRuntimeState(), dispatcher)

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()

        val failure = assertIs<DynamicState.Failure>(store.state.value)
        assertEquals("Unable to load dynamic screen", failure.message)
        assertEquals(null, entry.screenResponse)
        store.close()
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
