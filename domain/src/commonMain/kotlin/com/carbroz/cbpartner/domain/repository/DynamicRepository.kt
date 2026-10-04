package com.carbroz.cbpartner.domain.repository

import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue

/**
 * Domain boundary for loading and executing Dynamic destinations.
 *
 * Transport metadata is owned by the shared network infrastructure and is not
 * part of the Dynamic repository contract.
 */
interface DynamicRepository {
    suspend fun fetch(
        destination: DynamicDestination,
        body: DynamicValue? = null,
    ): Result<DynamicResponse>
}
