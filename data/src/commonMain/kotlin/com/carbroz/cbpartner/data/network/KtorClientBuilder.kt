package com.carbroz.cbpartner.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class KtorClientBuilder(
    private val networkConfig: NetworkConfig,
) {
    fun build(): HttpClient = HttpClient {
        expectSuccess = false

        defaultRequest {
            url(networkConfig.baseUrl)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }
}
