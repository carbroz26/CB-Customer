package com.carbroz.cbpartner.app

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.carbroz.cbpartner.network.AppEnvironment

/**
 * Selects the minimum permitted diagnostic level from the deployment environment.
 *
 * Build debuggability does not affect this policy. Sensitive values are prohibited
 * at every level and are never made safe merely by lowering the severity.
 */
internal fun loggingSeverity(environment: AppEnvironment): Severity = when (environment) {
    AppEnvironment.Development -> Severity.Debug
    AppEnvironment.Staging -> Severity.Info
    AppEnvironment.Production -> Severity.Warn
}

/**
 * Configures the existing shared Kermit logger before any Compose UI starts.
 *
 * Platform entry points call this during startup, not during recomposition.
 * A logging failure must never prevent application initialization.
 */
fun configureLogging(environment: AppEnvironment) {
    runCatching { Logger.setMinSeverity(loggingSeverity(environment)) }
}
