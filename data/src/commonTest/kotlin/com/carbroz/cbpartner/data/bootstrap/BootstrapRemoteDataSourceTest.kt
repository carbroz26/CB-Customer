package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.core.runtime.RuntimeMetadata
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.data.bootstrap.remote.BootstrapRemoteDataSource
import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BootstrapRemoteDataSourceTest {
    @Test
    fun requestContainsKnownBootstrapHeadersAndEndpoint() = runTest {
        var requestUrl = ""
        var platform = ""
        var appVersion = ""
        var buildNumber = ""

        val result = createDataSource(
            MockEngine { request ->
                requestUrl = request.url.toString()
                platform = request.headers["X-CarBroz-Platform"].orEmpty()
                appVersion = request.headers["X-CarBroz-App-Version"].orEmpty()
                buildNumber = request.headers["X-CarBroz-Build-Number"].orEmpty()
                respond(
                    SUCCESS_RESPONSE,
                    HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        ).fetch()

        assertIs<NetworkResult.Success<String>>(result)
        assertTrue(requestUrl.endsWith("/api/v1/partner/config/bootstrap"))
        assertEquals("ANDROID", platform)
        assertEquals("1.0.0", appVersion)
        assertEquals("1", buildNumber)
    }

    private fun createDataSource(engine: MockEngine): BootstrapRemoteDataSource =
        BootstrapRemoteDataSource(
            RequestHandler(
                httpClient = HttpClient(engine) {
                    expectSuccess = true
                },
                runtimeMetadataProvider = RuntimeMetadataProvider {
                    RuntimeMetadata(
                        platform = "ANDROID",
                        appVersion = "1.0.0",
                        buildNumber = "1",
                        deviceId = "test-device-id",
                    )
                },
            ),
        )

    private companion object {
        const val SUCCESS_RESPONSE = """{"status":200,"code":"SUCCESS","message":"ok","data":null,"traceId":"req-1"}"""
    }
}
