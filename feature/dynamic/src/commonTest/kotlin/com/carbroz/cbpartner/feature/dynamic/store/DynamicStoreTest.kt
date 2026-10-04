package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DynamicStoreTest {
    @Test
    fun requestResolvesPhoneBindingAndAddsDeviceIdToJsonBody() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        var capturedBody: String? = null
        val repository = object : DynamicRepository {
            override suspend fun fetch(
                destination: DynamicDestination,
                headers: Map<String, String>,
                body: String?,
            ): Result<DynamicResponse> {
                capturedBody = body
                return Result.success(
                    DynamicResponse(
                        status = 200,
                        code = "OTP_SENT",
                        message = "OTP sent",
                        data = null,
                        traceId = "test",
                        nextScreen = null,
                    ),
                )
            }
        }

        val store = DynamicStore(
            repository = repository,
            destination = DynamicDestination(
                screenId = "partner_login",
                templateId = "tpl_login",
                templateType = "form_template",
                endpoint = "/api/v1/partner/auth/login",
            ),
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.ValueChanged("mobileNumber", "test-phone"))
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "method" to DynamicValue.StringValue("POST"),
                        "endpoint" to DynamicValue.StringValue("/api/v1/partner/auth/send_otp"),
                        "body" to DynamicValue.ObjectValue(
                            mapOf(
                                "phoneNumber" to DynamicValue.ObjectValue(
                                    mapOf(
                                        "${'$'}binding" to DynamicValue.StringValue("mobileNumber"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )

        advanceUntilIdle()

        val body = assertNotNull(capturedBody)
        val json = Json.parseToJsonElement(body).jsonObject
        assertEquals("test-phone", json["phoneNumber"]?.jsonPrimitive?.content)
        assertEquals("bruno-test-device-123", json["deviceId"]?.jsonPrimitive?.content)
        store.close()
    }
}
