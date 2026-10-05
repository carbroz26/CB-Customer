package com.carbroz.cbpartner.domain.repository

import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse

/** Domain boundary for loading Dynamic screens and executing Dynamic requests. */
interface DynamicRepository {
    suspend fun fetch(request: DynamicRequest): Result<DynamicResponse>
}
