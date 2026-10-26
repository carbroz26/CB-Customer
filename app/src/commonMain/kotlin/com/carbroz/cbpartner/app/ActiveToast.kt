package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * App-root transient Toast identity with a monotonic start shared across rotations.
 * Owns no networking, queues, renderers, or business actions.
 */
internal class ActiveToast(
    val effect: DynamicActionEffect.Toast,
    private val startedAt: TimeMark = TimeSource.Monotonic.markNow(),
) {
    fun progressFraction(): Float =
        (startedAt.elapsedNow().inWholeMilliseconds.toDouble() /
            effect.presentation.durationMillis.toDouble()).coerceIn(0.0, 1.0).toFloat()
}
