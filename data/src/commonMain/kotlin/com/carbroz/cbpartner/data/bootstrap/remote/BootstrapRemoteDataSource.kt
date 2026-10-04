package com.carbroz.cbpartner.data.bootstrap.remote

import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.http.HttpMethod

/**
 * Transport boundary for the partner bootstrap endpoint.
 *
 * This class owns endpoint and request metadata only. DTO decoding and
 * domain conversion stay in the repository/mapper layers.
 */
class BootstrapRemoteDataSource(
    private val requestHandler: RequestHandler,
) {
    suspend fun fetch(): NetworkResult<String> = requestHandler.executeRequest<Any, String>(
        method = HttpMethod.Get,
        urlPathSegments = BOOTSTRAP_ENDPOINT.split('/').filter(String::isNotEmpty),
        headers = mapOf(
            PLATFORM_HEADER to PLATFORM,
            APP_VERSION_HEADER to APP_VERSION,
            BUILD_NUMBER_HEADER to BUILD_NUMBER,
        ),
    )

    private companion object {
        const val BOOTSTRAP_ENDPOINT = "/api/v1/partner/config/bootstrap"
        const val PLATFORM_HEADER = "X-CarBroz-Platform"
        const val APP_VERSION_HEADER = "X-CarBroz-App-Version"
        const val BUILD_NUMBER_HEADER = "X-CarBroz-Build-Number"
        const val PLATFORM = "ANDROID"
        const val APP_VERSION = "1.0.0"
        const val BUILD_NUMBER = "1"
    }
}
