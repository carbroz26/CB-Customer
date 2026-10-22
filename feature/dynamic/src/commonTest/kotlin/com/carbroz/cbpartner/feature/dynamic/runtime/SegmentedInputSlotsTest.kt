package com.carbroz.cbpartner.feature.dynamic.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SegmentedInputSlotsTest {
    @Test
    fun normalizesEmptyAndPartialInputWithoutShifting() {
        assertEquals("______", SegmentedInputSlots.normalize("", 6))
        assertEquals("12____", SegmentedInputSlots.normalize("12", 6))
        assertEquals("1_3___", SegmentedInputSlots.normalize("1_3", 6))
        assertEquals("", SegmentedInputSlots.display("", 6, 0))
        assertEquals("3", SegmentedInputSlots.display("1_3", 6, 2))
    }

    @Test
    fun replacesSelectedSlotAndAdvancesWithoutShifting() {
        val result = SegmentedInputSlots.edit("123456", 6, 2, "9")
        assertEquals("129456", result.value)
        assertEquals(3, result.focusIndex)
        assertEquals("12_456", SegmentedInputSlots.edit("123456", 6, 2, "").value)
    }

    @Test
    fun emptyBackspaceOnlyMovesFocusAndLeavesOtherDigitsUntouched() {
        val result = SegmentedInputSlots.backspaceOnEmpty("12_456", 6, 2)
        assertEquals("12_456", result.value)
        assertEquals(1, result.focusIndex)
    }

    @Test
    fun pasteOverwritesFromSelectedSlotAndIgnoresOverflow() {
        val result = SegmentedInputSlots.edit("123456", 6, 3, "987654")
        assertEquals("123987", result.value)
        assertEquals(5, result.focusIndex)
    }

    @Test
    fun completedRequiresOnlyAsciiDigitsAndExactLength() {
        assertTrue(SegmentedInputSlots.complete("123456", 6))
        assertFalse(SegmentedInputSlots.complete("12_456", 6))
        assertFalse(SegmentedInputSlots.complete("12345", 6))
        assertFalse(SegmentedInputSlots.complete("1234567", 6))
        assertFalse(SegmentedInputSlots.complete("１２３４５６", 6))
    }
}
