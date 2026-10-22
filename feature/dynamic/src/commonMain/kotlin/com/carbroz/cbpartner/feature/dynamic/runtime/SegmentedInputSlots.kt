package com.carbroz.cbpartner.feature.dynamic.runtime

/**
 * Pure positional editing for a segmented InputElement. One Dynamic string binding
 * owns the values; '_' represents a vacant slot so deletions never shift later digits.
 * The sentinel is never eligible for a verified request (full numeric validation).
 */
internal object SegmentedInputSlots {
    private const val EMPTY = '_'

    data class Edit(val value: String, val focusIndex: Int)

    fun normalize(raw: String, count: Int): String {
        require(count > 0) { "Segmented input count must be positive" }
        return raw.take(count).padEnd(count, EMPTY).map { digit ->
            if (digit in '0'..'9') digit else EMPTY
        }.joinToString("")
    }

    fun display(raw: String, count: Int, index: Int): String {
        val digit = normalize(raw, count)[index]
        return if (digit == EMPTY) "" else digit.toString()
    }

    fun complete(raw: String, count: Int): Boolean =
        raw.length == count && raw.all { it in '0'..'9' }

    /**
     * onValueChange covers typed single digits, replacing a selected cell, and
     * clipboard insertion of multiple digits starting at the selected cell.
     */
    fun edit(raw: String, count: Int, index: Int, incoming: String): Edit {
        require(index in 0 until count) { "Segment index is outside input" }
        val slots = normalize(raw, count).toCharArray()
        val digits = incoming.filter { it in '0'..'9' }
        if (digits.isEmpty()) {
            slots[index] = EMPTY
            return Edit(slots.concatToString(), index)
        }
        digits.take(count - index).forEachIndexed { offset, digit ->
            slots[index + offset] = digit
        }
        return Edit(slots.concatToString(), (index + digits.length).coerceAtMost(count - 1))
    }

    /** Backspace on empty moves focus back without deleting/shifting other slots. */
    fun backspaceOnEmpty(raw: String, count: Int, index: Int): Edit {
        require(index in 0 until count) { "Segment index is outside input" }
        return Edit(normalize(raw, count), (index - 1).coerceAtLeast(0))
    }
}
