package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.ExecuteDynamicActionUseCase
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.LoadDynamicScreenUseCase
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
    fun requestResolvesBindingAndContextOutsideStore() = runTest {
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
                        code = "SUCCESS",
                        message = "Request completed",
                        data = null,
                        traceId = "test",
                        nextScreen = com.carbroz.cbpartner.domain.model.dynamic.DynamicNextScreen(
                            screenId = "next",
                            templateId = "tpl_next",
                            templateType = "stack_template",
                            endpoint = "/next",
                        ),
                    ),
                )
            }
        }

        val destination = DynamicDestination(
            screenId = "current",
            templateId = "tpl_current",
            templateType = "stack_template",
            endpoint = "/current",
        )
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            executeDynamicAction = ExecuteDynamicActionUseCase(repository),
            destination = destination,
            contextProvider = { mapOf("deviceId" to "test-device") },
            scope = CoroutineScope(dispatcher),
        )

        store.accept(DynamicIntent.ValueChanged("firstName", "John"))
        store.accept(
            DynamicIntent.Action(
                DynamicAction(
                    type = "request",
                    payload = mapOf(
                        "method" to DynamicValue.StringValue("POST"),
                        "endpoint" to DynamicValue.StringValue("/submit"),
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
            ),
        )

        advanceUntilIdle()

        val body = assertNotNull(capturedBody)
        val json = Json.parseToJsonElement(body).jsonObject
        assertEquals("John", json["firstName"]?.jsonPrimitive?.content)
        assertEquals("test-device", json["deviceId"]?.jsonPrimitive?.content)
        val effect = store.effects.value
        assertNotNull(effect)
        assertEquals("next", (effect as DynamicEffect.Navigate).destination.screenId)
        store.close()
    }
}
