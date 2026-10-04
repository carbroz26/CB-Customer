package com.carbroz.cbpartner.data.bootstrap

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.network.NetworkResult
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class DefaultApplicationBootstrap(
    private val requestHandler: RequestHandler,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : ApplicationBootstrap {
    private val logger = Logger.withTag("Bootstrap")

    override suspend fun invoke(): Result<BootstrapOutput> {
        logger.i { "Requesting $BOOTSTRAP_ENDPOINT" }

        return try {
            when (
                val response = requestHandler.executeRequest<Any, String>(
                    method = HttpMethod.Get,
                    urlPathSegments = BOOTSTRAP_ENDPOINT.split('/').filter(String::isNotEmpty),
                    headers = mapOf(
                        "X-CarBroz-Platform" to "ANDROID",
                        "X-CarBroz-App-Version" to "1.0.0",
                        "X-CarBroz-Build-Number" to "1",
                    ),
                )
            ) {
                is NetworkResult.Success -> {
                    logger.i { "Response parsed successfully" }
                    json.decodeFromString<BootstrapResponseDto>(response.result).toDomain()
                }

                is NetworkResult.Error -> {
                    logger.e { "Transport failure: ${response.exception.message}" }
                    response.exception.statusCode?.let {
                        Result.failure(BootstrapFailure.Http(it))
                    } ?: Result.failure(
                        BootstrapFailure.Transport(
                            response.exception.message ?: "Unable to reach bootstrap service",
                        ),
                    )
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: SerializationException) {
            logger.e(error) { "Serialization failure: ${error.message}" }
            Result.failure(
                BootstrapFailure.Serialization(
                    error.message ?: "Unable to decode bootstrap response",
                ),
            )
        } catch (error: Throwable) {
            logger.e(error) { "Transport failure: ${error.message}" }
            Result.failure(
                BootstrapFailure.Transport(
                    error.message ?: "Unable to reach bootstrap service",
                ),
            )
        }
    }

    private companion object {
        const val BOOTSTRAP_ENDPOINT = "/api/v1/partner/config/bootstrap"
    }
}
