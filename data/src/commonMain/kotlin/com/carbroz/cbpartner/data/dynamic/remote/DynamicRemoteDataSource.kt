package com.carbroz.cbpartner.data.dynamic.remote

import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.http.HttpMethod

/**
 * Sends Dynamic API requests to relative paths on the configured backend origin.
 *
 * Validates the original endpoint before splitting it into Ktor path segments;
 * splitting first would discard leading slashes and hide malformed authority syntax.
 * Query parameters and fragments are not supported in this endpoint contract.
 */
class DynamicRemoteDataSource(
    private val requestHandler: RequestHandler,
) {
    suspend fun fetch(
        endpoint: String,
        method: String,
        authentication: String,
        body: String?,
        traceContext: TraceContext? = null,
    ): NetworkResult<String> {
        val segments = endpoint.removePrefix("/").split('/')
        require(
            segments.isNotEmpty() &&
                segments.all { segment ->
                    segment.isNotBlank() &&
                        segment != "." &&
                        segment != ".." &&
                        segment.none { it == ':' || it == '\\' || it == '?' || it == '#' || it == '%' }
                },
        ) { "Dynamic endpoint must contain only relative path segments" }
        return requestHandler.executeRequest(
            method = HttpMethod.parse(method),
            urlPathSegments = segments,
            authentication = authentication,
            traceContext = traceContext,
            body = body,
        )
    }
}
