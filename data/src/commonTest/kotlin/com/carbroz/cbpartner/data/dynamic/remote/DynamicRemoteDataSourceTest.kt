package com.carbroz.cbpartner.data.dynamic.remote

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestConfig
import co.touchlab.kermit.TestLogWriter
import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DynamicRemoteDataSourceTest {
    @Test
    fun unsafeRawEndpointsAreRejectedBeforeSendingHttp() = runTest {
        var requestsSent = 0
        val client = HttpClient(MockEngine {
            requestsSent++
            respond("unexpected request")
        }) {
            expectSuccess = true
        }
        try {
            val source = DynamicRemoteDataSource(RequestHandler(client))
            listOf(
                "https://evil.example/path",
                "//evil.example/path",
                "/api/../private",
                "/api/%2e%2e/private",
                "/api/screen?token=secret",
                "/api/screen#fragment",
                "",
            ).forEach { endpoint ->
                assertFailsWith<IllegalArgumentException> {
                    source.fetch(endpoint, "GET", "NONE", null)
                }
            }
            assertEquals(0, requestsSent)
        } finally {
            client.close()
        }
    }

    @OptIn(ExperimentalKermitApi::class)
    @Test
    fun dynamicRequestEmitsTrustedOperationInsteadOfServerPath() = runTest {
        val writer = TestLogWriter(loggable = Severity.Verbose)
        val logger = Logger(TestConfig(minSeverity = Severity.Verbose, logWriterList = listOf(writer)), "CBPartnerNetwork")
        val client = HttpClient(MockEngine { respond("ok") }) { expectSuccess = true }
        try {
            val source = DynamicRemoteDataSource(RequestHandler(client, logger = logger))
            val result = source.fetch("/api/private-dynamic-path", "GET", "NONE", null)
            assertTrue(result is NetworkResult.Success)
            assertEquals(2, writer.logs.size)
            assertTrue(writer.logs.all { it.message.contains("operation=dynamic_request") })
            assertTrue(writer.logs.none { it.message.contains("private-dynamic-path") })
        } finally {
            client.close()
        }
    }

    @Test
    fun acceptedRelativeEndpointPreservesPathAndResponse() = runTest {
        var actualPath: String? = null
        val client = HttpClient(MockEngine { request ->
            actualPath = request.url.encodedPath
            respond("response")
        }) {
            expectSuccess = true
        }
        try {
            val source = DynamicRemoteDataSource(RequestHandler(client))
            val result = source.fetch("/api/v1/partner/screen", "GET", "NONE", null)
            assertTrue(result is NetworkResult.Success)
            assertEquals("response", result.result)
            assertEquals("/api/v1/partner/screen", actualPath)
        } finally {
            client.close()
        }
    }
}
