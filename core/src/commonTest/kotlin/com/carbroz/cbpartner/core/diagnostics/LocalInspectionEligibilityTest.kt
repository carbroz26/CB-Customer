package com.carbroz.cbpartner.core.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalInspectionEligibilityTest {
    @Test
    fun onlyAllVerifiedConditionsEnableInspection() {
        for (mask in 0..15) {
            val isDevelopment = mask and 1 != 0
            val debuggable = mask and 2 != 0
            val verifiedLocalBackend = mask and 4 != 0
            val optedIn = mask and 8 != 0

            assertEquals(
                mask == 15,
                isLocalInspectionEligible(isDevelopment, debuggable, verifiedLocalBackend, optedIn),
                "Unexpected inspection eligibility for mask=$mask",
            )
        }
    }

    @Test
    fun losingAnyPrerequisiteDisablesAnEligibleSession() {
        assertTrue(isLocalInspectionEligible(true, true, true, true))
        assertFalse(isLocalInspectionEligible(false, true, true, true))
        assertFalse(isLocalInspectionEligible(true, false, true, true))
        assertFalse(isLocalInspectionEligible(true, true, false, true))
        assertFalse(isLocalInspectionEligible(true, true, true, false))
    }

    @Test
    fun unknownBackendTrustMustFailClosed() {
        assertFalse(
            isLocalInspectionEligible(
                isDevelopment = true,
                debuggable = true,
                verifiedLocalBackend = false,
                optedIn = true,
            ),
        )
    }
}
