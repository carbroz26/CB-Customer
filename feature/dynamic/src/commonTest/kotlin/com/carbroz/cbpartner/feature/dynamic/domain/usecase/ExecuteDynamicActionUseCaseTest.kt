package com.carbroz.cbpartner.feature.dynamic.domain.usecase

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicNextScreen
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionHandler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExecuteDynamicActionUseCaseTest {
    @Test
    fun requestResolvesBindingAndContextBeforeRepository() = runTest {
        var capturedBody: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(
                destination: DynamicDestination,
                body: DynamicValue?,
            ): Result<DynamicResponse> {
                capturedBody = body
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "SUCCESS",
                        message = "Request completed",
                        data = null,
                        traceId = "test",
                        nextScreen = DynamicNextScreen(
                            screenId = "partner_next",
                            templateId = "tpl_next",
                            templateType = "stack_template",
                            endpoint = "/next",
                        ),
                    ),
                )
            }
        }

        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/submit"),
                "method" to DynamicValue.StringValue("POST"),
                "body" to DynamicValue.ObjectValue(
                    mapOf(
                        "name" to DynamicValue.ObjectValue(
                            mapOf("${'$'}binding" to DynamicValue.StringValue("name")),
                        ),
                        "deviceId" to DynamicValue.ObjectValue(
                            mapOf("${'$'}context" to DynamicValue.StringValue("deviceId")),
                        ),
                    ),
                ),
            ),
        )

        val result = ExecuteDynamicActionUseCase(repository, DynamicActionHandler())(
            action = action,
            bindings = mapOf("name" to "John"),
            context = mapOf("deviceId" to "device-1"),
        ).getOrThrow()

        val body = assertIs<DynamicValue.ObjectValue>(capturedBody)
        assertEquals(DynamicValue.StringValue("John"), body.value["name"])
        assertEquals(DynamicValue.StringValue("device-1"), body.value["deviceId"])

        val navigation = assertIs<DynamicActionResult.Navigate>(result)
        assertEquals("partner_next", navigation.destination.screenId)
    }

    @Test
    fun requestWithNoneResponseModeCompletesWithoutNavigation() = runTest {
        val response = DynamicResponse(
            status = 200,
            code = "OTP_SENT",
            message = "OTP sent",
            data = null,
            traceId = "trace-1",
            dataPayload = DynamicValue.ObjectValue(
                mapOf("challengeId" to DynamicValue.StringValue("challenge-1")),
            ),
        )
        val repository = object : DynamicRepository {
            override suspend fun fetch(
                destination: DynamicDestination,
                body: DynamicValue?,
            ): Result<DynamicResponse> = Result.success(response)
        }

        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/send-otp"),
                "method" to DynamicValue.StringValue("POST"),
                "responseMode" to DynamicValue.StringValue("none"),
            ),
        )

        val result = ExecuteDynamicActionUseCase(repository, DynamicActionHandler())(
            action = action,
            bindings = emptyMap(),
            context = emptyMap(),
        ).getOrThrow()

        val completed = assertIs<DynamicActionResult.Completed>(result)
        assertEquals(response, completed.response)
    }

    @Test
    fun requestCanResolvePreviousResponseValue() = runTest {
        var capturedBody: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(
                destination: DynamicDestination,
                body: DynamicValue?,
            ): Result<DynamicResponse> {
                capturedBody = body
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "SUCCESS",
                        message = "Verified",
                        data = null,
                        traceId = "trace-2",
                    ),
                )
            }
        }

        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "endpoint" to DynamicValue.StringValue("/verify-otp"),
                "method" to DynamicValue.StringValue("POST"),
                "responseMode" to DynamicValue.StringValue("none"),
                "body" to DynamicValue.ObjectValue(
                    mapOf(
                        "challengeId" to DynamicValue.ObjectValue(
                            mapOf("${'$'}response" to DynamicValue.StringValue("data.challengeId")),
                        ),
                    ),
                ),
            ),
        )

        ExecuteDynamicActionUseCase(repository, DynamicActionHandler())(
            action = action,
            bindings = emptyMap(),
            context = emptyMap(),
            previousResponse = DynamicValue.ObjectValue(
                mapOf(
                    "data" to DynamicValue.ObjectValue(
                        mapOf("challengeId" to DynamicValue.StringValue("challenge-1")),
                    ),
                ),
            ),
        ).getOrThrow()

        val body = assertIs<DynamicValue.ObjectValue>(capturedBody)
        assertEquals(
            DynamicValue.StringValue("challenge-1"),
            body.value["challengeId"],
        )
    }
}
