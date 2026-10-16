package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Owns a logical dynamic session and accepted, ordered effect progress.
 *
 * Does not execute actions, perform navigation, open external URIs or retain DI
 * dependencies. An active application consumer dispatches claimed effects.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class DynamicSession(
    val diagnosticSessionId: String,
    initialDestination: DynamicDestination,
) {
    val navigationState = DynamicNavigationState(initialDestination, diagnosticSessionId)
    val runtimeState = DynamicRuntimeState()

    private val _workRevision = MutableStateFlow(0L)
    val workRevision: StateFlow<Long> = _workRevision

    // This lock covers only short, non-suspending state changes. Never dispatch an
    // effect or invoke application callbacks while holding it.
    private val busy = AtomicBoolean(false)
    private var closed = false
    private var nextLease = 0L
    private var currentLease: Long? = null
    private var nextBatch = 0L
    private val batches = ArrayDeque<Batch>()
    private var reservation: Reservation? = null

    private class Batch(
        val id: Long,
        val effects: List<DynamicActionEffect>,
        val trace: TraceContext?,
        var index: Int = 0,
    )

    private class Reservation(
        val claim: ClaimedEffect,
        var started: Boolean = false,
    )

    private inline fun <T> locked(block: () -> T): T {
        while (!busy.compareAndSet(false, true)) {
            // Non-suspending cross-platform commit/lease serialization.
        }
        try {
            return block()
        } finally {
            busy.store(false)
        }
    }

    private fun changed() {
        _workRevision.value += 1L
    }

    /** Accepts state changes and a copied effect batch without suspension. */
    fun commit(
        changes: Map<String, DynamicValue>,
        effects: List<DynamicActionEffect>,
        trace: TraceContext? = null,
    ): Boolean = locked {
        if (closed) return@locked false
        val copied = effects.toList()
        runtimeState.setAll(changes)
        if (copied.isNotEmpty()) {
            batches.addLast(Batch(++nextBatch, copied, trace))
            changed()
        }
        true
    }

    /** Grants the only active consumer lease, replacing any previous one. */
    fun attachConsumer(): Long = locked {
        if (closed) return@locked 0L
        releaseReservation()
        currentLease = ++nextLease
        changed()
        nextLease
    }

    /** Reserves only the first uncompleted effect for the active consumer. */
    fun claimNext(leaseId: Long): ClaimedEffect? = locked {
        if (closed || currentLease != leaseId) return@locked null
        reservation?.let { return@locked it.claim }
        val batch = batches.firstOrNull() ?: return@locked null
        ClaimedEffect(
            leaseId = leaseId,
            batchId = batch.id,
            effectIndex = batch.index,
            effect = batch.effects[batch.index],
            trace = batch.trace,
        ).also { reservation = Reservation(it) }
    }

    /** Records the only permitted dispatch attempt before platform invocation. */
    fun markDispatchStarted(claim: ClaimedEffect): Boolean = locked {
        val active = reservation
        if (closed || currentLease != claim.leaseId ||
            active?.claim != claim || active.started
        ) return@locked false
        active.started = true
        true
    }

    /** Completes an attempted effect; rejection still permits later effects. */
    fun acknowledge(claim: ClaimedEffect, outcome: EffectOutcome): Boolean = locked {
        val active = reservation
        if (closed || currentLease != claim.leaseId ||
            active?.claim != claim || !active.started
        ) return@locked false
        // The caller renders any generic UI-safe error for Rejected.
        advance()
        true
    }

    /** Revokes the active lease without replaying a dispatch-started effect. */
    fun detachConsumer(leaseId: Long) = locked {
        if (!closed && currentLease == leaseId) {
            releaseReservation()
            currentLease = null
            changed()
        }
    }

    /** Final retirement; transient consumer detachment must not call close. */
    fun close() = locked {
        if (!closed) {
            closed = true
            currentLease = null
            reservation = null
            batches.clear()
            changed()
        }
    }

    private fun releaseReservation() {
        if (reservation?.started == true) advance() // Ambiguous attempt: no replay.
        else reservation = null
    }

    private fun advance() {
        val batch = batches.firstOrNull() ?: return
        batch.index += 1
        if (batch.index == batch.effects.size) batches.removeFirst()
        reservation = null
        changed()
    }

    data class ClaimedEffect(
        val leaseId: Long,
        val batchId: Long,
        val effectIndex: Int,
        val effect: DynamicActionEffect,
        val trace: TraceContext?,
    )

    enum class EffectOutcome { Applied, Rejected }
}
