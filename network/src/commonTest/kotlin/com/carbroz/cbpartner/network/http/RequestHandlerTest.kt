package com.carbroz.cbpartner.network.http

import com.carbroz.cbpartner.network.NetworkException
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestHandlerTest {
    @Test
    fun successfulRequestReturnsResponse() = runTest {
        val requestHandler = RequestHandler(
            HttpClient(
                MockEngine {
                    respond("response")
                },
            ) {
                expectSuccess = true
            },
        )

        val result = requestHandler.get<String>(listOf("api", "v1", "screen"))

        assertEquals(NetworkResult.Success("response"), result)
    }

    @Test
    fun failedRequestReturnsNetworkException() = runTest {
        val requestHandler = RequestHandler(
            HttpClient(
                MockEngine {
                    respond("", HttpStatusCode.ServiceUnavailable)
                },
            ) {
                expectSuccess = true
            },
        )

        val result = requestHandler.executeRequest<Any, String>(
            method = HttpMethod.Get,
            urlPathSegments = listOf("api", "v1", "screen"),
        )
        val error = result as NetworkResult.Error

        assertTrue(error.exception is NetworkException.NotFoundException)
        assertEquals(503, error.exception.statusCode)
    }
}
