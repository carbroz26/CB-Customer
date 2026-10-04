package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.remote.DynamicRemoteDataSource
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.network.NetworkResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class DynamicRepositoryImpl(
    private val remoteDataSource: DynamicRemoteDataSource,
    private val json: Json,
    private val mapper: DynamicResponseMapper,
) : DynamicRepository {

    override suspend fun fetch(
        destination: DynamicDestination,
        body: DynamicValue?,
    ): Result<DynamicResponse> {
        return when (
            val response = remoteDataSource.fetch(
                endpoint = destination.endpoint,
                method = destination.method,
                body = body?.toJsonElement()?.let {
                    json.encodeToString(JsonElement.serializer(), it)
                },
            )
        ) {
            is NetworkResult.Success -> runCatching {
                mapper.map(json.decodeFromString<DynamicResponseDto>(response.result))
            }

            is NetworkResult.Error -> Result.failure(response.exception)
        }
    }

    private fun DynamicValue.toJsonElement(): JsonElement = when (this) {
        is DynamicValue.ObjectValue -> JsonObject(value.mapValues { (_, child) -> child.toJsonElement() })
        is DynamicValue.ArrayValue -> JsonArray(value.map { it.toJsonElement() })
        is DynamicValue.StringValue -> JsonPrimitive(value)
        is DynamicValue.NumberValue -> JsonPrimitive(value)
        is DynamicValue.BooleanValue -> JsonPrimitive(value)
        DynamicValue.NullValue -> JsonNull
    }
}
