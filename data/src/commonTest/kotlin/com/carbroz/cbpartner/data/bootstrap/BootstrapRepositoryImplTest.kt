package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.bootstrap.remote.BootstrapRemoteDataSource
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BootstrapRepositoryImplTest {
    @Test
    fun successfulResponseIsMappedToDomainOutput() = runTest {
        val result = createRepository(
            MockEngine {
                respond(SUCCESS_RESPONSE, HttpStatusCode.OK)
            },
        ).fetch()

        assertTrue(result.isSuccess)
        assertEquals("1", result.getOrThrow().version)
        assertEquals("partner_login", result.getOrThrow().destination.screenId)
        assertEquals(false, result.getOrThrow().authenticated)
    }

    @Test
    fun malformedResponseBecomesSerializationFailure() = runTest {
        val result = createRepository(
            MockEngine {
                respond("not-json", HttpStatusCode.OK)
            },
        ).fetch()

        assertIs<BootstrapFailure.Serialization>(result.exceptionOrNull())
    }

    @Test
    fun incompleteResponseBecomesInvalidResponseFailure() = runTest {
        val result = createRepository(
            MockEngine {
                respond(
                    """{"status":200,"code":"SUCCESS","message":"ok","data":null,"traceId":"req-1"}""",
                    HttpStatusCode.OK,
                )
            },
        ).fetch()

        assertIs<BootstrapFailure.InvalidResponse>(result.exceptionOrNull())
    }

    @Test
    fun remoteHttpFailureIsPreserved() = runTest {
        val result = createRepository(
            MockEngine {
                respond("", HttpStatusCode.ServiceUnavailable)
            },
        ).fetch()

        assertTrue(result.isFailure)
        assertEquals(
            503,
            (result.exceptionOrNull() as? com.carbroz.cbpartner.network.NetworkException.Server)?.statusCode,
        )
    }

    private fun createRepository(engine: MockEngine): BootstrapRepositoryImpl =
        BootstrapRepositoryImpl(
            remoteDataSource = BootstrapRemoteDataSource(
                RequestHandler(
                    HttpClient(engine) {
                        expectSuccess = true
                    },
                ),
            ),
            mapper = BootstrapMapper(),
            json = Json { ignoreUnknownKeys = true },
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
                  "destination": {
                    "screenId": "partner_login",
                    "templateId": "tpl_7K2M9Q",
                    "templateType": "form_template",
                    "endpoint": "/api/v1/partner/screen/auth_login"
                  }
                }
              },
              "traceId": "req-1"
            }
        """
    }
}
