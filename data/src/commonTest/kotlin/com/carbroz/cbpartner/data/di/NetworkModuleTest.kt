package com.carbroz.cbpartner.data.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class NetworkModuleTest {

    @Test
    fun mockEngineExecutesRequestWithoutRealNetwork() = runTest {
        val client = HttpClient(
            MockEngine {
                respond(
                    content = "",
                    status = HttpStatusCode.OK,
                )
            }
        )

        try {
            val response = client.get("https://example.test")
            assertEquals(HttpStatusCode.OK, response.status)
        } finally {
            client.close()
        }
    }
}
