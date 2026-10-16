package com.carbroz.cbpartner.domain.repository

import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput

/**
 * Domain boundary for obtaining the application's startup/bootstrap state.
 */
interface BootstrapRepository {
    suspend fun fetch(): Result<BootstrapOutput>

    /** Optional diagnostics; existing test fakes and callers keep the original contract. */
    suspend fun fetch(sessionId: String?, actionId: String?): Result<BootstrapOutput> = fetch()
}
