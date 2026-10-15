package com.carbroz.cbpartner.network.http

import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.NetworkException
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
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

    @Test
    fun bearerHeaderUsesProvidedTokenOnlyWhenAuthenticationRequiresIt() = runTest {
        val seenAuthorization = mutableListOf<String?>()
        val handler = RequestHandler(
            HttpClient(MockEngine { request ->
                seenAuthorization += request.headers["Authorization"]
                respond("response")
            }) {
                expectSuccess = true
            },
            bearerTokenProvider = { "test-token" },
        )

        val publicResult = handler.executeRequest<Any, String>(
            method = HttpMethod.Get,
            urlPathSegments = listOf("api", "v1", "public"),
            authentication = "NONE",
        )
        val privateResult = handler.executeRequest<Any, String>(
            method = HttpMethod.Get,
            urlPathSegments = listOf("api", "v1", "private"),
            authentication = "BEARER",
        )

        assertEquals(NetworkResult.Success("response"), publicResult)
        assertEquals(NetworkResult.Success("response"), privateResult)
        assertEquals(listOf(null, "Bearer test-token"), seenAuthorization)
    }

    @Test
    fun missingBearerProviderFailsWithoutSendingRequest() = runTest {
        var requestsSent = 0
        val handler = RequestHandler(
            HttpClient(MockEngine {
                requestsSent++
                respond("unexpected")
            }) {
                expectSuccess = true
            },
        )

        val result = handler.executeRequest<Any, String>(
            method = HttpMethod.Get,
            urlPathSegments = listOf("api", "v1", "private"),
            authentication = "BEARER",
        )

        assertTrue(result is NetworkResult.Error)
        assertEquals(0, requestsSent)
    }

    @Test
    fun canonicalRelativeEndpointKeepsConfiguredOriginAndPath() = runTest {
        var capturedHost: String? = null
        var capturedPath: String? = null
        val handler = RequestHandler(
            HttpClient(MockEngine { request ->
                capturedHost = request.url.host
                capturedPath = request.url.encodedPath
                respond("response")
            }) {
                expectSuccess = true
                defaultRequest {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "api.example.test"
                    }
                }
            },
        )

        val result = handler.get<String>(listOf("api", "v1", "partner", "screen"))

        assertEquals(NetworkResult.Success("response"), result)
        assertEquals("api.example.test", capturedHost)
        assertEquals("/api/v1/partner/screen", capturedPath)
    }

    @Test
    fun httpErrorCodesAreClassifiedByStatus() = runTest {
        val cases = listOf(401, 422, 429, 500)
        for (code in cases) {
            val handler = RequestHandler(
                HttpClient(MockEngine {
                    respond("{}", HttpStatusCode.fromValue(code))
                }) {
                    expectSuccess = true
                },
            )
            val result = handler.get<String>(listOf("api", "test"))
            val failure = result as NetworkResult.Error
            assertEquals(code, failure.exception.statusCode)
            when (code) {
                401 -> assertTrue(failure.exception is NetworkException.Unauthorized)
                422 -> assertTrue(failure.exception is NetworkException.Validation)
                429 -> assertTrue(failure.exception is NetworkException.RateLimit)
                500 -> assertTrue(failure.exception is NetworkException.Server)
            }
        }
    }
}
