package com.carbroz.cbpartner.network

/**
 * Immutable deployment environment supplied by each platform entry point.
 *
 * This is configuration identity, not a network client or a mutable global environment.
 */
enum class AppEnvironment(val identifier: String) {
    Development("development"),
    Staging("staging"),
    Production("production");

    companion object {
        fun parse(value: String): AppEnvironment =
            entries.firstOrNull { it.identifier == value }
                ?: throw IllegalArgumentException("Unsupported application environment")
    }
}
