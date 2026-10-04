package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.remote.DynamicRemoteDataSource
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import kotlinx.serialization.json.Json

/**
 * Data-layer implementation of the Dynamic repository.
 *
 * Flow:
 * DynamicRepository -> DynamicRemoteDataSource -> common RemoteDataSource
 * -> JSON DTO -> domain model.
 */
class DynamicRepositoryImpl(
    private val remoteDataSource: DynamicRemoteDataSource,
    private val json: Json,
    private val mapper: DynamicResponseMapper,
) : DynamicRepository {

    override suspend fun fetch(
        destination: DynamicDestination,
        body: String?,
    ): Result<DynamicResponse> = runCatching {
        val remoteResponse = remoteDataSource.fetch(
            endpoint = destination.endpoint,
            method = destination.method,
            body = body,
        )

        val dto = json.decodeFromString<DynamicResponseDto>(remoteResponse.body)
        mapper.map(dto)
    }
}
