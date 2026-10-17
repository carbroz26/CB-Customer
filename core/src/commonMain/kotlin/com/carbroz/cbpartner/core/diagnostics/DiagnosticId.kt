package com.carbroz.cbpartner.core.diagnostics

/**
 * Returns a bounded, log-safe locally generated diagnostic identifier.
 *
 * Identifiers are diagnostic-only; never accept user content or server values.
 * Unsupported values are replaced instead of partially exposing their contents.
 */
fun safeDiagnosticId(value: String?): String {
    if (value.isNullOrEmpty() || value.length > 64) return "-"
    return value.takeIf { id ->
        id.all { char ->
            char in 'a'..'z' ||
                char in 'A'..'Z' ||
                char in '0'..'9' ||
                char == '-' ||
                char == '_'
        }
    } ?: "-"
}
