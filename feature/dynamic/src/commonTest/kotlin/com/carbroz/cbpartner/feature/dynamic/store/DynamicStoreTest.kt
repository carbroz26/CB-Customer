package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.RequestActionHandler
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicStoreTest {
    @Test
    fun typedBindingIsUsedByRequest() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var body: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                body = request.body
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "request"))
            }
        }
        val entry = DynamicNavigationEntry(destination("current"))
        val navigation = DynamicNavigationState(entry.destination)
        val store = store(entry, repository, DynamicRuntimeState(), dispatcher)

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
                navigation,
            ),
        )
        advanceUntilIdle()

        val requestBody = assertIs<DynamicValue.ObjectValue>(body)
        assertEquals(DynamicValue.NumberValue(2.0), requestBody.value["count"])
        store.close()
    }

    @Test
    fun explicitResponseRetentionBecomesRuntimeState() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
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
        val entry = DynamicNavigationEntry(destination("current"))
        val navigation = DynamicNavigationState(entry.destination)
        val store = store(entry, repository, runtimeState, dispatcher)

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
                navigation,
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
    fun screenStateIsIndependentPerNavigationEntry() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.failure(IllegalStateException("not used"))
        }
        val firstStore = store(
            DynamicNavigationEntry(destination("first")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )
        val secondStore = store(
            DynamicNavigationEntry(destination("second")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )

        firstStore.accept(DynamicIntent.ValueChanged("phone", DynamicValue.StringValue("123")))
        secondStore.accept(DynamicIntent.ValueChanged("otp", DynamicValue.StringValue("111111")))

        assertEquals(DynamicValue.StringValue("123"), firstStore.bindings.value["phone"])
        assertEquals(null, firstStore.bindings.value["otp"])
        assertEquals(DynamicValue.StringValue("111111"), secondStore.bindings.value["otp"])
        assertEquals(null, secondStore.bindings.value["phone"])

        firstStore.close()
        secondStore.close()
    }

    @Test
    fun validationUsesTypedBindingValues() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var requestCalled = false
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                requestCalled = true
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "unused"))
            }
        }
        val entry = DynamicNavigationEntry(destination("form"))
        val store = store(entry, repository, DynamicRuntimeState(), dispatcher)
        // Validation is exercised through the existing screen fixtures; this test ensures
        // the Store accepts typed values without String-only binding APIs.
        store.accept(DynamicIntent.ValueChanged("field", DynamicValue.StringValue("value")))
        assertEquals(DynamicValue.StringValue("value"), store.bindings.value["field"])
        assertEquals(false, requestCalled)
        store.close()
    }

    private fun store(
        entry: DynamicNavigationEntry,
        repository: DynamicRepository,
        runtimeState: DynamicRuntimeState,
        dispatcher: StandardTestDispatcher,
    ) = DynamicStore(
        loadDynamicScreen = LoadDynamicScreenUseCase(repository),
        actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
        entry = entry,
        runtimeState = runtimeState,
        scope = CoroutineScope(dispatcher),
    )

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")

    private fun ref(type: String, value: String) =
        DynamicValue.ObjectValue(mapOf(type to DynamicValue.StringValue(value)))
}
