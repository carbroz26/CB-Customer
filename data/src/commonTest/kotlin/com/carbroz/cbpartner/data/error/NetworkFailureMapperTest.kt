package com.carbroz.cbpartner.data.error

import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.network.NetworkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class NetworkFailureMapperTest {
    private val mapper = NetworkFailureMapper(Json { ignoreUnknownKeys = true })

    @Test
    fun neverExposesRawExceptionDetailsInUserMessage() {
        val failure = mapper.map(
            NetworkException.Server("token=secret; internal/path", IllegalStateException("secret"), 503),
            """{"status":503,"code":"INTERNAL_SERVER_ERROR","message":"internal token secret","data":null}""",
        )
        assertEquals(AppFailure.Category.SERVER, failure.category)
        assertNull(failure.userMessage)
    }

    @Test
    fun preservesApprovedServiceMessageAndTypedCategory() {
        val failure = mapper.map(
            NetworkException.Server("server", IllegalStateException(), 503),
            """{"status":503,"code":"SERVICE_MAINTENANCE",
               "message":"Car wash service is temporarily unavailable.","data":null}""",
        )
        assertEquals("SERVICE_MAINTENANCE", failure.responseCode)
        assertEquals("Car wash service is temporarily unavailable.", failure.userMessage)
    }

    @Test
    fun distinguishesConnectionAndRateLimit() {
        assertEquals(
            AppFailure.Category.CONNECTION,
            mapper.map(NetworkException.Timeout("timed out", IllegalStateException()), null).category,
        )
        assertEquals(
            AppFailure.Category.RATE_LIMIT,
            mapper.map(NetworkException.RateLimit("rate limited", IllegalStateException()), null).category,
        )
    }
}
