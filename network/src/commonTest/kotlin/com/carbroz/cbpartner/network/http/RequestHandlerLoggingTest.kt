package com.carbroz.cbpartner.network.http

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestConfig
import co.touchlab.kermit.TestLogWriter
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

@OptIn(ExperimentalKermitApi::class)
class RequestHandlerLoggingTest {

    private fun testLogger(writer: TestLogWriter) = Logger(
        TestConfig(
            minSeverity = Severity.Verbose,
            logWriterList = listOf(writer),
        ),
        "CBPartnerNetwork",
    )

    @Test
    fun successfulRequestDoesNotLogSensitiveData() = runTest {
        val secret = "private-otp-987654"
        val writer = TestLogWriter(loggable = Severity.Verbose)
        val client = HttpClient(MockEngine {
            respond("""{"otp":"$secret"}""")
        }) {
            expectSuccess = true
        }

        try {
            val handler = RequestHandler(
                httpClient = client,
                logger = testLogger(writer),
            )

            val result = handler.executeRequest<String, String>(
                method = HttpMethod.Post,
                urlPathSegments = listOf("api", "secret-path"),
                headers = mapOf("X-Test-Secret" to secret),
                body = """{"otp":"$secret"}""",
            )

            assertIs<NetworkResult.Success<String>>(result)
            assertEquals(2, writer.logs.size)

            val message = writer.logs.last().message

            assertTrue(message.contains("method=POST"))
            assertTrue(message.contains("outcome=success"))
            assertTrue(message.contains("status=200"))
            assertTrue(message.contains("category=none"))
            assertTrue(message.contains("durationMs="))

            assertFalse(message.contains(secret))
            assertFalse(message.contains("secret-path"))
            assertFalse(message.contains("X-Test-Secret"))
        } finally {
            client.close()
        }
    }

    @Test
    fun failedRequestLogsSafeCategory() = runTest {
        val writer = TestLogWriter(loggable = Severity.Verbose)

        val client = HttpClient(MockEngine {
            respond(
                """{"message":"private-server-message"}""",
                HttpStatusCode.ServiceUnavailable,
            )
        }) {
            expectSuccess = true
        }

        try {
            val handler = RequestHandler(
                httpClient = client,
                logger = testLogger(writer),
            )

            val result = handler.get<String>(
                listOf("private-endpoint")
            )

            assertIs<NetworkResult.Error<*>>(result)
            assertEquals(2, writer.logs.size)

            val message = writer.logs.last().message

            assertTrue(message.contains("status=503"))
            assertTrue(message.contains("category=http"))
            assertFalse(message.contains("private-server-message"))
            assertFalse(message.contains("private-endpoint"))
        } finally {
            client.close()
        }
    }

    @Test
    fun cancellationDoesNotBecomeSuccess() = runTest {
        val writer = TestLogWriter(loggable = Severity.Verbose)

        val client = HttpClient(MockEngine {
            throw CancellationException("private-cancellation-message")
        }) {
            expectSuccess = true
        }

        try {
            val handler = RequestHandler(
                httpClient = client,
                logger = testLogger(writer),
            )

            assertFailsWith<CancellationException> {
                handler.get<String>(listOf("private-endpoint"))
            }

            assertEquals(2, writer.logs.size)

            val message = writer.logs.last().message

            assertTrue(message.contains("outcome=cancelled"))
            assertFalse(message.contains("outcome=success"))
            assertFalse(message.contains("private-cancellation-message"))
        } finally {
            client.close()
        }
    }

    @Test
    fun differentLoggersKeepTheirLogsSeparate() = runTest {
        val first = TestLogWriter(loggable = Severity.Verbose)
        val second = TestLogWriter(loggable = Severity.Verbose)

        val client = HttpClient(MockEngine {
            respond("ok")
        }) {
            expectSuccess = true
        }

        try {
            RequestHandler(
                httpClient = client,
                logger = testLogger(first),
            ).get<String>(listOf("first"))

            assertEquals(2, first.logs.size)
            assertTrue(second.logs.isEmpty())

            RequestHandler(
                httpClient = client,
                logger = testLogger(second),
            ).get<String>(listOf("second"))

            assertEquals(2, first.logs.size)
            assertEquals(2, second.logs.size)
        } finally {
            client.close()
        }
    }

    @Test
    fun requestAndResponseShareOneRequestId() = runTest {
        val writer = TestLogWriter(loggable = Severity.Verbose)
        val client = HttpClient(MockEngine {
            respond("ok")
        }) {
            expectSuccess = true
        }

        try {
            val handler = RequestHandler(
                httpClient = client,
                logger = testLogger(writer),
            )

            handler.get<String>(listOf("screen"))

            assertEquals(2, writer.logs.size)

            val request = writer.logs[0].message
            val response = writer.logs[1].message

            assertTrue(request.contains("event=http.request"))
            assertTrue(response.contains("event=http.response"))

            val pattern = Regex(
                """requestId=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"""
            )

            val firstId = pattern.find(request)?.groupValues?.get(1)
            val secondId = pattern.find(response)?.groupValues?.get(1)

            assertTrue(firstId != null)
            assertEquals(firstId, secondId)
        } finally {
            client.close()
        }
    }

    @Test
    fun separateHttpExecutionsHaveDifferentRequestIds() = runTest {
        val writer = TestLogWriter(loggable = Severity.Verbose)
        val client = HttpClient(MockEngine {
            respond("ok")
        }) {
            expectSuccess = true
        }

        try {
            val handler = RequestHandler(
                httpClient = client,
                logger = testLogger(writer),
            )

            handler.get<String>(listOf("first"))
            handler.get<String>(listOf("second"))

            assertEquals(4, writer.logs.size)

            val pattern = Regex(
                """requestId=([0-9a-f-]{36})"""
            )

            val firstId = pattern.find(
                writer.logs[0].message
            )?.groupValues?.get(1)

            val secondId = pattern.find(
                writer.logs[2].message
            )?.groupValues?.get(1)

            assertTrue(firstId != null)
            assertTrue(secondId != null)
            assertTrue(firstId != secondId)

            assertFalse(
                writer.logs.any {
                    it.message.contains("first") ||
                        it.message.contains("second")
                }
            )
        } finally {
            client.close()
        }
    }
}
