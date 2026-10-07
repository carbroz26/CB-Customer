package com.carbroz.cbpartner.network.http

import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.network.NetworkException
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class RequestHandler(
    private val httpClient: HttpClient,
    private val bearerTokenProvider: () -> String? = { null },
    private val runtimeMetadataProvider: RuntimeMetadataProvider? = null,
) {
    suspend inline fun <reified B, reified R> executeRequest(
        method: HttpMethod,
        urlPathSegments: List<Any>,
        headers: Map<String, String>? = null,
        authentication: String = "NONE",
        body: B? = null,
        queryParams: Map<String, Any>? = null,
    ): NetworkResult<R> {
        val authenticationMode = authentication.uppercase()
        require(authenticationMode in setOf("NONE", "BEARER")) { "Unsupported authentication mode: $authentication" }
        return try {
            logRequest(
                method = method,
                urlPathSegments = urlPathSegments,
                body = body,
            )
            val response = performRequest(
                method = method,
                urlPathSegments = urlPathSegments,
                headers = headers,
                authenticationMode = authenticationMode,
                body = body,
                queryParams = queryParams,
            )
            val result = response.body<R>()
            if (result is String) {
                logResponse(
                    method = method,
                    urlPathSegments = urlPathSegments,
                    status = response.status.value,
                    body = result,
                )
            }
            NetworkResult.Success(result)
        } catch (error: ResponseException) {
            NetworkResult.Error(null, error.toNetworkException())
        } catch (error: kotlinx.serialization.SerializationException) {
            NetworkResult.Error(null, NetworkException.Serialization(error.message ?: "Unable to decode network response", error))
        } catch (error: Throwable) {
            NetworkResult.Error(null, NetworkException.Unknown(error.message ?: "Unknown network error", error))
        }
    }

    @PublishedApi
    internal fun logRequest(
        method: HttpMethod,
        urlPathSegments: List<Any>,
        body: Any?,
    ) {
        println("▶️ ${CYAN}FLOW${RESET}  ${method.value} /${urlPathSegments.joinToString("/")} ")
        println("╭─ 🌐 ${CYAN}API REQUEST${RESET}  ${method.value} /${urlPathSegments.joinToString("/")}")
        if (body is String) {
            println("│ JSON")
            println(formatJson(body))
        } else if (body != null) {
            println("│ BODY  $body")
        }
        println("╰────────────────────────────────────────────────────────")
    }

    @PublishedApi
    internal fun logResponse(
        method: HttpMethod,
        urlPathSegments: List<Any>,
        status: Int,
        body: String,
    ) {
        val url = "${method.value} /${urlPathSegments.joinToString("/")}"
        println("╭─ 🌐 ${GREEN}API RESPONSE${RESET}  $url")
        println("│ ${GREEN}STATUS${RESET}  $status")
        println("│ JSON")
        println(formatJson(body))
        println("╰────────────────────────────────────────────────────────")
    }

    @PublishedApi
    internal fun formatJson(value: String): String = runCatching {
        val element = Json.parseToJsonElement(value)
        Json { prettyPrint = true }.encodeToString(JsonElement.serializer(), element)
    }.getOrElse { value }

    @PublishedApi
    internal suspend fun performRequest(
        method: HttpMethod,
        urlPathSegments: List<Any>,
        headers: Map<String, String>?,
        authenticationMode: String,
        body: Any?,
        queryParams: Map<String, Any>?,
    ): HttpResponse {
        return httpClient.prepareRequest {
            this.method = method
            url { appendPathSegments(urlPathSegments.map(Any::toString)) }
            runtimeMetadataProvider?.current()?.let { metadata ->
                header(PLATFORM_HEADER, metadata.platform)
                header(APP_VERSION_HEADER, metadata.appVersion)
                header(BUILD_NUMBER_HEADER, metadata.buildNumber)
            }
            headers?.forEach { (key, value) -> header(key, value) }
            if (authenticationMode == "BEARER") {
                val token = bearerTokenProvider() ?: error("Bearer authentication requested but no credential is available")
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            body?.let(::setBody)
            queryParams?.forEach { (key, value) -> parameter(key, value) }
        }.execute()
    }

    @PublishedApi
    internal suspend fun ResponseException.toNetworkException(): NetworkException {
        val message = runCatching { response.body<DefaultError>().message }.getOrDefault(response.status.description)
        return when (response.status.value) {
            HttpStatusCode.BadRequest.value -> NetworkException.BadRequest(message, this)
            HttpStatusCode.Unauthorized.value -> NetworkException.Unauthorized(message, this)
            HttpStatusCode.Forbidden.value -> NetworkException.Forbidden(message, this)
            HttpStatusCode.NotFound.value -> NetworkException.NotFound(message, this)
            HttpStatusCode.Conflict.value -> NetworkException.Conflict(message, this)
            422 -> NetworkException.Validation(message, this)
            HttpStatusCode.TooManyRequests.value -> NetworkException.RateLimit(message, this)
            in 500..599 -> NetworkException.Server(message, this, response.status.value)
            else -> NetworkException.Protocol(message, this, response.status.value)
        }
    }

    suspend inline fun <reified R> get(urlPathSegments: List<Any>, queryParams: Map<String, Any>? = null): NetworkResult<R> =
        executeRequest<Any, R>(HttpMethod.Get, urlPathSegments, queryParams = queryParams)

    suspend inline fun <reified B, reified R> post(urlPathSegments: List<Any>, body: B? = null): NetworkResult<R> =
        executeRequest(HttpMethod.Post, urlPathSegments, body = body)

    suspend inline fun <reified B, reified R> put(urlPathSegments: List<Any>, body: B? = null): NetworkResult<R> =
        executeRequest(HttpMethod.Put, urlPathSegments, body = body)

    suspend inline fun <reified B, reified R> delete(urlPathSegments: List<Any>, body: B? = null): NetworkResult<R> =
        executeRequest(HttpMethod.Delete, urlPathSegments, body = body)

    private companion object {
        const val PLATFORM_HEADER = "X-CarBroz-Platform"
        const val APP_VERSION_HEADER = "X-CarBroz-App-Version"
        const val BUILD_NUMBER_HEADER = "X-CarBroz-Build-Number"
        const val RESET = "\u001B[0m"
        const val GREEN = "\u001B[32m"
        const val CYAN = "\u001B[36m"
    }
}
