package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.RequestActionHandler
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicStoreTest {
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
    fun effectHandlerFailureUsesFailureStateAndPreservesCurrentScreen() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "test"))
        }
        val store = store(
            DynamicNavigationEntry(destination("current")),
            repository,
            DynamicRuntimeState(),
            dispatcher,
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        val currentScreen = assertIs<DynamicState.Success>(store.state.value).screen

        store.accept(DynamicIntent.EffectFailed("Unable to open external URI"))

        val failure = assertIs<DynamicState.Failure>(store.state.value)
        assertEquals("Unable to open external URI", failure.message)
        assertEquals(currentScreen, failure.screen)
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
