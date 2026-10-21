package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicButtonLoading
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicLoadingIndicator
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DismissActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.action.PresentActionHandler
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
    fun valueChangedImmediatelyBeforeActionUsesLatestBindingSnapshot() = runTest {
        // Use a queued dispatcher to cover the real accept() ordering: state update
        // is synchronous, while Request action execution begins on the next turn.
        val dispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
        val entry = DynamicNavigationEntry(destination("current"))
        var submitted: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/submit") submitted = request.body
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, null))
            }
        }
        val store = store(entry, repository, DynamicRuntimeState(), dispatcher)
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()

        store.accept(DynamicIntent.ValueChanged("otp", DynamicValue.StringValue("1")))
        store.accept(DynamicIntent.ValueChanged("otp", DynamicValue.StringValue("12")))
        store.accept(DynamicIntent.ValueChanged("otp", DynamicValue.StringValue("1234")))
        store.accept(DynamicIntent.Action(DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "responseMode" to DynamicValue.StringValue("none"),
                "body" to DynamicValue.ObjectValue(mapOf("otp" to ref("\$binding", "otp"))),
            ),
        )))
        advanceUntilIdle()

        assertEquals(
            DynamicValue.StringValue("1234"),
            assertIs<DynamicValue.ObjectValue>(submitted).value["otp"],
        )
        assertEquals(
            DynamicValue.StringValue("1234"),
            assertIs<DynamicState.Success>(store.state.value).screen.bindings["otp"],
        )
        assertEquals(DynamicValue.StringValue("1234"), entry.bindings["otp"])
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


    @Test
    fun presentationLifecycleIsOwnedByScreenState() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val entry = DynamicNavigationEntry(destination("root"))
        val store = presentationStore(entry, presentationRepository(), dispatcher)

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
            ),
        )
        advanceUntilIdle()

        var screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals("picker", screen.activePresentationId)
        assertEquals(DynamicValue.StringValue(""), screen.presentationBindings["search"])

        store.accept(
            DynamicIntent.PresentationValueChanged(
                "picker",
                "search",
                DynamicValue.StringValue("india"),
            ),
        )
        screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(DynamicValue.StringValue("india"), screen.presentationBindings["search"])
        assertEquals(null, entry.bindings["search"])

        store.accept(
            DynamicIntent.PresentationValueChanged(
                "picker",
                "selectedId",
                DynamicValue.StringValue("IN"),
            ),
        )
        screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(DynamicValue.StringValue("IN"), screen.bindings["selectedId"])
        assertEquals(DynamicValue.StringValue("IN"), entry.bindings["selectedId"])

        store.accept(DynamicIntent.Action(DynamicAction("dismiss"), "picker"))
        advanceUntilIdle()
        screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(null, screen.activePresentationId)
        assertTrue(screen.presentationBindings.isEmpty())

        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
            ),
        )
        advanceUntilIdle()
        screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(DynamicValue.StringValue(""), screen.presentationBindings["search"])
        store.close()
    }

    @Test
    fun invalidOrNestedPresentationIsRejectedWithoutNavigationEffect() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var committedEffects = 0
        val entry = DynamicNavigationEntry(destination("root"))
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(presentationRepository()),
            actionExecutor = DynamicActionExecutor(
                listOf(PresentActionHandler(), DismissActionHandler()),
            ),
            validator = DynamicFormValidator(),
            entry = entry,
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
            commitActionResult = { _, effects, _ ->
                committedEffects += effects.size
                true
            },
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("missing")),
                ),
            ),
        )
        advanceUntilIdle()
        var screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(null, screen.activePresentationId)
        assertEquals("Unable to complete action", screen.validationMessage)
        assertEquals(0, committedEffects)

        store.accept(DynamicIntent.ClearMessage)
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
            ),
        )
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
                "picker",
            ),
        )
        advanceUntilIdle()
        screen = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals("picker", screen.activePresentationId)
        assertEquals("Unable to complete action", screen.validationMessage)
        assertEquals(0, committedEffects)
        store.close()
    }

    @Test
    fun stalePresentationOriginDoesNotExecuteAction() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var invoked = false
        val capture = object : DynamicActionHandler {
            override val type = "capture"

            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ): DynamicActionResult {
                invoked = true
                return DynamicActionResult.Completed
            }
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(presentationRepository()),
            actionExecutor = DynamicActionExecutor(
                listOf(PresentActionHandler(), DismissActionHandler(), capture),
            ),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(DynamicAction("capture"), "picker"))
        advanceUntilIdle()

        assertEquals(false, invoked)
        store.close()
    }

    @Test
    fun presentationOriginUsesLocalBindingPrecedenceAndScopedValidation() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var observedBindings: Map<String, DynamicValue>? = null
        var requestInvoked = false
        val request = object : DynamicActionHandler {
            override val type = "request"

            override suspend fun handle(
                action: DynamicAction,
                bindings: Map<String, DynamicValue>,
                context: Map<String, DynamicValue>,
                state: Map<String, DynamicValue>,
            ): DynamicActionResult {
                observedBindings = bindings
                requestInvoked = true
                return DynamicActionResult.Completed
            }
        }
        val entry = DynamicNavigationEntry(destination("root"))
        entry.updateBindings(
            mapOf(
                "search" to DynamicValue.StringValue("screen"),
                "parent" to DynamicValue.StringValue("kept"),
            ),
        )
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(
                presentationRepository(withValidation = true),
            ),
            actionExecutor = DynamicActionExecutor(listOf(PresentActionHandler(), request)),
            validator = DynamicFormValidator(),
            entry = entry,
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
            ),
        )
        advanceUntilIdle()
        store.accept(
            DynamicIntent.PresentationValueChanged(
                "picker",
                "modalValue",
                DynamicValue.StringValue("valid"),
            ),
        )
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "request",
                    mapOf("validate" to DynamicValue.BooleanValue(true)),
                ),
                "picker",
            ),
        )
        advanceUntilIdle()

        assertTrue(requestInvoked)
        assertEquals(DynamicValue.StringValue(""), observedBindings?.get("search"))
        assertEquals(DynamicValue.StringValue("kept"), observedBindings?.get("parent"))
        assertEquals(DynamicValue.StringValue("valid"), observedBindings?.get("modalValue"))
        assertEquals(
            null,
            assertIs<DynamicState.Success>(store.state.value).screen.validationMessage,
        )
        store.close()
    }

    @Test
    fun systemDismissOnlyClosesMatchingActivePresentation() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val store = presentationStore(
            DynamicNavigationEntry(destination("root")),
            presentationRepository(),
            dispatcher,
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
            ),
        )
        advanceUntilIdle()
        store.accept(DynamicIntent.DismissPresentation("stale"))
        assertEquals(
            "picker",
            assertIs<DynamicState.Success>(store.state.value).screen.activePresentationId,
        )
        store.accept(DynamicIntent.DismissPresentation("picker"))
        assertEquals(
            null,
            assertIs<DynamicState.Success>(store.state.value).screen.activePresentationId,
        )
        store.close()
    }

    private fun presentationStore(
        entry: DynamicNavigationEntry,
        repository: DynamicRepository,
        dispatcher: CoroutineDispatcher,
    ) = DynamicStore(
        loadDynamicScreen = LoadDynamicScreenUseCase(repository),
        actionExecutor = DynamicActionExecutor(
            listOf(PresentActionHandler(), DismissActionHandler()),
        ),
        validator = DynamicFormValidator(),
        entry = entry,
        runtimeState = DynamicRuntimeState(),
        scope = CoroutineScope(dispatcher),
    )

    private fun presentationRepository(withValidation: Boolean = false): DynamicRepository =
        object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(presentationResponse(withValidation))
        }

    private fun presentationResponse(withValidation: Boolean = false): DynamicResponse {
        val screenInput = if (withValidation) {
            listOf(
                InputElement(
                    id = "screen_required",
                    binding = DynamicBinding("screenRequired"),
                    validation = DynamicValidation(
                        required = true,
                        pattern = null,
                        message = "Screen should not validate",
                    ),
                ),
            )
        } else {
            emptyList()
        }
        val presentationInput = if (withValidation) {
            listOf(
                InputElement(
                    id = "modal_required",
                    binding = DynamicBinding("modalValue"),
                    validation = DynamicValidation(
                        required = true,
                        pattern = null,
                        message = "Modal value required",
                    ),
                ),
            )
        } else {
            emptyList()
        }
        val presentation = BottomSheetPresentation(
            id = "picker",
            initialBindings = mapOf(
                "search" to DynamicValue.StringValue(""),
                "modalValue" to DynamicValue.StringValue(""),
            ),
            components = listOf(
                StackComponent(
                    id = "picker_component",
                    elements = presentationInput,
                ),
            ),
        )
        val screen = DynamicScreen(
            screenId = "root",
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(
                id = "tpl_root",
                components = listOf(
                    StackComponent(
                        id = "screen_component",
                        elements = screenInput,
                    ),
                ),
            ),
            theme = null,
            presentations = listOf(presentation),
        )
        return DynamicResponse(200, "SUCCESS", "Loaded", screen, null)
    }



    @Test
    fun screenInitialBindingsSeedEntryAndExistingEntryValuesWin() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var fetches = 0
        val screen = DynamicScreen(
            screenId = "root",
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(id = "tpl_root"),
            theme = null,
            initialBindings = mapOf(
                "countryId" to DynamicValue.StringValue("IN"),
                "mobileNumber" to DynamicValue.StringValue(""),
            ),
        )
        val response = DynamicResponse(200, "SUCCESS", "Loaded", screen, null)
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                fetches++
                return Result.success(response)
            }
        }
        val entry = DynamicNavigationEntry(destination("root"))
        entry.updateBindings(
            mapOf("mobileNumber" to DynamicValue.StringValue("9876543210")),
        )

        val first = store(entry, repository, DynamicRuntimeState(), dispatcher)
        first.accept(DynamicIntent.Load)
        advanceUntilIdle()
        val loaded = assertIs<DynamicState.Success>(first.state.value).screen
        assertEquals(DynamicValue.StringValue("IN"), loaded.bindings["countryId"])
        assertEquals(
            DynamicValue.StringValue("9876543210"),
            loaded.bindings["mobileNumber"],
        )
        assertEquals(loaded.bindings, entry.bindings)
        assertEquals(1, fetches)
        first.close()

        val second = store(entry, repository, DynamicRuntimeState(), dispatcher)
        second.accept(DynamicIntent.Load)
        advanceUntilIdle()
        assertEquals(1, fetches)
        assertEquals(
            DynamicValue.StringValue("9876543210"),
            assertIs<DynamicState.Success>(second.state.value)
                .screen.bindings["mobileNumber"],
        )
        second.close()
    }

    @Test
    fun validationErrorsAreRetainedAndRevalidatedOnEdit() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var submitCalls = 0
        val response = validationResponse()
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/root") return Result.success(response)
                submitCalls++
                return Result.success(DynamicResponse(200, "SUCCESS", "Done", null, null))
            }
        }
        val entry = DynamicNavigationEntry(destination("root"))
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = entry,
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "request",
                    mapOf(
                        "endpoint" to DynamicValue.StringValue("/submit"),
                        "responseMode" to DynamicValue.StringValue("none"),
                        "validate" to DynamicValue.BooleanValue(true),
                    ),
                ),
            ),
        )
        advanceUntilIdle()

        var current = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals("Phone required", current.validationErrors["phone"])
        assertEquals(null, current.validationMessage)
        assertEquals(current.validationErrors, entry.validationErrors)
        assertEquals(0, submitCalls)

        store.accept(
            DynamicIntent.ValueChanged(
                "phone",
                DynamicValue.StringValue("1234567890"),
            ),
        )
        current = assertIs<DynamicState.Success>(store.state.value).screen
        assertTrue(current.validationErrors.isEmpty())
        assertTrue(entry.validationErrors.isEmpty())
        store.close()
    }

    @Test
    fun focusStateIsScopedBetweenScreenAndPresentation() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val store = presentationStore(
            DynamicNavigationEntry(destination("root")),
            presentationRepository(),
            dispatcher,
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.FocusChanged("screenField", true))
        assertEquals(
            "screenField",
            assertIs<DynamicState.Success>(store.state.value).screen.focusedBinding,
        )

        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    "present",
                    mapOf("presentationId" to DynamicValue.StringValue("picker")),
                ),
            ),
        )
        advanceUntilIdle()
        store.accept(DynamicIntent.PresentationFocusChanged("picker", "search", true))

        var current = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals("screenField", current.focusedBinding)
        assertEquals("search", current.presentationFocusedBinding)

        store.accept(DynamicIntent.PresentationFocusChanged("stale", "other", true))
        current = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals("search", current.presentationFocusedBinding)

        store.accept(DynamicIntent.DismissPresentation("picker"))
        current = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(null, current.presentationFocusedBinding)
        assertEquals("screenField", current.focusedBinding)
        store.close()
    }

    private fun validationResponse(): DynamicResponse {
        val input = InputElement(
            id = "phone",
            binding = DynamicBinding("phone"),
            validation = DynamicValidation(
                required = true,
                pattern = "^[0-9]{10}$",
                message = "Phone required",
            ),
        )
        val group = StackGroup(
            id = "phone_group",
            elements = listOf(input),
            validationPresentation = DynamicValidationPresentation(
                binding = "phone",
                errorBorder = DynamicBorder(1.0, "#FF0000"),
            ),
        )
        val screen = DynamicScreen(
            screenId = "root",
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(
                id = "tpl_root",
                components = listOf(
                    StackComponent(
                        id = "component",
                        sections = listOf(
                            StackSection(
                                id = "section",
                                groups = listOf(group),
                            ),
                        ),
                    ),
                ),
            ),
            theme = null,
        )
        return DynamicResponse(200, "SUCCESS", "Loaded", screen, null)
    }

    private fun pendingResponse(
        validationRequired: Boolean = false,
        withPresentation: Boolean = false,
    ): Pair<DynamicResponse, DynamicAction> {
        val request = DynamicAction(
            "request",
            mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "validate" to DynamicValue.BooleanValue(validationRequired),
                "body" to DynamicValue.ObjectValue(
                    mapOf("field" to ref("\$binding", "field")),
                ),
            ),
        )
        val button = ButtonElement(
            id = "shared",
            text = "Submit",
            actions = mapOf("onClick" to request),
            loading = DynamicButtonLoading(
                whileActionPending = true,
                disableWhilePending = true,
                replaceContent = true,
                indicator = DynamicLoadingIndicator.Circular(20.0, "#FFFFFF"),
            ),
        )
        val input = if (validationRequired) {
            listOf(
                InputElement(
                    id = "input",
                    binding = DynamicBinding("field"),
                    validation = DynamicValidation(required = true, pattern = null, message = "Required"),
                ),
            )
        } else emptyList()
        val screen = DynamicScreen(
            screenId = "root",
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(
                id = "tpl_root",
                components = listOf(
                    StackComponent(id = "main", elements = input + button),
                ),
            ),
            theme = null,
            presentations = if (withPresentation) {
                listOf(
                    BottomSheetPresentation(
                        id = "picker",
                        initialBindings = mapOf("search" to DynamicValue.StringValue("")),
                        components = listOf(StackComponent(id = "sheet", elements = listOf(button))),
                    ),
                )
            } else emptyList(),
        )
        return DynamicResponse(200, "SUCCESS", "OK", screen, null) to request
    }

    @Test
    fun pendingBeginsAfterValidationAndBlocksRepeatedSubmitUntilSuccess() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val (response, requestAction) = pendingResponse()
        val release = CompletableDeferred<Unit>()
        var submits = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/submit") {
                    submits++
                    release.await()
                    Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, null))
                } else {
                    Result.success(response)
                }
        }
        val store = store(DynamicNavigationEntry(destination("root")), repository, DynamicRuntimeState(), dispatcher)
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(requestAction, buttonId = "shared"))
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        runCurrent()
        assertEquals(DynamicPendingButton("shared"), assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        assertEquals(1, submits)
        store.accept(DynamicIntent.Action(requestAction, buttonId = "shared"))
        runCurrent()
        assertEquals(1, submits)
        release.complete(Unit)
        advanceUntilIdle()
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        store.close()
    }

    @Test
    fun validFormRequestUsesLatestBindingBeforeStartingPending() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val (response, action) = pendingResponse(validationRequired = true)
        val gate = CompletableDeferred<Unit>()
        var bodyObserved: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/submit") {
                    bodyObserved = request.body
                    gate.await()
                    Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, null))
                } else Result.success(response)
        }
        val store = store(DynamicNavigationEntry(destination("root")), repository, DynamicRuntimeState(), dispatcher)
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.ValueChanged("field", DynamicValue.StringValue("ready")))
        store.accept(DynamicIntent.Action(action, buttonId = "shared"))
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        runCurrent()
        val pending = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals(DynamicPendingButton("shared"), pending.pendingButton)
        assertTrue(pending.validationErrors.isEmpty())
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("field" to DynamicValue.StringValue("ready")),
            ),
            bodyObserved,
        )
        gate.complete(Unit)
        advanceUntilIdle()
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        store.close()
    }

    @Test
    fun failedValidationOrUnrecognizedButtonOriginDoesNotStartPending() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (response, requestAction) = pendingResponse(validationRequired = true)
        var submits = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/submit") {
                    submits++
                    Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, null))
                } else Result.success(response)
        }
        val store = store(DynamicNavigationEntry(destination("root")), repository, DynamicRuntimeState(), dispatcher)
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(requestAction, buttonId = "unknown"))
        store.accept(DynamicIntent.Action(requestAction.copy(type = "navigate"), buttonId = "shared"))
        assertEquals(0, submits)
        store.accept(DynamicIntent.Action(requestAction, buttonId = "shared"))
        advanceUntilIdle()
        assertEquals(0, submits)
        val validated = assertIs<DynamicState.Success>(store.state.value).screen
        assertNull(validated.pendingButton)
        assertNotNull(validated.validationErrors["field"])
        store.close()
    }

    @Test
    fun pendingClearsAfterNetworkFailureAndRejectedResultCommit() = runTest {
        for (failNetwork in listOf(true, false)) {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            val (response, action) = pendingResponse()
            val gate = CompletableDeferred<Unit>()
            var accepted = 0
            val repository = object : DynamicRepository {
                override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                    if (request.endpoint == "/submit") {
                        gate.await()
                        if (failNetwork) Result.failure(IllegalStateException("private details"))
                        else Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, null))
                    } else Result.success(response)
            }
            val store = DynamicStore(
                loadDynamicScreen = LoadDynamicScreenUseCase(repository),
                actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
                validator = DynamicFormValidator(),
                entry = DynamicNavigationEntry(destination("root")),
                runtimeState = DynamicRuntimeState(),
                scope = CoroutineScope(dispatcher),
                commitActionResult = { _, _, _ -> accepted++; false },
            )
            store.accept(DynamicIntent.Load)
            advanceUntilIdle()
            store.accept(DynamicIntent.Action(action, buttonId = "shared"))
            assertEquals(DynamicPendingButton("shared"), assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
            gate.complete(Unit)
            advanceUntilIdle()
            val done = assertIs<DynamicState.Success>(store.state.value).screen
            assertNull(done.pendingButton)
            assertEquals("Unable to complete action", done.validationMessage)
            assertEquals(if (failNetwork) 0 else 1, accepted)
            store.close()
        }
    }

    @Test
    fun closingStoreCancelsPendingRequestAndPreventsItsCommit() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (response, action) = pendingResponse()
        val gate = CompletableDeferred<Unit>()
        var effects = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/submit") {
                    gate.await()
                    Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, null))
                } else Result.success(response)
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
            commitActionResult = { _, _, _ -> effects++; true },
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(action, buttonId = "shared"))
        assertEquals(DynamicPendingButton("shared"), assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        store.close()
        gate.complete(Unit)
        advanceUntilIdle()
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        assertEquals(0, effects)
        store.accept(DynamicIntent.Action(action, buttonId = "shared"))
        assertEquals(0, effects)
    }

    @Test
    fun presentationGenerationRejectsStaleCallbacksAfterDismissAndReopen() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (response, action) = pendingResponse(withPresentation = true)
        val gate = CompletableDeferred<Unit>()
        var requestCalls = 0
        var effectCount = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/submit") {
                    requestCalls++
                    gate.await()
                    Result.success(DynamicResponse(204, "NO_CONTENT", "OK", null, null))
                } else Result.success(response)
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(PresentActionHandler(), RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(destination("root")),
            runtimeState = DynamicRuntimeState(),
            scope = CoroutineScope(dispatcher),
            commitActionResult = { _, _, _ -> effectCount++; true },
        )
        val presentAction = DynamicAction(
            "present",
            mapOf("presentationId" to DynamicValue.StringValue("picker")),
        )
        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.Action(presentAction))
        advanceUntilIdle()
        val firstGeneration = assertIs<DynamicState.Success>(store.state.value).screen.presentationGeneration
        assertEquals(1L, firstGeneration)
        store.accept(DynamicIntent.Action(action, "picker", "shared", firstGeneration))
        assertEquals(
            DynamicPendingButton("shared", "picker", firstGeneration),
            assertIs<DynamicState.Success>(store.state.value).screen.pendingButton,
        )
        store.accept(DynamicIntent.DismissPresentation("picker", firstGeneration))
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        gate.complete(Unit)
        advanceUntilIdle()
        assertEquals(0, effectCount)
        store.accept(DynamicIntent.Action(presentAction))
        advanceUntilIdle()
        val current = assertIs<DynamicState.Success>(store.state.value).screen
        val reopenedGeneration = current.presentationGeneration
        assertEquals(firstGeneration + 1, reopenedGeneration)
        store.accept(DynamicIntent.Action(action, "picker", "shared", firstGeneration))
        store.accept(DynamicIntent.PresentationValueChanged(
            "picker", "search", DynamicValue.StringValue("stale"), firstGeneration,
        ))
        store.accept(DynamicIntent.PresentationFocusChanged("picker", "search", true, firstGeneration))
        store.accept(DynamicIntent.DismissPresentation("picker", firstGeneration))
        val safe = assertIs<DynamicState.Success>(store.state.value).screen
        assertEquals("picker", safe.activePresentationId)
        assertEquals(DynamicValue.StringValue(""), safe.presentationBindings["search"])
        assertNull(safe.presentationFocusedBinding)
        assertNull(safe.pendingButton)
        assertEquals(1, requestCalls)
        store.accept(DynamicIntent.Action(action, "picker", "shared", reopenedGeneration))
        advanceUntilIdle()
        assertEquals(2, requestCalls)
        assertNull(assertIs<DynamicState.Success>(store.state.value).screen.pendingButton)
        store.close()
    }


}
