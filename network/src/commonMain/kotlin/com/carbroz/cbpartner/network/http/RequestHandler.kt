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
            "Unsupported Dynamic authentication mode: $authentication"
        }

        return try {
            val response = httpClient.prepareRequest {
                this.method = method
                url {
                    val pathSegments = urlPathSegments.map { it.toString() }
                    appendPathSegments(pathSegments)
                }
                headers?.forEach { (key, value) -> header(key, value) }
                body?.let { setBody(body) }
                queryParams?.let { params ->
                    params.forEach { (key, value) ->
                        parameter(key, value)
                    }
                }
            }.execute().body<R>()
            NetworkResult.Success(response)
        } catch (e: Exception) {
            val networkException = if (e is ResponseException) {
                val errorMessage = runCatching {
                    e.response.body<DefaultError>().message
                }.getOrDefault("API Not found")

                when (e.response.status) {
                    HttpStatusCode.Unauthorized -> NetworkException.UnauthorizedException(
                        errorMessage,
                        e,
                    )

                    else -> NetworkException.NotFoundException(
                        errorMessage,
                        e,
                        e.response.status.value,
                    )
                }
            } else {
                NetworkException.UnknownException(e.message ?: "Unknown error", e)
            }
            NetworkResult.Error(null, networkException)
        }
    }

    suspend inline fun <reified R> get(
        urlPathSegments: List<Any>,
        queryParams: Map<String, Any>? = null,
    ): NetworkResult<R> = executeRequest<Any, R>(
        method = HttpMethod.Get,
        urlPathSegments = urlPathSegments.toList(),
        queryParams = queryParams,
    )

    suspend inline fun <reified B, reified R> post(
        urlPathSegments: List<Any>,
        body: B? = null,
    ): NetworkResult<R> = executeRequest(
        method = HttpMethod.Post,
        urlPathSegments = urlPathSegments.toList(),
        body = body,
    )

    suspend inline fun <reified B, reified R> put(
        urlPathSegments: List<Any>,
        body: B? = null,
    ): NetworkResult<R> = executeRequest(
        method = HttpMethod.Put,
        urlPathSegments = urlPathSegments.toList(),
        body = body,
    )

    suspend inline fun <reified B, reified R> delete(
        urlPathSegments: List<Any>,
        body: B? = null,
    ): NetworkResult<R> = executeRequest(
        method = HttpMethod.Delete,
        urlPathSegments = urlPathSegments.toList(),
        body = body,
    )
}
