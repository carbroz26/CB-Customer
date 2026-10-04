package com.carbroz.cbpartner.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType

class RemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun execute(
        method: HttpMethod,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): RemoteResponse {
        val response = httpClient.request(url) {
            this.method = method
            headers.forEach { (name, value) -> header(name, value) }
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(it)
            }
        }

        return RemoteResponse(
            statusCode = response.status.value,
            body = response.body<String>(),
        )
    }
}

data class RemoteResponse(
    val statusCode: Int,
    val body: String,
)
