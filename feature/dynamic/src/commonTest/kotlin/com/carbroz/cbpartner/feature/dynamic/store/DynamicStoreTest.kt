package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.RequestActionHandler
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicStoreTest {
    @Test
    fun requestResolvesMultipleBindingsAndContext() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var capturedBody: DynamicValue? = null
        val next = destination("screen_next", "tpl_next", "/next")
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                capturedBody = request.body
                return Result.success(
                    DynamicResponse(200, "SUCCESS", "Accepted", null, "request-1", destination = next),
                )
            }
        }

        val current = destination("screen_current", "tpl_current", "/current")
        val navigation = DynamicNavigationState(current)
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            destination = current,
            contextProvider = { mapOf("session.id" to "session-42") },
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.ValueChanged("firstName", "Ada"))
        store.accept(DynamicIntent.ValueChanged("email", "ada@example.com"))
        store.accept(
            DynamicIntent.Action(
                action = DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/submit"),
                        "method" to DynamicValue.StringValue("POST"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf(
                                "firstName" to ref("$binding", "firstName"),
                                "email" to ref("$binding", "email"),
                                "sessionId" to ref("$context", "session.id"),
                            ),
                        ),
                    ),
                ),
                navigation = navigation,
            ),
        )

        advanceUntilIdle()

        val body = assertIs<DynamicValue.ObjectValue>(assertNotNull(capturedBody))
        assertEquals(DynamicValue.StringValue("Ada"), body.value["firstName"])
        assertEquals(DynamicValue.StringValue("ada@example.com"), body.value["email"])
        assertEquals(DynamicValue.StringValue("session-42"), body.value["sessionId"])
        assertEquals("screen_next", navigation.currentDestination.screenId)
        store.close()
    }

    @Test
    fun previousResponseReferenceSurvivesDestinationTransition() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val firstDestination = destination("screen_first", "tpl_first", "/first")
        val secondDestination = destination("screen_second", "tpl_second", "/second")
        var secondBody: DynamicValue? = null

        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                return if (request.endpoint == "/first-action") {
                    Result.success(
                        DynamicResponse(
                            200,
                            "SUCCESS",
                            "Created",
                            null,
                            "first",
                            dataPayload = DynamicValue.ObjectValue(
                                mapOf("referenceId" to DynamicValue.StringValue("ref-123")),
                            ),
                            destination = secondDestination,
                        ),
                    )
                } else {
                    secondBody = request.body
                    Result.success(DynamicResponse(200, "SUCCESS", "Done", null, "second"))
                }
            }
        }

        val navigation = DynamicNavigationState(firstDestination)
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            destination = firstDestination,
            scope = CoroutineScope(dispatcher),
        )

        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/first-action"),
                        "responseMode" to DynamicValue.StringValue("destination"),
                    ),
                ),
                navigation,
            ),
        )
        advanceUntilIdle()
        assertEquals("screen_second", navigation.currentDestination.screenId)

        store.updateDestination(secondDestination)
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/second-action"),
                        "responseMode" to DynamicValue.StringValue("none"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf(
                                "referenceId" to ref("$response", "data.referenceId"),
                            ),
                        ),
                    ),
                ),
                navigation,
            ),
        )
        advanceUntilIdle()

        val body = assertIs<DynamicValue.ObjectValue>(assertNotNull(secondBody))
        assertEquals(DynamicValue.StringValue("ref-123"), body.value["referenceId"])
        store.close()
    }

    @Test
    fun validationTraversesMultipleInputsAndBlocksInvalidRequest() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var requestCalled = false
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                requestCalled = true
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, "unused"))
            }
        }

        val current = destination("screen_form", "tpl_form", "/form")
        val screen = DynamicScreen(
            screenId = current.screenId,
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(
                id = current.templateId,
                components = listOf(
                    StackComponent(
                        id = "form",
                        sections = listOf(
                            StackSection(
                                id = "fields",
                                elements = listOf(
                                    InputElement(
                                        id = "required",
                                        binding = com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding("required"),
                                        validation = DynamicValidation(required = true, message = "Required field"),
                                    ),
                                    InputElement(
                                        id = "code",
                                        binding = com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding("code"),
                                        validation = DynamicValidation(pattern = "^[0-9]{4}$", message = "Four digits"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            theme = null,
        )

        val navigation = DynamicNavigationState(current)
        val loadRepository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(200, "SUCCESS", "Loaded", screen, "load"))
        }
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(loadRepository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            destination = current,
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.Load)
        advanceUntilIdle()
        store.accept(DynamicIntent.ValueChanged("code", "12"))
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf("endpoint" to DynamicValue.StringValue("/submit"), "validate" to DynamicValue.BooleanValue(true)),
                ),
                navigation,
            ),
        )
        advanceUntilIdle()

        val failure = assertIs<DynamicState.Failure>(store.state.value)
        assertEquals("Required field", failure.message)
        assertEquals(false, requestCalled)
        store.close()
    }

    @Test
    fun requestFailureBecomesStoreFailure() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val current = destination("screen_current", "tpl_current", "/current")
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.failure(IllegalStateException("network unavailable"))
        }

        val navigation = DynamicNavigationState(current)
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            destination = current,
            scope = CoroutineScope(dispatcher),
        )

        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf("endpoint" to DynamicValue.StringValue("/submit")),
                ),
                navigation,
            ),
        )
        advanceUntilIdle()

        val failure = assertIs<DynamicState.Failure>(store.state.value)
        assertEquals("network unavailable", failure.message)
        store.close()
    }

    private fun destination(screenId: String, templateId: String, endpoint: String) =
        DynamicDestination(screenId, templateId, "form_template", endpoint)

    private fun ref(type: String, value: String) =
        DynamicValue.ObjectValue(mapOf(type to DynamicValue.StringValue(value)))
}
