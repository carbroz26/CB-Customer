package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicCountdownClockTest {
    @Test
    fun monotonicRemainingSecondsRoundUpAndNeverGoNegative() {
        fun calculate(seconds: Long, elapsedMillis: Long) = DynamicCountdownClock.secondsRemaining(seconds, elapsedMillis)
        assertEquals(10L, calculate(10, 0))
        assertEquals(10L, calculate(10, 1))
        assertEquals(9L, calculate(10, 1000))
        assertEquals(1L, calculate(10, 9001))
        assertEquals(0L, calculate(10, 10_000))
        assertEquals(0L, calculate(10, 20_000))
    }

    @Test
    fun missingDurationFailsClosed() {
        val state = countdownState(mapOf("resend" to null))
        val countdowns = state["countdowns"] as DynamicValue.ObjectValue
        val resend = countdowns.value["resend"] as DynamicValue.ObjectValue
        assertEquals(DynamicValue.StringValue("--:--"), resend.value["remainingText"])
        assertEquals(DynamicValue.BooleanValue(false), resend.value["ready"])
    }
}
