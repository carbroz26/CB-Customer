package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertFailsWith

class RequestActionHandlerTest {

    @Test
    fun successOnlyToastReferenceEmittedAfterRequestRetainAndNeverOnFailure() = runTest {
        val success = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(
                    status = 200, code = "SUCCESS", message = "Sent", data = null, traceId = null,
                    dataPayload = DynamicValue.ObjectValue(mapOf(
                        "challengeId" to DynamicValue.StringValue("next-challenge"),
                        "resendAfterSeconds" to DynamicValue.NumberValue(25.0),
                    )),
                ))
        }
        val payload = mapOf(
            "endpoint" to DynamicValue.StringValue("/send_otp"),
            "responseMode" to DynamicValue.StringValue("none"),
            "successPresentationId" to DynamicValue.StringValue("otp_resend_success"),
            "retain" to DynamicValue.ObjectValue(mapOf(
                "challengeId" to DynamicValue.StringValue("data.challengeId"),
                "resendAfterSeconds" to DynamicValue.StringValue("data.resendAfterSeconds"),
            )),
        )
        val result = RequestActionHandler(success).handle(
            DynamicAction("request", payload), emptyMap(), emptyMap(), emptyMap(),
        )
        val completed = assertIs<DynamicActionResult.SuccessPresentation>(result)
        assertEquals("otp_resend_success", completed.presentationId)
        assertEquals(DynamicValue.StringValue("next-challenge"), completed.stateChanges["challengeId"])
        assertEquals(DynamicValue.NumberValue(25.0), completed.stateChanges["resendAfterSeconds"])

        val failure = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.failure(IllegalStateException("Request failed"))
        }
        assertFailsWith<IllegalStateException> {
            RequestActionHandler(failure).handle(
                DynamicAction("request", payload), emptyMap(), emptyMap(), emptyMap(),
            )
        }
    }

    @Test
    fun successPresentationCannotBeCombinedWithNavigationMode() = runTest {
        var requests = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                requests++
                return Result.success(DynamicResponse(200, "SUCCESS", "OK", null, null))
            }
        }
        assertFailsWith<IllegalStateException> {
            RequestActionHandler(repository).handle(
                DynamicAction("request", mapOf(
                    "endpoint" to DynamicValue.StringValue("/submit"),
                    "responseMode" to DynamicValue.StringValue("destination"),
                    "successPresentationId" to DynamicValue.StringValue("notification"),
                )), emptyMap(), emptyMap(), emptyMap(),
            )
        }
        assertEquals(0, requests)
    }

    @Test
    fun diagnosticContextReachesRequestWithoutEnteringBody() = runTest {
        var captured: DynamicRequest? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                captured = request
                return Result.success(DynamicResponse(200, "OK", "Done", null, null))
            }
        }
        val trace = com.carbroz.cbpartner.core.diagnostics.TraceContext(
            "session-123", "action-456", "parent-789",
        )
        RequestActionHandler(repository).handle(
            DynamicAction(
                "request",
                mapOf(
                    "endpoint" to DynamicValue.StringValue("/submit"),
                    "responseMode" to DynamicValue.StringValue("none"),
                    "body" to DynamicValue.ObjectValue(mapOf("field" to DynamicValue.StringValue("value"))),
                ),
            ),
            emptyMap(), emptyMap(), emptyMap(), trace,
        )
        val request = requireNotNull(captured)
        assertEquals(trace.sessionId, request.sessionId)
        assertEquals(trace.actionId, request.actionId)
        assertEquals(trace.parentActionId, request.parentActionId)
        val body = assertIs<DynamicValue.ObjectValue>(request.body)
        assertEquals(setOf("field"), body.value.keys)
    }


    @Test
    fun resolvesTypedReferencesAndExplicitlyRetainsResponseValues() = runTest {
        var capturedRequest: DynamicRequest? = null
        val next = DynamicDestination("screen_next", "tpl_next", "form_template", "/next")
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
                        dataPayload = DynamicValue.ObjectValue(
                            mapOf("referenceId" to DynamicValue.StringValue("ref-123")),
                        ),
                        destination = next,
                    ),
                )
            }
        }
        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "method" to DynamicValue.StringValue("PATCH"),
                "responseMode" to DynamicValue.StringValue("destination"),
                "retain" to DynamicValue.ObjectValue(
                    mapOf("referenceId" to DynamicValue.StringValue("data.referenceId")),
                ),
                "body" to DynamicValue.ObjectValue(
                    mapOf(
                        "name" to ref("\$binding", "name"),
                        "environment" to ref("\$context", "environment.id"),
                        "oldReference" to ref("\$state", "oldReference"),
                    ),
                ),
            ),
        )

        val result = RequestActionHandler(repository).handle(
            action = action,
            bindings = mapOf("name" to DynamicValue.StringValue("Ada")),
            context = mapOf(
                "environment" to DynamicValue.ObjectValue(
                    mapOf("id" to DynamicValue.StringValue("environment-42")),
                ),
            ),
            state = mapOf("oldReference" to DynamicValue.StringValue("old")),
        )

        val navigate = assertIs<DynamicActionResult.Navigate>(result)
        assertEquals("screen_next", navigate.destination?.screenId)
        assertEquals(
            DynamicValue.StringValue("ref-123"),
            navigate.stateChanges["referenceId"],
        )
        val request = requireNotNull(capturedRequest)
        val body = assertIs<DynamicValue.ObjectValue>(request.body)
        assertEquals(DynamicValue.StringValue("Ada"), body.value["name"])
        assertEquals(DynamicValue.StringValue("environment-42"), body.value["environment"])
        assertEquals(DynamicValue.StringValue("old"), body.value["oldReference"])
    }

    @Test
    fun nonNavigatingRequestReturnsCompletedResult() = runTest {
        var capturedResponseMode: String? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                capturedResponseMode = request.responseMode
                return Result.success(DynamicResponse(204, "NO_CONTENT", "Completed", null, "response-2"))
            }
        }
        val result = RequestActionHandler(repository).handle(
            DynamicAction(
                type = "request",
                payload = mapOf(
                    "endpoint" to DynamicValue.StringValue("/save"),
                    "responseMode" to DynamicValue.StringValue("none"),
                ),
            ),
            emptyMap(),
            emptyMap(),
            emptyMap(),
        )
        assertEquals("none", capturedResponseMode)
        assertIs<DynamicActionResult.Completed>(result)
    }


    @Test
    fun missingResponseModeDefaultsToNoneWithoutNavigation() = runTest {
        var observedMode: String? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                observedMode = request.responseMode
                return Result.success(DynamicResponse(200, "OK", "Done", null, null))
            }
        }
        val result = RequestActionHandler(repository).handle(
            DynamicAction("request", mapOf("endpoint" to DynamicValue.StringValue("/submit"))),
            emptyMap(), emptyMap(), emptyMap(),
        )
        assertEquals("none", observedMode)
        assertIs<DynamicActionResult.Completed>(result)
    }

    @Test
    fun invalidResponseModeIsRejectedBeforeHttpCall() = runTest {
        var calls = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                calls++
                return Result.success(DynamicResponse(200, "OK", "Done", null, null))
            }
        }
        assertFailsWith<IllegalStateException> {
            RequestActionHandler(repository).handle(
                DynamicAction("request", mapOf(
                    "endpoint" to DynamicValue.StringValue("/submit"),
                    "responseMode" to DynamicValue.StringValue("unsupported"),
                )),
                emptyMap(), emptyMap(), emptyMap(),
            )
        }
        assertEquals(0, calls)
    }

    @Test
    fun requestPopModeIsRejectedBeforeHttpCall() = runTest {
        var calls = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                calls++
                return Result.success(DynamicResponse(200, "OK", "Done", null, null))
            }
        }
        assertFailsWith<IllegalStateException> {
            RequestActionHandler(repository).handle(
                DynamicAction("request", mapOf(
                    "endpoint" to DynamicValue.StringValue("/submit"),
                    "responseMode" to DynamicValue.StringValue("destination"),
                    "mode" to DynamicValue.StringValue("pop"),
                )),
                emptyMap(), emptyMap(), emptyMap(),
            )
        }
        assertEquals(0, calls)
    }

    @Test
    fun missingRetainPathRejectsInsteadOfSilentlyStoringNull() = runTest {
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(
                    200, "OK", "Done", null, null,
                    dataPayload = DynamicValue.ObjectValue(emptyMap()),
                ))
        }
        assertFailsWith<IllegalStateException> {
            RequestActionHandler(repository).handle(
                DynamicAction("request", mapOf(
                    "endpoint" to DynamicValue.StringValue("/submit"),
                    "responseMode" to DynamicValue.StringValue("none"),
                    "retain" to DynamicValue.ObjectValue(mapOf(
                        "reference" to DynamicValue.StringValue("data.missing"),
                    )),
                )),
                emptyMap(), emptyMap(), emptyMap(),
            )
        }
    }

    @Test
    fun explicitNullRetainedValueRemainsNull() = runTest {
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                Result.success(DynamicResponse(
                    200, "OK", "Done", null, null,
                    dataPayload = DynamicValue.ObjectValue(mapOf(
                        "reference" to DynamicValue.NullValue,
                    )),
                ))
        }
        val result = RequestActionHandler(repository).handle(
            DynamicAction("request", mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "responseMode" to DynamicValue.StringValue("none"),
                "retain" to DynamicValue.ObjectValue(mapOf(
                    "reference" to DynamicValue.StringValue("data.reference"),
                )),
            )),
            emptyMap(), emptyMap(), emptyMap(),
        )
        assertEquals(
            DynamicValue.NullValue,
            assertIs<DynamicActionResult.StateChanged>(result).changes["reference"],
        )
    }

    @Test
    fun malformedRetainMappingIsRejectedBeforeHttp() = runTest {
        var calls = 0
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                calls++
                return Result.success(DynamicResponse(200, "OK", "Done", null, null))
            }
        }
        assertFailsWith<IllegalArgumentException> {
            RequestActionHandler(repository).handle(
                DynamicAction("request", mapOf(
                    "endpoint" to DynamicValue.StringValue("/submit"),
                    "retain" to DynamicValue.ObjectValue(mapOf(
                        "reference" to DynamicValue.NumberValue(1.0))),
                )),
                emptyMap(), emptyMap(), emptyMap(),
            )
        }
        assertEquals(0, calls)
    }

    private fun ref(type: String, value: String) =
        DynamicValue.ObjectValue(mapOf(type to DynamicValue.StringValue(value)))
}
