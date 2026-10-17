package com.carbroz.cbpartner.core.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals

class DiagnosticIdTest {
    @Test
    fun allowsBoundedDiagnosticIdentifiers() {
        assertEquals("123e4567-e89b-12d3-a456-426614174000", safeDiagnosticId("123e4567-e89b-12d3-a456-426614174000"))
        assertEquals("abc_DEF-123", safeDiagnosticId("abc_DEF-123"))
        assertEquals("a".repeat(64), safeDiagnosticId("a".repeat(64)))
    }

    @Test
    fun rejectsArbitraryOrOversizedValues() {
        assertEquals("-", safeDiagnosticId(null))
        assertEquals("-", safeDiagnosticId(""))
        assertEquals("-", safeDiagnosticId("unsafe value"))
        assertEquals("-", safeDiagnosticId("line\nsecret"))
        assertEquals("-", safeDiagnosticId("https://example.org/private"))
        assertEquals("-", safeDiagnosticId("a".repeat(65)))
    }
}
