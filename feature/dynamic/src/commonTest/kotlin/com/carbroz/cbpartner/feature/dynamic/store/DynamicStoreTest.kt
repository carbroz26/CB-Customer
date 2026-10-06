package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
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
                request: com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest,
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

    private fun navigationDestination(navigation: DynamicNavigationState): DynamicDestination {
        return navigation.currentDestination
    }
}
