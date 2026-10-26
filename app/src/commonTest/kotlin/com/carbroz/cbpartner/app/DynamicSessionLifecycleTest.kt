package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.presentation.ToastPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.ToastProgress
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Isolated G1-B handoff and retirement assertions; Compose/Koin host requires smoke testing. */
class DynamicSessionLifecycleTest {
    @Test
    fun initialHandoffOwnsNavigationAndRuntimeWithoutSharingAnotherSession() {
        val first = DynamicSession("bootstrap-a", destination("first"))
        first.navigationState.push(destination("second"))
        first.runtimeState.set("status", DynamicValue.StringValue("retained"))

        assertEquals("bootstrap-a", first.diagnosticSessionId)
        assertEquals("second", first.navigationState.currentDestination.screenId)
        val another = DynamicSession("bootstrap-b", destination("first"))
        assertNotSame(first.navigationState, another.navigationState)
        assertNotSame(first.runtimeState, another.runtimeState)
        assertEquals("first", another.navigationState.currentDestination.screenId)
        assertEquals(DynamicValue.NullValue, another.runtimeState.get("status"))
    }

    @Test
    fun temporaryConsumerDetachPreservesSessionAndOnlyRetirementDiscardsWork() {
        val session = DynamicSession("bootstrap-a", destination("first"))
        assertTrue(session.commit(emptyMap(), listOf(DynamicActionEffect.ExternalUri("https://example.invalid/pending"))))
        val first = session.attachConsumer()
        session.detachConsumer(first)
        val second = session.attachConsumer()
        assertEquals(DynamicActionEffect.ExternalUri("https://example.invalid/pending"), session.claimNext(second)?.effect)
        session.close()
        session.close()
        assertNull(session.claimNext(second))
        assertFalse(session.commit(emptyMap(), listOf(DynamicActionEffect.ExternalUri("https://example.invalid/pending"))))
    }

    @Test
    fun loginNavigationAndToastAreOneOrderedBatchAfterStateCommit() {
        val session = DynamicSession("session-a", destination("partner_login"))
        val dest = destination("partner_otp")
        val toast = DynamicActionEffect.Toast(
            presentation = ToastPresentation(
                id = "otp_sent_success",
                placement = "top",
                durationMillis = 5000,
                background = DynamicBackground.Solid("#FFFFFF"),
                shape = DynamicShape.RoundedCorner(20.0),
                padding = DynamicPadding(16.0, 14.0, 16.0, 14.0),
                progress = ToastProgress(4.0, "#FFFFFF", "#079553"),
                components = listOf(StackComponent("toast")),
            ),
            bindings = mapOf(
                "countryCode" to DynamicValue.StringValue("+91"),
                "mobileNumber" to DynamicValue.StringValue("9876543210"),
            ),
            contextValues = emptyMap(),
            stateValues = mapOf("challengeId" to DynamicValue.StringValue("new-challenge")),
        )
        val accepted = session.commit(
            mapOf("challengeId" to DynamicValue.StringValue("new-challenge")),
            listOf(
                DynamicActionEffect.Navigate(
                    dest,
                    com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult.NavigationOperation.Push,
                ),
                toast,
            ),
        )
        assertTrue(accepted)
        assertEquals(
            DynamicValue.StringValue("new-challenge"),
            session.runtimeState.get("challengeId"),
        )
        val lease = session.attachConsumer()
        val navigate = session.claimNext(lease)!!
        assertEquals(0, navigate.effectIndex)
        assertEquals(dest, (navigate.effect as DynamicActionEffect.Navigate).destination)
        assertTrue(session.markDispatchStarted(navigate))
        assertTrue(session.acknowledge(navigate, DynamicSession.EffectOutcome.Applied))
        val notification = session.claimNext(lease)!!
        assertEquals(navigate.batchId, notification.batchId)
        assertEquals(1, notification.effectIndex)
        assertEquals(toast, notification.effect)
        assertTrue(session.markDispatchStarted(notification))
        assertTrue(session.acknowledge(notification, DynamicSession.EffectOutcome.Applied))
        assertNull(session.claimNext(lease))
        session.close()
    }

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}
