package com.carbroz.cbpartner.network.http

import com.carbroz.cbpartner.network.NetworkException
import com.carbroz.cbpartner.network.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments

class RequestHandler(val httpClient: HttpClient) {

    suspend inline fun <reified B, reified R> executeRequest(
        method: HttpMethod,
        urlPathSegments: List<Any>,
        headers: Map<String, String>? = null,
        authentication: String = "NONE",
        body: B? = null,
        queryParams: Map<String, Any>? = null,
    ): NetworkResult<R> {
        require(authentication.uppercase() in setOf("NONE", "BEARER")) {
            "Unsupported authentication mode: $authentication"
        }

        return try {
            val response = httpClient.prepareRequest {
                this.method = method
                url {
                    appendPathSegments(urlPathSegments.map(Any::toString))
                }
                headers?.forEach { (key, value) -> header(key, value) }
                body?.let(::setBody)
                queryParams?.forEach { (key, value) -> parameter(key, value) }
            }.execute().body<R>()

            NetworkResult.Success(response)
        } catch (error: ResponseException) {
            NetworkResult.Error(null, error.toNetworkException())
        } catch (error: kotlinx.serialization.SerializationException) {
            NetworkResult.Error(
                null,
                NetworkException.Serialization(
                    error.message ?: "Unable to decode network response",
                    error,
                ),
            )
        } catch (error: Throwable) {
            NetworkResult.Error(
                null,
                NetworkException.Unknown(error.message ?: "Unknown network error", error),
            )
        }
    }

    private suspend fun ResponseException.toNetworkException(): NetworkException {
        val message = runCatching { response.body<DefaultError>().message }
            .getOrDefault(response.status.description)

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

    suspend inline fun <reified R> get(
        urlPathSegments: List<Any>,
        queryParams: Map<String, Any>? = null,
    ): NetworkResult<R> = executeRequest<Any, R>(
        method = HttpMethod.Get,
        urlPathSegments = urlPathSegments,
        queryParams = queryParams,
    )

    suspend inline fun <reified B, reified R> post(
        urlPathSegments: List<Any>,
        body: B? = null,
    ): NetworkResult<R> = executeRequest(
        method = HttpMethod.Post,
        urlPathSegments = urlPathSegments,
        body = body,
    )

    suspend inline fun <reified B, reified R> put(
        urlPathSegments: List<Any>,
        body: B? = null,
    ): NetworkResult<R> = executeRequest(
        method = HttpMethod.Put,
        urlPathSegments = urlPathSegments,
        body = body,
    )

    suspend inline fun <reified B, reified R> delete(
        urlPathSegments: List<Any>,
        body: B? = null,
    ): NetworkResult<R> = executeRequest(
        method = HttpMethod.Delete,
        urlPathSegments = urlPathSegments,
        body = body,
    )
}
