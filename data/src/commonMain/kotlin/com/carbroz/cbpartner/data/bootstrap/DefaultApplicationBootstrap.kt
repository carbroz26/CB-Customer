package com.carbroz.cbpartner.data.bootstrap

import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.repository.BootstrapRepository

/**
 * Startup-facing adapter. Bootstrap execution is delegated to the data
 * repository so Splash never owns transport or decoding concerns.
 */
class DefaultApplicationBootstrap(
    private val repository: BootstrapRepository,
) : ApplicationBootstrap {
    override suspend fun invoke(): Result<BootstrapOutput> = repository.fetch()
}
