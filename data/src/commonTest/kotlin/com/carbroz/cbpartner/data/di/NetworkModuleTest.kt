package com.carbroz.cbpartner.data.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.koin.core.context.koinApplication
import org.koin.core.parameter.parametersOf
import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkModuleTest {
    @Test
    fun networkModuleProvidesHttpClient() {
        val application = koinApplication {
            modules(networkModule)
        }

        val client = application.koin.get<HttpClient>()
        client.close()
        application.close()
    }

    @Test
    fun ktorMockEngineExecutesRequestWithoutNetworkAccess() = runTest {
        val client = HttpClient(MockEngine) {
            engine {
                addHandler {
                    respond(
                        content = "ok",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "text/plain")
                    )
                }
            }
        }

        val response = client.get("https://example.invalid/health")

        assertEquals(HttpStatusCode.OK, response.status)
        client.close()
    }
}
