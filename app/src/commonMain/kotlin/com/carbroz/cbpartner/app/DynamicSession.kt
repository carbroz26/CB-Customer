package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Test-only behavioral scaffold for the owner-frozen G1/G2 API in document 91.
 *
 * The session lifecycle, effect queue, consumer leasing and commit transaction
 * are deliberately NOT implemented. Tests must fail on behavior, not missing symbols.
 * This type is not wired into App or DynamicStore until separate authorization.
 */
internal class DynamicSession(
    val diagnosticSessionId: String,
    initialDestination: DynamicDestination,
) {
    val navigationState: DynamicNavigationState =
        DynamicNavigationState(initialDestination, diagnosticSessionId)
    val runtimeState: DynamicRuntimeState = DynamicRuntimeState()

    private val _workRevision = MutableStateFlow(0L)
    val workRevision: StateFlow<Long> = _workRevision

    // Intentional failing placeholders; no production queue or state patch here.
    fun commit(
        changes: Map<String, DynamicValue>,
        effects: List<DynamicActionEffect>,
        trace: TraceContext? = null,
    ): Boolean = false

    fun attachConsumer(): Long = 0L

    fun claimNext(leaseId: Long): ClaimedEffect? = null

    fun markDispatchStarted(claim: ClaimedEffect): Boolean = false

    fun acknowledge(claim: ClaimedEffect, outcome: EffectOutcome): Boolean = false

    fun detachConsumer(leaseId: Long) = Unit

    fun close() = Unit

    data class ClaimedEffect(
        val leaseId: Long,
        val batchId: Long,
        val effectIndex: Int,
        val effect: DynamicActionEffect,
        val trace: TraceContext?,
    )

    enum class EffectOutcome { Applied, Rejected }
}
