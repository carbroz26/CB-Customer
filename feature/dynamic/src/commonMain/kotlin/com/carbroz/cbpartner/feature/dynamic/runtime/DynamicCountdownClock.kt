package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** Entry-owned monotonic countdown calculation, not a mutable decrementing counter. */
internal class DynamicCountdownClock(
    private val durationSeconds: Long,
    private val mark: TimeMark = TimeSource.Monotonic.markNow(),
) {
    fun remainingSeconds(): Long = secondsRemaining(durationSeconds, mark.elapsedNow().inWholeMilliseconds)

    companion object {
        fun secondsRemaining(durationSeconds: Long, elapsedMillis: Long): Long =
            ((durationSeconds * 1000 - elapsedMillis.coerceAtLeast(0)).coerceAtLeast(0) + 999) / 1000
    }
}

/** Computed countdown values live in entry state, never session-retained state. */
internal fun countdownState(clocks: Map<String, DynamicCountdownClock?>): Map<String, DynamicValue> {
    val entries = clocks.mapValues { (_, clock) ->
        val remaining = clock?.remainingSeconds()
        val label = if (remaining == null) "--:--" else {
            (remaining / 60).toString().padStart(2, '0') + ":" +
                (remaining % 60).toString().padStart(2, '0')
        }
        DynamicValue.ObjectValue(mapOf(
            "remainingText" to DynamicValue.StringValue(label),
            "ready" to DynamicValue.BooleanValue(remaining == 0L && clock != null),
        ))
    }
    return mapOf("countdowns" to DynamicValue.ObjectValue(entries))
}
