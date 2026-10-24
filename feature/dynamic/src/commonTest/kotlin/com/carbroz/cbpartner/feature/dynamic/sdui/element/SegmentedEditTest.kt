package com.carbroz.cbpartner.feature.dynamic.sdui.element

import kotlin.test.Test
import kotlin.test.assertEquals

/** Pure slot editing of one Dynamic binding; no screen-specific OTP rules. */
class SegmentedEditTest {
    @Test
    fun typingFillsNextCellAndSelectedDigitCanBeReplacedWithoutShifting() {
        assertEquals(SegmentedEdit("1", 1), editSegmentedValue("", 0, "1", 6))
        assertEquals(SegmentedEdit("12", 2), editSegmentedValue("1", 1, "2", 6))
        assertEquals(SegmentedEdit("19", 2), editSegmentedValue("12", 1, "9", 6))
        assertEquals(SegmentedEdit("129", 3), editSegmentedValue("12", 1, "29", 6))
    }

    @Test
    fun clearingASelectedCellLeavesOtherDigitsInTheirSlots() {
        assertEquals(SegmentedEdit("1 3456", 1), editSegmentedValue("123456", 1, "", 6))
        assertEquals(SegmentedEdit("12 456", 2), editSegmentedValue("123456", 2, "", 6))
    }

    @Test
    fun pasteOverwritesOnlyFromSelectedPositionAndIgnoresExcess() {
        assertEquals(SegmentedEdit("123456", 5), editSegmentedValue("", 0, "12345678", 6))
        assertEquals(SegmentedEdit("129876", 5), editSegmentedValue("123456", 2, "98765", 6))
        assertEquals(SegmentedEdit("1 3456", 1), editSegmentedValue("123456", 1, "", 6))
    }

    @Test
    fun segmentedEditingUsesAuthoredFilterRatherThanHardcodedDigitPolicy() {
        assertEquals(SegmentedEdit("AB", 2), editSegmentedValue("", 0, "AB", 4))
        assertEquals(SegmentedEdit("12", 2), editSegmentedValue("", 0, "1x2", 6, "digits"))
    }

    @Test
    fun incompleteCellsRemainInteriorSpacesAndNeverPassSixDigitPattern() {
        val afterClear = editSegmentedValue("123456", 3, "", 6).value
        assertEquals("123 56", afterClear)
        assertEquals(false, Regex("^[0-9]{6}$").matches(afterClear))
    }
}
