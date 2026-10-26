package com.carbroz.cbpartner.data.bootstrap

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.error.NetworkFailureMapper
import com.carbroz.cbpartner.data.dynamic.diagnostics.sanitizedResponseJson
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
    private val failureMapper = NetworkFailureMapper(json)

    override suspend fun fetch(): Result<BootstrapOutput> = fetch(null, null)

    override suspend fun fetch(sessionId: String?, actionId: String?): Result<BootstrapOutput> = try {
        runCatching {
            Logger.withTag("CBPartnerFlow").d {
                "event=trace.step owner=BootstrapRepositoryImpl method=fetch stage=enter sessionId=${safeDiagnosticId(sessionId)} actionId=${safeDiagnosticId(actionId)}"
            }
        }
        when (val response = remoteDataSource.fetch(sessionId?.let { TraceContext(it, actionId) })) {
            is NetworkResult.Success -> {
                runCatching {
                    val logger = Logger.withTag("CBPartnerFlow")
                    val identity = " sessionId=${safeDiagnosticId(sessionId)} actionId=${safeDiagnosticId(actionId)}"
                    logger.d { "event=api.json.begin kind=bootstrap$identity" }
                    sanitizedResponseJson(response.result).forEach { line ->
                        logger.d { "event=api.json.line value=$line" }
                    }
                    logger.d { "event=api.json.end kind=bootstrap$identity" }
                }
                mapper.map(json.decodeFromString<BootstrapResponseDto>(response.result))
            }
            is NetworkResult.Error -> Result.failure(failureMapper.map(response.exception, response.body))
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: SerializationException) {
        Result.failure(BootstrapFailure.Serialization("Unable to decode bootstrap response"))
    } catch (error: Throwable) {
        Result.failure(BootstrapFailure.Transport("Unable to process bootstrap response"))
    }
}
