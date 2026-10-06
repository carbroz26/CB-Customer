package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RequestActionHandlerTest {
    @Test
    fun resolvesMultipleBindingsContextAndResponseReferencesInNestedRequestBody() = runTest {
        var capturedRequest: DynamicRequest? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                capturedRequest = request
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "SUCCESS",
                        message = "Accepted",
                        data = null,
                        traceId = "response-1",
                        destination = DynamicDestination(
                            "screen_next",
                            "tpl_next",
                            "form_template",
                            "/next",
                        ),
                    ),
                )
            }
        }

        val current = DynamicDestination(
            "screen_current",
            "tpl_current",
            "form_template",
            "/current",
        )
        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "method" to DynamicValue.StringValue("PATCH"),
                "endpoint" to DynamicValue.StringValue("/submit"),
                "authentication" to DynamicValue.StringValue("SESSION"),
                "validate" to DynamicValue.BooleanValue(true),
                "responseMode" to DynamicValue.StringValue("destination"),
                "body" to DynamicValue.ObjectValue(
                    mapOf(
                        "profile" to DynamicValue.ObjectValue(
                            mapOf(
                                "firstName" to DynamicValue.ObjectValue(
                                    mapOf("$binding" to DynamicValue.StringValue("firstName")),
                                ),
                                "email" to DynamicValue.ObjectValue(
                                    mapOf("$binding" to DynamicValue.StringValue("email")),
                                ),
                            ),
                        ),
                        "sessionId" to DynamicValue.ObjectValue(
                            mapOf("$context" to DynamicValue.StringValue("session.id")),
                        ),
                        "referenceId" to DynamicValue.ObjectValue(
                            mapOf("$response" to DynamicValue.StringValue("data.referenceId")),
                        ),
                    ),
                ),
            ),
        )

        val previousResponse = DynamicValue.ObjectValue(
            mapOf(
                "data" to DynamicValue.ObjectValue(
                    mapOf("referenceId" to DynamicValue.StringValue("ref-123")),
                ),
            ),
        )

        val navigation = DynamicNavigationState(current)
        val result = RequestActionHandler(repository).handle(
            action = action,
            navigation = navigation,
            bindings = mapOf(
                "firstName" to "Ada",
                "email" to "ada@example.com",
            ),
            context = mapOf("session.id" to "session-42"),
            previousResponse = previousResponse,
        )

        val navigate = assertIs<DynamicActionResult.Navigate>(result)
        assertEquals("screen_next", navigate.destination.screenId)

        val request = requireNotNull(capturedRequest)
        assertEquals("/submit", request.endpoint)
        assertEquals("PATCH", request.method)
        assertEquals("SESSION", request.authentication)
        assertEquals(true, request.validate)

        val body = assertIs<DynamicValue.ObjectValue>(request.body)
        val profile = assertIs<DynamicValue.ObjectValue>(body.value["profile"])
        assertEquals(DynamicValue.StringValue("Ada"), profile.value["firstName"])
        assertEquals(DynamicValue.StringValue("ada@example.com"), profile.value["email"])
        assertEquals(DynamicValue.StringValue("session-42"), body.value["sessionId"])
        assertEquals(DynamicValue.StringValue("ref-123"), body.value["referenceId"])
    }

    @Test
    fun returnsCompletedForNonNavigatingRequest() = runTest {
        var capturedResponseMode: String? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                capturedResponseMode = request.responseMode
                return Result.success(
                    DynamicResponse(
                        status = 204,
                        code = "NO_CONTENT",
                        message = "Completed",
                        data = null,
                        traceId = "response-2",
                    ),
                )
            }
        }

        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/save"),
                "responseMode" to DynamicValue.StringValue("none"),
            ),
        )

        val result = RequestActionHandler(repository).handle(
            action = action,
            navigation = DynamicNavigationState(
                DynamicDestination("screen_current", "tpl_current", "form_template", "/current"),
            ),
            bindings = emptyMap(),
            context = emptyMap(),
            previousResponse = null,
        )

        assertEquals("none", capturedResponseMode)
        assertIs<DynamicActionResult.Completed>(result)
    }

    @Test
    fun propagatesRepositoryFailure() = runTest {
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.failure(IllegalStateException("network unavailable"))
        }

        val action = DynamicAction(
            type = "request",
            payload = mapOf("endpoint" to DynamicValue.StringValue("/save")),
        )

        val result = runCatching {
            RequestActionHandler(repository).handle(
                action = action,
                navigation = DynamicNavigationState(
                    DynamicDestination("screen_current", "tpl_current", "form_template", "/current"),
                ),
                bindings = emptyMap(),
                context = emptyMap(),
                previousResponse = null,
            )
        }

        assertEquals("network unavailable", result.exceptionOrNull()?.message)
    }
}
