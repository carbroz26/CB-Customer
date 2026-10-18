package com.carbroz.cbpartner.data.bootstrap.remote

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
import kotlin.test.assertTrue

@OptIn(ExperimentalKermitApi::class)
class BootstrapRemoteDataSourceLoggingTest {
    @Test
    fun bootstrapUsesFixedTrustedOperationAndKeepsRealRequestPath() = runTest {
        val writer = TestLogWriter(loggable = Severity.Verbose)
        val logger = Logger(
            TestConfig(minSeverity = Severity.Verbose, logWriterList = listOf(writer)),
            "CBPartnerNetwork",
        )
        var actualPath: String? = null
        val client = HttpClient(MockEngine { request ->
            actualPath = request.url.encodedPath
            respond("ok")
        }) { expectSuccess = true }
        try {
            val result = BootstrapRemoteDataSource(RequestHandler(client, logger = logger)).fetch()
            assertTrue(result is NetworkResult.Success)
            assertEquals("/api/v1/partner/config/bootstrap", actualPath)
            assertEquals(2, writer.logs.size)
            assertTrue(writer.logs.all { it.message.contains("operation=bootstrap") })
        } finally {
            client.close()
        }
    }
}
