package com.carbroz.cbpartner.domain.repository

import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput

/**
 * Domain boundary for obtaining the application's startup/bootstrap state.
 */
interface BootstrapRepository {
    suspend fun fetch(): Result<BootstrapOutput>
}
