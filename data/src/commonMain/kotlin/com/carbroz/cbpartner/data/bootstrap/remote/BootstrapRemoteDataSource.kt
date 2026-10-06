package com.carbroz.cbpartner.data.bootstrap.remote

import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.http.HttpMethod

/**
 * Transport boundary for the partner bootstrap endpoint.
 *
 * Common runtime metadata headers are supplied by the network boundary.
 * DTO decoding and domain conversion stay in the repository/mapper layers.
 */
class BootstrapRemoteDataSource(
    private val requestHandler: RequestHandler,
) {
    suspend fun fetch(): NetworkResult<String> = requestHandler.executeRequest<Any, String>(
        method = HttpMethod.Get,
        urlPathSegments = BOOTSTRAP_ENDPOINT.split('/').filter(String::isNotEmpty),
    )

    private companion object {
        const val BOOTSTRAP_ENDPOINT = "/api/v1/partner/config/bootstrap"
    }
}
