package com.carbroz.cbpartner.network

import io.ktor.http.URLProtocol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApiBaseUrlTest {
    @Test
    fun parsesDevelopmentHttpOrigin() {
        val origin = ApiBaseUrl("http://10.0.2.2:3000", AppEnvironment.Development)
        assertEquals(URLProtocol.HTTP, origin.url.protocol)
        assertEquals("10.0.2.2", origin.url.host)
        assertEquals(3000, origin.url.port)
    }

    @Test
    fun parsesTrustedHttpsOriginForStageAndProduction() {
        for (environment in listOf(AppEnvironment.Staging, AppEnvironment.Production)) {
            val origin = ApiBaseUrl("https://api.example.org", environment)
            assertEquals(URLProtocol.HTTPS, origin.url.protocol)
        }
    }

    @Test
    fun rejectsMalformedAndNonOriginUrls() {
        for (url in listOf(
            "http://localhost:3000/api", "https://api.example.org/path",
            "https://api.example.org/?token=secret", "https://user:pass@api.example.org",
        )) {
            assertFailsWith<IllegalArgumentException> { ApiBaseUrl(url) }
        }
    }

    @Test
    fun stagingAndProductionRejectLocalAndPlaceholderOrigins() {
        for (environment in listOf(AppEnvironment.Staging, AppEnvironment.Production)) {
            for (url in listOf(
                "http://api.example.org", "https://localhost:3000",
                "https://127.0.0.1:3000", "https://api.invalid",
                "https://api.example.test", "https://api.example",
            )) {
                assertFailsWith<IllegalArgumentException> {
                    ApiBaseUrl(url, environment)
                }
            }
        }
    }

    @Test
    fun stagingAndProductionRejectTrailingDotHostname() {
        for (environment in listOf(AppEnvironment.Staging, AppEnvironment.Production)) {
            assertFailsWith<IllegalArgumentException> {
                ApiBaseUrl("https://api.example.org.", environment)
            }
        }
    }

    @Test
    fun parsesOnlyThreeKnownEnvironmentIdentifiers() {
        assertEquals(AppEnvironment.Development, AppEnvironment.parse("development"))
        assertEquals(AppEnvironment.Staging, AppEnvironment.parse("staging"))
        assertEquals(AppEnvironment.Production, AppEnvironment.parse("production"))
        assertFailsWith<IllegalArgumentException> { AppEnvironment.parse("debug") }
        assertFailsWith<IllegalArgumentException> { AppEnvironment.parse("Production") }
    }
}
