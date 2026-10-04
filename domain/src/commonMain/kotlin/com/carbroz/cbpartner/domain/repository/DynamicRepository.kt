package com.carbroz.cbpartner.domain.repository

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue

/**
 * Domain boundary for loading Dynamic screens and executing Dynamic requests.
 * Destination identifies a screen; HTTP method and authentication are request metadata.
 */
interface DynamicRepository {
    suspend fun fetch(
        destination: DynamicDestination,
        body: DynamicValue? = null,
        method: String = "GET",
        authentication: String = "NONE",
    ): Result<DynamicResponse>
}
