package com.carbroz.cbpartner.data.dynamic.remote

import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.http.HttpMethod

class DynamicRemoteDataSource(
    private val requestHandler: RequestHandler,
) {
    suspend fun fetch(
        endpoint: String,
        method: String,
        body: String?,
    ): NetworkResult<String> = requestHandler.executeRequest(
        method = HttpMethod.parse(method),
        urlPathSegments = endpoint.split('/').filter(String::isNotEmpty),
        body = body,
    )
}
