package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
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
        assertTrue(session.commit(emptyMap(), listOf(DynamicActionEffect.Dismiss)))
        val first = session.attachConsumer()
        session.detachConsumer(first)
        val second = session.attachConsumer()
        assertEquals(DynamicActionEffect.Dismiss, session.claimNext(second)?.effect)
        session.close()
        session.close()
        assertNull(session.claimNext(second))
        assertFalse(session.commit(emptyMap(), listOf(DynamicActionEffect.Dismiss)))
    }

    private fun destination(id: String) =
        DynamicDestination(id, "tpl_$id", "form_template", "/$id")
}
