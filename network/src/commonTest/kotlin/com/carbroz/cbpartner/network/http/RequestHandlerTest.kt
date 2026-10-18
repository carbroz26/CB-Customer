package com.carbroz.cbpartner.network.http

import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.NetworkException
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.network.UnresolvedAddressException
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
                    platform = "android",
                    appVersion = "1.0.0",
                    buildNumber = "1",
                    deviceId = "device-1",
                )
            },
        )

        requestHandler.get<String>(listOf("api", "v1", "screen"))

        assertEquals("ANDROID", capturedPlatform)
        assertEquals("1.0.0", capturedAppVersion)
        assertEquals("1", capturedBuildNumber)
    }

    @Test
    fun safeHeaderDiagnosticsUseOnlyBoundedRuntimeMetadata() {
        val handler = RequestHandler(HttpClient(MockEngine { respond("ok") }))
        val fields = handler.safeMetadataHeaderFields(
            RuntimeMetadata(
                platform = "android",
                appVersion = "1.0.0",
                buildNumber = "1",
                deviceId = "private-device-id",
            ),
        )
        assertEquals(" headerPlatform=ANDROID headerAppVersion=1.0.0 headerBuildNumber=1", fields)
        assertTrue("private-device-id" !in fields)
        assertTrue("Authorization" !in fields)
        assertEquals("", handler.safeMetadataHeaderFields(null))
    }

    @Test
    fun unsafeRuntimeMetadataValuesAreRedacted() {
        val handler = RequestHandler(HttpClient(MockEngine { respond("ok") }))
        val fields = handler.safeMetadataHeaderFields(
            RuntimeMetadata(
                platform = "android secret",
                appVersion = "1.0.0 token=secret",
                buildNumber = "123;Authorization=secret",
                deviceId = "private",
            ),
        )
        assertEquals(" headerPlatform=[redacted] headerAppVersion=[redacted] headerBuildNumber=[redacted]", fields)
        assertTrue("secret" !in fields)
    }

    @Test
    fun bodyDiagnosticsRespectRealPresenceAndMaskPhoneValue() {
        val handler = RequestHandler(HttpClient(MockEngine { respond("ok") }))
        assertEquals(" bodyState=absent", handler.safeBodyDiagnosticFields(null))
        assertEquals(" bodyState=present", handler.safeBodyDiagnosticFields("""{"other":"secret"}"""))
        assertEquals(
            " bodyState=present bodyPhoneField=present",
            handler.safeBodyDiagnosticFields("""{"phone":"9876543210"}"""),
        )
        assertTrue("9876543210" !in handler.safeBodyDiagnosticFields("""{"phone":"9876543210"}"""))
        assertEquals(" bodyState=present", handler.safeBodyDiagnosticFields("not-json"))
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

    @Test
    fun unexpectedTransportFailureCurrentlyUsesUnknownError() = runTest {
        val handler = RequestHandler(
            HttpClient(MockEngine {
                throw IllegalStateException("simulated transport failure")
            }) {
                expectSuccess = true
            },
        )

        val result = handler.get<String>(listOf("api", "v1", "screen"))

        val failure = result as NetworkResult.Error
        assertTrue(failure.exception is NetworkException.Unknown)
        assertEquals(null, failure.exception.statusCode)
    }

    @Test
    fun malformedHttpErrorBodyFallsBackToStatusDescription() = runTest {
        val handler = RequestHandler(
            HttpClient(MockEngine {
                respond("not-json", HttpStatusCode.Forbidden)
            }) {
                expectSuccess = true
            },
        )

        val result = handler.get<String>(listOf("api", "v1", "screen"))

        val failure = result as NetworkResult.Error
        assertTrue(failure.exception is NetworkException.Forbidden)
        assertEquals(403, failure.exception.statusCode)
    }

    @Test
    fun ktorTimeoutsProduceTypedFailuresWithoutLeakingEndpoint() = runTest {
        val cases = listOf(
            HttpRequestTimeoutException("https://private.example.test/token", 1000),
            ConnectTimeoutException("private-host"),
            SocketTimeoutException("private-host"),
        )
        for (cause in cases) {
            val handler = RequestHandler(
                HttpClient(MockEngine { throw cause }) {
                    expectSuccess = true
                },
            )

            val result = handler.get<String>(listOf("api", "v1", "private"))
            val failure = result as NetworkResult.Error
            assertTrue(failure.exception is NetworkException.Timeout)
            assertEquals(null, failure.exception.statusCode)
            assertTrue(failure.exception.message.orEmpty().contains("private").not())
        }
    }

    @Test
    fun unresolvedAddressProducesConnectionFailure() = runTest {
        val handler = RequestHandler(
            HttpClient(MockEngine {
                throw UnresolvedAddressException()
            }) {
                expectSuccess = true
            },
        )

        val result = handler.get<String>(listOf("api", "v1", "screen"))

        val failure = result as NetworkResult.Error
        assertTrue(failure.exception is NetworkException.Connection)
        assertEquals(null, failure.exception.statusCode)
        assertEquals("Unable to resolve network address", failure.exception.message)
    }

    @Test
    fun blankBearerCredentialFailsBeforeNetworkRequest() = runTest {
        listOf("", "   ").forEach { credential ->
            var requestsSent = 0
            val client = HttpClient(MockEngine {
                requestsSent++
                respond("unexpected")
            }) {
                expectSuccess = true
            }
            try {
                val handler = RequestHandler(client, bearerTokenProvider = { credential })
                val result = handler.executeRequest<Any, String>(
                    method = HttpMethod.Get,
                    urlPathSegments = listOf("api", "v1", "private"),
                    authentication = "BEARER",
                )

                assertTrue(result is NetworkResult.Error)
                assertEquals(0, requestsSent)
            } finally {
                client.close()
            }
        }
    }
}
