package com.carbroz.cbpartner.network.http

import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.NetworkException
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestHandlerTest {
    @Test
    fun cancellationIsRethrownInsteadOfMappedToNetworkFailure() = runTest {
        val handler = RequestHandler(
            HttpClient(MockEngine { throw CancellationException("request cancelled") }) {
                expectSuccess = true
            },
        )
        assertFailsWith<CancellationException> {
            handler.get<String>(listOf("test"))
        }
    }

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
    fun runtimeMetadataHeadersAreApplied() = runTest {
        var capturedPlatform: String? = null
        var capturedAppVersion: String? = null
        var capturedBuildNumber: String? = null
        val requestHandler = RequestHandler(
            HttpClient(
                MockEngine { request ->
                    capturedPlatform = request.headers["X-CarBroz-Platform"]
                    capturedAppVersion = request.headers["X-CarBroz-App-Version"]
                    capturedBuildNumber = request.headers["X-CarBroz-Build-Number"]
                    respond("response")
                },
            ) {
                expectSuccess = true
            },
            runtimeMetadataProvider = RuntimeMetadataProvider {
                RuntimeMetadata(
                    platform = "WEB",
                    appVersion = "1.0.0",
                    buildNumber = "1",
                    deviceId = "device-1",
                )
            },
        )

        requestHandler.get<String>(listOf("api", "v1", "screen"))

        assertEquals("WEB", capturedPlatform)
        assertEquals("1.0.0", capturedAppVersion)
        assertEquals("1", capturedBuildNumber)
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

        assertTrue(error.exception is NetworkException.Server)
        assertEquals(503, error.exception.statusCode)
    }

    @Test
    fun sensitiveResponseRemainsAvailableToCallerWithoutTransportLogging() = runTest {
        val sensitiveResponse = """{"otp":"123456","accessToken":"secret-value"}"""
        val requestHandler = RequestHandler(
            HttpClient(MockEngine { respond(sensitiveResponse) }) {
                expectSuccess = true
            },
        )

        val result = requestHandler.get<String>(listOf("api", "v1", "screen"))

        assertEquals(NetworkResult.Success(sensitiveResponse), result)
    }
    @Test
    fun callerCannotOverrideTransportOwnedHeadersRegardlessOfCase() = runTest {
        val handler = RequestHandler(
            HttpClient(MockEngine { respond("unexpected execution") }) {
                expectSuccess = true
            },
        )
        listOf(
            "authorization",
            "AUTHORIZATION",
            "x-carbroz-platform",
            "X-CarBroz-App-Version",
            "X-CARBROZ-BUILD-NUMBER",
        ).forEach { forbidden ->
            assertFailsWith<IllegalArgumentException> {
                handler.executeRequest<Any, String>(
                    method = HttpMethod.Get,
                    urlPathSegments = listOf("api", "v1", "screen"),
                    headers = mapOf(forbidden to "attacker-value"),
                )
            }
        }
    }

    @Test
    fun ordinaryCallerHeaderIsPassedThrough() = runTest {
        var requestHeader: String? = null
        val handler = RequestHandler(
            HttpClient(MockEngine { request ->
                requestHeader = request.headers["X-Request-Id"]
                respond("response")
            }) {
                expectSuccess = true
            },
        )

        val result = handler.executeRequest<Any, String>(
            method = HttpMethod.Get,
            urlPathSegments = listOf("api", "v1", "screen"),
            headers = mapOf("X-Request-Id" to "safe-correlation-id"),
        )

        assertEquals(NetworkResult.Success("response"), result)
        assertEquals("safe-correlation-id", requestHeader)
    }

}
