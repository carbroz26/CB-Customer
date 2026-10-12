package com.carbroz.cbpartner.network

import io.ktor.http.URLProtocol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApiBaseUrlTest {
    @Test
    fun parsesHttpOrigin() {
        val apiBaseUrl = ApiBaseUrl("http://10.0.2.2:3000")

        assertEquals(URLProtocol.HTTP, apiBaseUrl.url.protocol)
        assertEquals("10.0.2.2", apiBaseUrl.url.host)
        assertEquals(3000, apiBaseUrl.url.port)
    }

    @Test
    fun parsesHttpsOrigin() {
        val apiBaseUrl = ApiBaseUrl("https://api.example.test")

        assertEquals(URLProtocol.HTTPS, apiBaseUrl.url.protocol)
        assertEquals("api.example.test", apiBaseUrl.url.host)
        assertEquals(443, apiBaseUrl.url.port)
    }

    @Test
    fun rejectsBaseUrlWithPath() {
        assertFailsWith<IllegalArgumentException> {
            ApiBaseUrl("http://localhost:3000/api")
        }
    }
}
