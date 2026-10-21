package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * G2-B deterministic regressions for document 91's session effect contract.
 * No real navigation, network, platform URI dispatch, clock delays or business screens.
 */
class DynamicSessionEffectsTest {

    @Test
    fun committedEffectsAreClaimedInOrderAndAcknowledgedOnce() = runTest {
        val session = session()
        val effects = effects()
        assertTrue(session.commit(emptyMap(), effects))
        val lease = session.attachConsumer()
        effects.forEach { expected ->
            val claim = assertNotNull(session.claimNext(lease))
            assertEquals(expected, claim.effect)
            assertTrue(session.markDispatchStarted(claim))
            assertTrue(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
            assertFalse(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
        }
        assertNull(session.claimNext(lease))
    }

    @Test
    fun pendingEffectsSurviveDetachAndNewConsumerClaimsInOrder() = runTest {
        val session = session()
        val ordered = effects()
        assertTrue(session.commit(emptyMap(), ordered))
        val firstLease = session.attachConsumer()
        session.detachConsumer(firstLease)
        val nextLease = session.attachConsumer()
        assertTrue(firstLease != nextLease)
        ordered.forEach { expected ->
            val claim = assertNotNull(session.claimNext(nextLease))
            assertEquals(expected, claim.effect)
            assertTrue(session.markDispatchStarted(claim))
            assertTrue(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
        }
        assertNull(session.claimNext(nextLease))
    }

    @Test
    fun unstartedClaimCanBeReclaimedButStaleConsumerCannotMutateIt() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val firstLease = session.attachConsumer()
        val firstClaim = assertNotNull(session.claimNext(firstLease))
        session.detachConsumer(firstLease)

        val secondLease = session.attachConsumer()
        val nextClaim = assertNotNull(session.claimNext(secondLease))
        assertEquals(firstClaim.effect, nextClaim.effect)
        assertFalse(session.markDispatchStarted(firstClaim))
        assertFalse(session.acknowledge(firstClaim, DynamicSession.EffectOutcome.Applied))
        assertTrue(session.markDispatchStarted(nextClaim))
        assertTrue(session.acknowledge(nextClaim, DynamicSession.EffectOutcome.Applied))
    }

    @Test
    fun partialBatchResumesOnlyRemainingEffectsAfterReattachment() = runTest {
        val session = session()
        val ordered = effects()
        assertTrue(session.commit(emptyMap(), ordered))
        val oldLease = session.attachConsumer()
        val first = assertNotNull(session.claimNext(oldLease))
        assertEquals(ordered[0], first.effect)
        assertTrue(session.markDispatchStarted(first))
        assertTrue(session.acknowledge(first, DynamicSession.EffectOutcome.Applied))
        session.detachConsumer(oldLease)

        val currentLease = session.attachConsumer()
        assertEquals(ordered[1], assertNotNull(session.claimNext(currentLease)).effect)
    }

    @Test
    fun startedButUnacknowledgedExternalUriIsNotReplayed() = runTest {
        val session = session()
        val external = DynamicActionEffect.ExternalUri("https://example.invalid/test")
        val afterward = DynamicActionEffect.ExternalUri("https://example.invalid/afterward")
        assertTrue(session.commit(emptyMap(), listOf(external, afterward)))
        val oldLease = session.attachConsumer()
        val claim = assertNotNull(session.claimNext(oldLease))
        assertEquals(external, claim.effect)
        assertTrue(session.markDispatchStarted(claim))
        session.detachConsumer(oldLease)

        val nextLease = session.attachConsumer()
        assertEquals(afterward, assertNotNull(session.claimNext(nextLease)).effect)
        assertFalse(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
    }

    @Test
    fun rejectedEffectDoesNotBlockNextEffect() = runTest {
        val session = session()
        val ordered = effects()
        assertTrue(session.commit(emptyMap(), ordered))
        val lease = session.attachConsumer()
        val rejected = assertNotNull(session.claimNext(lease))
        assertTrue(session.markDispatchStarted(rejected))
        assertTrue(session.acknowledge(rejected, DynamicSession.EffectOutcome.Rejected))
        assertEquals(ordered[1], assertNotNull(session.claimNext(lease)).effect)
    }

    @Test
    fun retirementDiscardsPendingEffectsAndRejectsFurtherWork() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val lease = session.attachConsumer()
        session.close()
        session.close()
        assertNull(session.claimNext(lease))
        assertFalse(session.commit(emptyMap(), effects()))
    }

    @Test
    fun commitDoesNotApplyStateWithoutAcceptingEffects() = runTest {
        val session = session()
        val changes = mapOf("transaction.marker" to DynamicValue.BooleanValue(true))
        val accepted = session.commit(changes, effects())
        assertTrue(accepted)
        assertEquals(DynamicValue.BooleanValue(true), session.runtimeState.get("transaction.marker"))
        assertNotNull(session.claimNext(session.attachConsumer()))
    }


    @Test
    fun workRevisionSignalsAcceptedAndCompletedWork() = runTest {
        val session = session()
        val initial = session.workRevision.value
        assertTrue(session.commit(emptyMap(), effects()))
        assertTrue(session.workRevision.value > initial)
        val lease = session.attachConsumer()
        val beforeAck = session.workRevision.value
        val claim = assertNotNull(session.claimNext(lease))
        assertTrue(session.markDispatchStarted(claim))
        assertTrue(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
        assertTrue(session.workRevision.value > beforeAck)
    }

    @Test
    fun consumerReplacementDoesNotReplayStartedEffect() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val first = session.attachConsumer()
        val attempted = assertNotNull(session.claimNext(first))
        assertTrue(session.markDispatchStarted(attempted))
        val second = session.attachConsumer()
        assertTrue(first != second)
        assertEquals(effects()[1], assertNotNull(session.claimNext(second)).effect)
        assertFalse(session.acknowledge(attempted, DynamicSession.EffectOutcome.Applied))
    }

    @Test
    fun emptyEffectCommitStillAppliesStateAndClosedCommitDoesNot() = runTest {
        val session = session()
        val value = DynamicValue.BooleanValue(true)
        assertTrue(session.commit(mapOf("confirmed" to value), emptyList()))
        assertEquals(value, session.runtimeState.get("confirmed"))
        val lease = session.attachConsumer()
        assertNull(session.claimNext(lease))
        session.close()
        assertFalse(session.commit(mapOf("confirmed" to DynamicValue.BooleanValue(false)), effects()))
        assertEquals(value, session.runtimeState.get("confirmed"))
    }

    @Test
    fun acceptedBatchesAreConsumedInCommitOrder() = runTest {
        val session = session()
        val first = DynamicActionEffect.ExternalUri("https://example.invalid/first")
        val second = DynamicActionEffect.ExternalUri("https://example.invalid/second")
        assertTrue(session.commit(emptyMap(), listOf(first)))
        assertTrue(session.commit(emptyMap(), listOf(second)))
        val lease = session.attachConsumer()
        for (effect in listOf(first, second)) {
            val claim = assertNotNull(session.claimNext(lease))
            assertEquals(effect, claim.effect)
            assertTrue(session.markDispatchStarted(claim))
            assertTrue(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
        }
        assertNull(session.claimNext(lease))
    }


    @Test
    fun detachedOldConsumerCannotInvalidateReplacementLease() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val oldLease = session.attachConsumer()
        val replacement = session.attachConsumer()
        session.detachConsumer(oldLease)
        val claim = assertNotNull(session.claimNext(replacement))
        assertEquals(effects().first(), claim.effect)
        assertTrue(session.markDispatchStarted(claim))
        assertTrue(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
    }

    @Test
    fun noDispatchMayBeAcknowledgedBeforeItStarts() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val lease = session.attachConsumer()
        val claim = assertNotNull(session.claimNext(lease))
        assertFalse(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
        assertEquals(claim, session.claimNext(lease))
        assertTrue(session.markDispatchStarted(claim))
        assertFalse(session.markDispatchStarted(claim))
    }

    @Test
    fun closeInvalidatesStartedClaimAndConsumerLease() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val lease = session.attachConsumer()
        val claim = assertNotNull(session.claimNext(lease))
        assertTrue(session.markDispatchStarted(claim))
        session.close()
        assertFalse(session.acknowledge(claim, DynamicSession.EffectOutcome.Applied))
        assertFalse(session.markDispatchStarted(claim))
        assertEquals(0L, session.attachConsumer())
        assertNull(session.claimNext(lease))
    }

    @Test
    fun drainAfterRevisionChangeClaimsEveryAcceptedEffect() = runTest {
        val session = session()
        val lease = session.attachConsumer()
        val observedRevision = session.workRevision.value
        val ordered = effects()
        assertTrue(session.commit(emptyMap(), ordered))
        assertTrue(session.workRevision.value > observedRevision)
        val claimed = mutableListOf<DynamicActionEffect>()
        while (true) {
            val next = session.claimNext(lease) ?: break
            claimed.add(next.effect)
            assertTrue(session.markDispatchStarted(next))
            assertTrue(session.acknowledge(next, DynamicSession.EffectOutcome.Applied))
        }
        assertEquals(ordered, claimed)
    }

    @Test
    fun copiedClaimCannotStartOrAcknowledgeAnotherReservation() = runTest {
        val session = session()
        assertTrue(session.commit(emptyMap(), effects()))
        val lease = session.attachConsumer()
        val issued = assertNotNull(session.claimNext(lease))
        val copied = issued.copy()
        assertEquals(issued, copied)
        assertFalse(session.markDispatchStarted(copied))
        assertTrue(session.markDispatchStarted(issued))
        assertFalse(session.acknowledge(copied, DynamicSession.EffectOutcome.Applied))
        assertTrue(session.acknowledge(issued, DynamicSession.EffectOutcome.Applied))
        assertEquals(effects()[1], assertNotNull(session.claimNext(lease)).effect)
    }

    private fun session() = DynamicSession("g2-test-session", destination("root"))

    private fun destination(id: String) =
        DynamicDestination(id, "template_$id", "form_template", "/$id")

    private fun effects(): List<DynamicActionEffect> = listOf(
        DynamicActionEffect.ExternalUri("https://example.invalid/first"),
        DynamicActionEffect.ExternalUri("https://example.invalid/placeholder"),
        DynamicActionEffect.ExternalUri("https://example.invalid/last"),
    )
}
