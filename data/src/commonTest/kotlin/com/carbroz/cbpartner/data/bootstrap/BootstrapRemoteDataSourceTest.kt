package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.network.http.RequestHandler
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BootstrapRemoteDataSourceTest {
    @Test
    fun successfulResponseIsMapped() = runTest {
        val result = createBootstrap(
            MockEngine {
                respond(
                    content = SUCCESS_RESPONSE,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        ).invoke()

        assertTrue(result.isSuccess)
        assertEquals("1", result.getOrThrow().config.version)
        assertEquals("partner_login", result.getOrThrow().startup.nextScreen.screenId)
    }

    @Test
    fun requestContainsKnownBootstrapHeadersAndEndpoint() = runTest {
        var requestUrl = ""
        var platform = ""
        var appVersion = ""
        var buildNumber = ""

        val result = createBootstrap(
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
        ).invoke()

        assertTrue(result.isSuccess)
        assertTrue(requestUrl.endsWith("/api/v1/partner/config/bootstrap"))
        assertEquals("ANDROID", platform)
        assertEquals("1.0.0", appVersion)
        assertEquals("1", buildNumber)
    }

    @Test
    fun nonSuccessfulHttpStatusIsMapped() = runTest {
        val result = createBootstrap(
            MockEngine {
                respond("", HttpStatusCode.ServiceUnavailable)
            },
        ).invoke()

        val failure = result.exceptionOrNull()

        assertTrue(failure is BootstrapFailure.Http)
        assertEquals(503, (failure as BootstrapFailure.Http).statusCode)
    }

    @Test
    fun incompleteSuccessfulResponseIsRejected() = runTest {
        val result = createBootstrap(
            MockEngine {
                respond(
                    """{"status":200,"code":"SUCCESS","message":"ok","data":null,"traceId":"req-1"}""",
                    HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            },
        ).invoke()

        val failure = result.exceptionOrNull()

        assertTrue(failure is BootstrapFailure.InvalidResponse)
    }

    @Test
    fun malformedResponseIsMappedToSerializationFailure() = runTest {
        val result = createBootstrap(
            MockEngine {
                respond("not-json", HttpStatusCode.OK)
            },
        ).invoke()

        val failure = result.exceptionOrNull()

        assertTrue(failure is BootstrapFailure.Serialization)
    }

    private fun createBootstrap(engine: MockEngine): DefaultApplicationBootstrap =
        DefaultApplicationBootstrap(
            RequestHandler(HttpClient(engine)),
        )

    private companion object {
        const val SUCCESS_RESPONSE = """
            {
              "status": 200,
              "code": "SUCCESS",
              "message": "Partner bootstrap completed",
              "data": {
                "config": {
                  "version": "1",
                  "maintenance": {"enabled": false, "title": null, "message": null},
                  "update": {"required": false, "optional": false, "minimumVersion": "1.0.0", "latestVersion": "1.0.0", "storeUrl": null},
                  "features": {"registrationEnabled": true, "individualPartnerEnabled": true, "organizationPartnerEnabled": true}
                },
                "startup": {
                  "authenticated": false,
                  "nextScreen": {
                    "screenId": "partner_login",
                    "templateId": "tpl_7K2M9Q",
                    "templateType": "form_template",
                    "endpoint": "/api/v1/partner/screen/auth_login",
                    "method": "GET",
                    "authentication": "NONE"
                  }
                }
              },
              "traceId": "req-1"
            }
        """
    }
}
