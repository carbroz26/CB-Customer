package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
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
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicStoreTest {
    @Test
    fun requestResolvesBindingAndContextOutsideStore() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var capturedBody: DynamicValue? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(
                request: DynamicRequest,
            ): Result<DynamicResponse> {
                capturedBody = request.body
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "SUCCESS",
                        message = "Request completed",
                        data = null,
                        traceId = "test",
                        destination = DynamicDestination(
                            screenId = "next",
                            templateId = "tpl_P103N",
                            templateType = "stack_template",
                            endpoint = "/next",
                        ),
                    ),
                )
            }
        }

        val currentDestination = DynamicDestination(
            screenId = "current",
            templateId = "tpl_P100C",
            templateType = "stack_template",
            endpoint = "/current",
        )
        val navigation = DynamicNavigationState(currentDestination)
        val actionExecutor = DynamicActionExecutor(
            handlers = listOf(RequestActionHandler(repository)),
        )

        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = actionExecutor,
            destination = currentDestination,
            contextProvider = { mapOf("deviceId" to "test-device") },
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.ValueChanged("firstName", "John"))
        store.accept(
            DynamicIntent.Action(
                action = DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "method" to DynamicValue.StringValue("POST"),
                        "endpoint" to DynamicValue.StringValue("/submit"),
                        "authentication" to DynamicValue.StringValue("NONE"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf(
                                "firstName" to DynamicValue.ObjectValue(
                                    mapOf("${'$'}binding" to DynamicValue.StringValue("firstName")),
                                ),
                                "deviceId" to DynamicValue.ObjectValue(
                                    mapOf("${'$'}context" to DynamicValue.StringValue("deviceId")),
                                ),
                            ),
                        ),
                    ),
                ),
                navigation = navigation,
            ),
        )

        advanceUntilIdle()

        val body = assertNotNull(capturedBody) as DynamicValue.ObjectValue
        assertEquals(
            DynamicValue.StringValue("John"),
            body.value["firstName"],
        )
        assertEquals(
            DynamicValue.StringValue("test-device"),
            body.value["deviceId"],
        )
        assertEquals("next", navigationDestination(navigation).screenId)
        store.close()
    }


    @Test
    fun verifyRequestResolvesChallengeIdFromRuntimeContextAcrossSendOtpDestinationTransition() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var verifyBody: DynamicValue? = null
        val sendOtpDestination = DynamicDestination(
            screenId = "partner_otp",
            templateId = "tpl_P102O",
            templateType = "form_template",
            endpoint = "/api/v1/partner/screen/auth_otp",
        )
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
                if (request.endpoint == "/api/v1/partner/auth/verify_otp") {
                    verifyBody = request.body
                    return Result.success(
                        DynamicResponse(200, "SUCCESS", "Verified", null, "verify"),
                    )
                }
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "SUCCESS",
                        message = "OTP sent",
                        data = null,
                        traceId = "send-otp",
                        dataPayload = DynamicValue.ObjectValue(
                            mapOf("challengeId" to DynamicValue.StringValue("challenge-123")),
                        ),
                        destination = sendOtpDestination,
                    ),
                )
            }
        }

        val loginDestination = DynamicDestination(
            screenId = "partner_login",
            templateId = "tpl_P101L",
            templateType = "form_template",
            endpoint = "/api/v1/partner/screen/auth_login",
        )
        val navigation = DynamicNavigationState(loginDestination)
        val actionExecutor = DynamicActionExecutor(
            handlers = listOf(RequestActionHandler(repository)),
        )
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = actionExecutor,
            destination = loginDestination,
            contextProvider = {
                mapOf(
                    "deviceId" to "test-device",
                    "authFlow.phoneNumber" to "9876543210",
                )
            },
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.ValueChanged("mobileNumber", "9876543210"))
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/api/v1/partner/auth/send_otp"),
                        "method" to DynamicValue.StringValue("POST"),
                        "authentication" to DynamicValue.StringValue("NONE"),
                        "responseMode" to DynamicValue.StringValue("destination"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf(
                                "phoneNumber" to DynamicValue.ObjectValue(
                                    mapOf("\$binding" to DynamicValue.StringValue("mobileNumber")),
                                ),
                                "deviceId" to DynamicValue.ObjectValue(
                                    mapOf("\$context" to DynamicValue.StringValue("deviceId")),
                                ),
                            ),
                        ),
                    ),
                ),
                navigation,
            ),
        )
        advanceUntilIdle()
        assertEquals("partner_otp", navigation.currentDestination.screenId)

        store.updateDestination(sendOtpDestination)
        advanceUntilIdle()

        store.accept(DynamicIntent.ValueChanged("otp", "123456"))
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "endpoint" to DynamicValue.StringValue("/api/v1/partner/auth/verify_otp"),
                        "method" to DynamicValue.StringValue("POST"),
                        "authentication" to DynamicValue.StringValue("NONE"),
                        "responseMode" to DynamicValue.StringValue("none"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf(
                                "challengeId" to DynamicValue.ObjectValue(
                                    mapOf("\$response" to DynamicValue.StringValue("data.challengeId")),
                                ),
                                "phoneNumber" to DynamicValue.ObjectValue(
                                    mapOf("\$context" to DynamicValue.StringValue("authFlow.phoneNumber")),
                                ),
                                "otp" to DynamicValue.ObjectValue(
                                    mapOf("\$binding" to DynamicValue.StringValue("otp")),
                                ),
                                "deviceId" to DynamicValue.ObjectValue(
                                    mapOf("\$context" to DynamicValue.StringValue("deviceId")),
                                ),
                            ),
                        ),
                    ),
                ),
                navigation,
            ),
        )
        advanceUntilIdle()

        val body = assertNotNull(verifyBody) as DynamicValue.ObjectValue
        assertEquals(DynamicValue.StringValue("challenge-123"), body.value["challengeId"])
        assertEquals(DynamicValue.StringValue("9876543210"), body.value["phoneNumber"])
        assertEquals(DynamicValue.StringValue("123456"), body.value["otp"])
        assertEquals(DynamicValue.StringValue("test-device"), body.value["deviceId"])
        store.close()
    }

    private fun navigationDestination(navigation: DynamicNavigationState): DynamicDestination {
        return navigation.currentDestination
    }
}
