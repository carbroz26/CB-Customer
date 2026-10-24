package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicCountdown
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.RequestActionHandler
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DynamicCountdownStoreTest {
    @Test
    fun guardsPrematureResendThenRetainsNewChallengeAndRestarts() = runTest {
        val runtime = DynamicRuntimeState()
        runtime.setAll(mapOf(
            "resendAfterSeconds" to DynamicValue.NumberValue(10.0),
            "challengeId" to DynamicValue.StringValue("old"),
        ))
        var requests = 0
        val screen = DynamicScreen(
            screenId = "root",
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = FormTemplate(id = "tpl_root"),
            theme = null,
            countdowns = listOf(DynamicCountdown("resend", "resendAfterSeconds")),
        )
        val repository = object : DynamicRepository {
            override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> =
                if (request.endpoint == "/resend") {
                    requests++
                    Result.success(DynamicResponse(
                        200, "SUCCESS", "Resent", null, null,
                        dataPayload = DynamicValue.ObjectValue(mapOf(
                            "challengeId" to DynamicValue.StringValue("new"),
                            "resendAfterSeconds" to DynamicValue.NumberValue(10.0),
                        )),
                    ))
                } else Result.success(DynamicResponse(200, "SUCCESS", "Loaded", screen, null))
        }
        val resend = DynamicAction("request", mapOf(
            "endpoint" to DynamicValue.StringValue("/resend"),
            "method" to DynamicValue.StringValue("POST"),
            "responseMode" to DynamicValue.StringValue("none"),
            "enabledWhen" to DynamicValue.ObjectValue(mapOf(
                "\$state" to DynamicValue.StringValue("countdowns.resend.ready"),
            )),
            "retain" to DynamicValue.ObjectValue(mapOf(
                "challengeId" to DynamicValue.StringValue("data.challengeId"),
                "resendAfterSeconds" to DynamicValue.StringValue("data.resendAfterSeconds"),
            )),
        ))
        val store = DynamicStore(
            loadDynamicScreen = LoadDynamicScreenUseCase(repository),
            actionExecutor = DynamicActionExecutor(listOf(RequestActionHandler(repository))),
            validator = DynamicFormValidator(),
            entry = DynamicNavigationEntry(DynamicDestination(
                "root", "tpl_root", "form_template", "/root",
            )),
            runtimeState = runtime,
            scope = CoroutineScope(StandardTestDispatcher(testScheduler)),
        )
        fun remaining(): String {
            val current = assertIs<DynamicState.Success>(store.state.value).screen
            val countdowns = current.transientState["countdowns"] as DynamicValue.ObjectValue
            val values = countdowns.value["resend"] as DynamicValue.ObjectValue
            return (values.value["remainingText"] as DynamicValue.StringValue).value
        }
        store.accept(DynamicIntent.Load)
        runCurrent()
        assertEquals("00:10", remaining())
        store.accept(DynamicIntent.Action(resend))
        runCurrent()
        assertEquals(0, requests)
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals("00:00", remaining())
        store.accept(DynamicIntent.Action(resend))
        runCurrent()
        assertEquals(1, requests)
        assertEquals(DynamicValue.StringValue("new"), runtime.get("challengeId"))
        assertEquals("00:10", remaining())
        store.accept(DynamicIntent.Action(resend))
        runCurrent()
        assertEquals(1, requests)
        store.close()
    }
}
