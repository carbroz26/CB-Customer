package com.carbroz.cbpartner.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteDataSourceTest {
    @Test
    fun relativeUrlUsesCentralBaseUrl() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("http://localhost:3000/api/v1/test", request.url.toString())
            respond(
                content = "{\"ok\":true}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine)
        val dataSource = RemoteDataSource(
            httpClient = client,
            networkConfig = NetworkConfig("http://localhost:3000"),
        )

        val response = dataSource.execute(
            method = HttpMethod.Get,
            url = "/api/v1/test",
        )

        assertEquals(200, response.statusCode)
        assertEquals("{\"ok\":true}", response.body)
        client.close()
    }

    @Test
    fun absoluteUrlIsUsedWithoutBaseUrlPrefix() = runTest {
        val engine = MockEngine { request ->
            assertEquals("https://example.test/api/v1/test", request.url.toString())
            respond(content = "ok", status = HttpStatusCode.OK)
        }
        val client = HttpClient(engine)
        val dataSource = RemoteDataSource(
            httpClient = client,
            networkConfig = NetworkConfig("http://localhost:3000"),
        )

        val response = dataSource.execute(
            method = HttpMethod.Get,
            url = "https://example.test/api/v1/test",
        )

        assertEquals(200, response.statusCode)
        assertEquals("ok", response.body)
        client.close()
    }

    @Test
    fun postBodyIsSentAsApplicationJson() = runTest {
        val expectedBody = "{\"phoneNumber\":\"test-phone\",\"deviceId\":\"bruno-test-device-123\"}"
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(ContentType.Application.Json, request.body.contentType)
            assertEquals(expectedBody, request.body.toByteArray().decodeToString())
            respond(
                content = "{\"ok\":true}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine)
        val dataSource = RemoteDataSource(
            httpClient = client,
            networkConfig = NetworkConfig("http://localhost:3000"),
        )

        dataSource.execute(
            method = HttpMethod.Post,
            url = "/api/v1/partner/auth/send_otp",
            body = expectedBody,
        )

        client.close()
    }
}
