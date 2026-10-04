package com.carbroz.cbpartner.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent

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
                // Send the already-serialized JSON as an explicit JSON request
                // body. Do not rely on ContentNegotiation to serialize a String,
                // because dynamic requests are runtime-defined payloads.
                setBody(TextContent(body, ContentType.Application.Json))
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
