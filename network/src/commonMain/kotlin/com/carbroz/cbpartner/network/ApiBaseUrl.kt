package com.carbroz.cbpartner.network

import io.ktor.http.URLProtocol
import io.ktor.http.Url

/**
 * The origin used by the shared Ktor client.
 *
 * Platform entry points supply this value so networking remains shared while
 * local development hosts can differ between Android, Web, and iOS.
 */
class ApiBaseUrl(value: String) {
    val url: Url = Url(value)

    init {
        require(url.protocol == URLProtocol.HTTP || url.protocol == URLProtocol.HTTPS) {
            "API base URL must use HTTP or HTTPS"
        }
        require(url.host.isNotBlank()) {
            "API base URL must include a host"
        }
        require(url.encodedPath.isEmpty() || url.encodedPath == "/") {
            "API base URL must be an origin without a path"
        }
        require(url.parameters.isEmpty() && url.fragment.isEmpty()) {
            "API base URL must not include query parameters or a fragment"
        }
    }

    override fun toString(): String = url.toString()
}
