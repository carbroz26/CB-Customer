package com.carbroz.cbpartner.data.bootstrap

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
        assertEquals("/api/v1/partner/config/bootstrap", requestUrl.substringAfter("/api/v1/partner/config/bootstrap"))
            .also { assertEquals("", it) }
        assertEquals("ANDROID", platform)
        assertEquals("1.0.0", appVersion)
        assertEquals("1", buildNumber)
    }

    private fun createDataSource(engine: MockEngine): BootstrapRemoteDataSource =
        BootstrapRemoteDataSource(
            RequestHandler(
                HttpClient(engine) {
                    expectSuccess = true
                },
            ),
        )

    private companion object {
        const val SUCCESS_RESPONSE = """{"status":200,"code":"SUCCESS","message":"ok","data":null,"traceId":"req-1"}"""
    }
}
