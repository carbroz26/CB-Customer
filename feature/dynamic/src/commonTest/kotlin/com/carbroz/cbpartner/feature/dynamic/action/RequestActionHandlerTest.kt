package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RequestActionHandlerTest {
    @Test
    fun verifyRequestResolvesBindingContextAndPreviousResponse() = runTest {
        var capturedRequest: DynamicRequest? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                capturedRequest = request
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "SUCCESS",
                        message = "Verified",
                        data = null,
                        traceId = "verify",
                        destination = DynamicDestination(
                            screenId = "partner_dashboard",
                            templateId = "tpl_dashboard",
                            templateType = "form_template",
                            endpoint = "/api/v1/partner/screen/dashboard",
                        ),
                    ),
                )
            }
        }

        val navigation = DynamicNavigationState(
            DynamicDestination(
                screenId = "partner_otp",
                templateId = "tpl_P102O",
                templateType = "form_template",
                endpoint = "/api/v1/partner/screen/auth_otp",
            ),
        )

        val action = DynamicAction(
            type = "request",
            payload = mapOf(
                "method" to DynamicValue.StringValue("POST"),
                "endpoint" to DynamicValue.StringValue("/api/v1/partner/auth/verify_otp"),
                "authentication" to DynamicValue.StringValue("NONE"),
                "validate" to DynamicValue.BooleanValue(true),
                "responseMode" to DynamicValue.StringValue("destination"),
                "body" to DynamicValue.ObjectValue(
                    mapOf(
                        "challengeId" to DynamicValue.ObjectValue(
                            mapOf("$response" to DynamicValue.StringValue("data.challengeId")),
                        ),
                        "phoneNumber" to DynamicValue.ObjectValue(
                            mapOf("$context" to DynamicValue.StringValue("authFlow.phoneNumber")),
                        ),
                        "otp" to DynamicValue.ObjectValue(
                            mapOf("$binding" to DynamicValue.StringValue("otp")),
                        ),
                    ),
                ),
            ),
        )

        val previousResponse = DynamicValue.ObjectValue(
            mapOf(
                "data" to DynamicValue.ObjectValue(
                    mapOf("challengeId" to DynamicValue.StringValue("challenge-123")),
                ),
            ),
        )

        RequestActionHandler(repository).handle(
            action = action,
            navigation = navigation,
            bindings = mapOf("otp" to "123456"),
            context = mapOf("authFlow.phoneNumber" to "+919876543210"),
            previousResponse = previousResponse,
        )

        val request = requireNotNull(capturedRequest)
        val body = assertIs<DynamicValue.ObjectValue>(request.body)

        assertEquals("/api/v1/partner/auth/verify_otp", request.endpoint)
        assertEquals("POST", request.method)
        assertEquals("NONE", request.authentication)
        assertEquals(
            DynamicValue.StringValue("challenge-123"),
            body.value["challengeId"],
        )
        assertEquals(
            DynamicValue.StringValue("+919876543210"),
            body.value["phoneNumber"],
        )
        assertEquals(
            DynamicValue.StringValue("123456"),
            body.value["otp"],
        )
    }
}
