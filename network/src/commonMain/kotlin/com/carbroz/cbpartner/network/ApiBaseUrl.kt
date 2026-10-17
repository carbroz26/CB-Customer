package com.carbroz.cbpartner.network

import io.ktor.http.URLProtocol
import io.ktor.http.Url

/**
 * The platform-provided HTTP origin for the application's single shared Ktor client.
 *
 * Development allows local HTTP. Staging and production require an explicitly configured,
 * HTTPS public-origin hostname. This check is not a substitute for TLS and deployment trust.
 */
class ApiBaseUrl(value: String, environment: AppEnvironment = AppEnvironment.Development) {
    val url: Url = Url(value)

    init {
        require(url.protocol == URLProtocol.HTTP || url.protocol == URLProtocol.HTTPS) {
            "API base URL must use HTTP or HTTPS"
        }
        require(url.host.isNotBlank()) { "API base URL must include a host" }
        require(url.encodedPath.isEmpty() || url.encodedPath == "/") {
            "API base URL must be an origin without a path"
        }
        require(url.parameters.isEmpty() && url.fragment.isEmpty()) {
            "API base URL must not include query parameters or a fragment"
        }
        require(url.user == null && url.password == null) {
            "API base URL cannot include credentials"
        }
        if (environment != AppEnvironment.Development) {
            val host = url.host.lowercase()
            val reservedSuffixes = listOf(".localhost", ".invalid", ".test", ".example", ".local", ".internal")
            require(url.protocol == URLProtocol.HTTPS &&
                host.contains('.') &&
                host != "localhost" &&
                host != "api.invalid" &&
                !host.endsWith('.') &&
                reservedSuffixes.none { host.endsWith(it) } &&
                host.all { it.isLetterOrDigit() || it == '.' || it == '-' } &&
                host.any { it.isLetter() }) {
                "Staging and production require an explicitly configured trusted HTTPS origin"
            }
        }
    }

    override fun toString(): String = url.toString()
}
