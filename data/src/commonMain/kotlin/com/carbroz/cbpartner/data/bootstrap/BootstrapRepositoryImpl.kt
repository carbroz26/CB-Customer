package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.bootstrap.model.BootstrapResponseDto
import com.carbroz.cbpartner.data.bootstrap.remote.BootstrapRemoteDataSource
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapFailure
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.repository.BootstrapRepository
import com.carbroz.cbpartner.network.NetworkResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class BootstrapRepositoryImpl(
    private val remoteDataSource: BootstrapRemoteDataSource,
    private val mapper: BootstrapMapper,
    private val json: Json,
) : BootstrapRepository {

    override suspend fun fetch(): Result<BootstrapOutput> = try {
        when (val response = remoteDataSource.fetch()) {
            is NetworkResult.Success -> mapResponse(response.result)
            is NetworkResult.Error -> Result.failure(response.exception)
        }
    } catch (error: SerializationException) {
        Result.failure(
            BootstrapFailure.Serialization(
                error.message ?: "Unable to decode bootstrap response",
            ),
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Result.failure(
            BootstrapFailure.Transport(
                error.message ?: "Unable to process bootstrap response",
            ),
        )
    }

    private fun mapResponse(rawResponse: String): Result<BootstrapOutput> = runCatching {
        json.decodeFromString<BootstrapResponseDto>(rawResponse)
    }.fold(
        onSuccess = mapper::map,
        onFailure = { error ->
            if (error is CancellationException) throw error
            if (error is SerializationException) {
                Result.failure(
                    BootstrapFailure.Serialization(
                        error.message ?: "Unable to decode bootstrap response",
                    ),
                )
            } else {
                Result.failure(
                    BootstrapFailure.Transport(
                        error.message ?: "Unable to process bootstrap response",
                    ),
                )
            }
        },
    )
}
