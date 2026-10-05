package com.carbroz.cbpartner.feature.dynamic.usecase

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicRequest
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.repository.DynamicRepository

class LoadDynamicScreenUseCase(
    private val repository: DynamicRepository,
) {
    suspend operator fun invoke(destination: DynamicDestination): Result<DynamicResponse> =
        repository.fetch(DynamicRequest(endpoint = destination.endpoint))
}
