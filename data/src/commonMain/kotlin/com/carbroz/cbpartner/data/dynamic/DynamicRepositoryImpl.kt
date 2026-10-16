package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.remote.DynamicRemoteDataSource
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import com.carbroz.cbpartner.network.NetworkResult
import kotlinx.coroutines.CancellationException
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

    override suspend fun fetch(request: DynamicRequest): Result<DynamicResponse> {
        return when (
            val response = remoteDataSource.fetch(
                endpoint = request.endpoint,
                method = request.method,
                authentication = request.authentication,
                traceContext = request.sessionId?.let { session ->
                    TraceContext(session, request.actionId, request.parentActionId)
                },
                body = request.body?.toJsonElement()?.let {
                    json.encodeToString(JsonElement.serializer(), it)
                },
            )
        ) {
            is NetworkResult.Success -> runCatching {
                mapper.map(json.decodeFromString<DynamicResponseDto>(response.result))
            }.onFailure { error ->
                if (error is CancellationException) throw error
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
