package com.carbroz.cbpartner.core.diagnostics

/**
 * Determines whether an explicitly opted-in local inspection session is eligible.
 *
 * The platform composition root supplies independently verified inputs; this pure
 * check never infers backend trust from a URL or performs any logging or I/O.
 * An unavailable or unverified prerequisite must be supplied as false.
 */
fun isLocalInspectionEligible(
    isDevelopment: Boolean,
    debuggable: Boolean,
    verifiedLocalBackend: Boolean,
    optedIn: Boolean,
): Boolean = isDevelopment && debuggable && verifiedLocalBackend && optedIn
