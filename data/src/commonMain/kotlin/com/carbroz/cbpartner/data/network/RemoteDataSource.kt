package com.carbroz.cbpartner.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType

/**
 * Single application remote-data boundary used by all feature data sources.
 *
 * It accepts either an API-relative path or an absolute URL. Relative paths use
 * the centrally configured [NetworkConfig.baseUrl]; absolute URLs are preserved
 * exactly as supplied by the caller/backend contract.
 */
class RemoteDataSource(
    private val httpClient: HttpClient,
    private val networkConfig: NetworkConfig,
) {
    suspend fun execute(
        method: HttpMethod,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): RemoteResponse {
        val resolvedUrl = resolveUrl(url)
        val response = httpClient.request(resolvedUrl) {
            this.method = method
            headers.forEach { (name, value) ->
                header(name, value)
            }
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

        return RemoteResponse(
            statusCode = response.status.value,
            body = response.bodyAsText(),
        )
    }

    private fun resolveUrl(url: String): String =
        if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            networkConfig.baseUrl.trimEnd('/') + "/" + url.trimStart('/')
        }
}

data class RemoteResponse(
    val statusCode: Int,
    val body: String,
)
