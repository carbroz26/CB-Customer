package com.carbroz.cbpartner.data.error

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.network.NetworkException
import kotlinx.serialization.json.Json

/**
 * Single boundary converting HTTP/transport failures to safe domain failures.
 * Raw response text never reaches presentation or logs.
 */
class NetworkFailureMapper(
    private val json: Json,
) {
    private val responseMapper = DynamicResponseMapper(json)

    fun map(error: NetworkException, body: String?): AppFailure {
        val category = when (error) {
            is NetworkException.Connection, is NetworkException.Timeout ->
                AppFailure.Category.CONNECTION
            is NetworkException.Server -> AppFailure.Category.SERVER
            is NetworkException.Unauthorized, is NetworkException.Forbidden ->
                AppFailure.Category.ACCESS
            is NetworkException.Validation, is NetworkException.BadRequest,
            is NetworkException.Conflict -> AppFailure.Category.VALIDATION
            is NetworkException.RateLimit -> AppFailure.Category.RATE_LIMIT
            else -> AppFailure.Category.UNKNOWN
        }
        val parsed = body?.takeIf { it.length <= 32768 }?.let {
            runCatching { json.decodeFromString<DynamicResponseDto>(it) }.getOrNull()
        }
        val validCode = parsed?.code?.takeIf {
            it.length <= 64 && it.matches(Regex("[A-Z][A-Z0-9_]*"))
        }
        val presentation = parsed?.let { response ->
            runCatching { responseMapper.map(response).presentation as? DialogPresentation }
                .getOrNull()
        }
        // A response message is approved for direct display only for these domain-owned
        // codes, after bounded text validation; other codes rely on local safe copy.
        val userMessage = parsed?.message?.trim()?.takeIf { message ->
            validCode in setOf("APP_MAINTENANCE", "SERVICE_MAINTENANCE") &&
                message.isNotBlank() && message.length <= 200 &&
                !message.contains("://") && !message.contains('\\') &&
                !message.contains('\n') && !message.contains('\r')
        }
        return AppFailure(
            category = category,
            responseCode = validCode,
            userMessage = userMessage,
            presentation = presentation,
            httpStatus = error.statusCode,
        )
    }
}

