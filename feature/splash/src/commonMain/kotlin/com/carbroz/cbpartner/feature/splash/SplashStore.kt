package com.carbroz.cbpartner.feature.splash

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.domain.repository.BootstrapRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

sealed interface SplashIntent {
    data object LoadBootstrap : SplashIntent
    data object RetryBootstrap : SplashIntent
    data object SkipOptionalUpdate : SplashIntent
}

sealed interface SplashState {
    data object Initial : SplashState
    data object Loading : SplashState
    data class Success(val output: BootstrapOutput) : SplashState
    data class Maintenance(val title: String?, val message: String?) : SplashState
    data class RequiredUpdate(val storeUrl: String?, val minimumVersion: String = "") : SplashState
    data class OptionalUpdate(val output: BootstrapOutput) : SplashState
    data class Failure(val message: String, val failure: AppFailure? = null) : SplashState
}

/**
 * Owns bootstrap loading, retry and startup blocking states until app handoff.
 * The caller closes this Store after successful handoff or composition disposal.
 * UI errors are deliberately generic; raw backend exception messages are never exposed.
 */
class SplashStore(
    private val bootstrap: BootstrapRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _state = MutableStateFlow<SplashState>(SplashState.Initial)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    /** Stable, locally generated identifier for the startup session. */
    val diagnosticSessionId: String = Uuid.random().toString()

    private val diagnosticLogger = Logger.withTag("CBPartnerFlow")
    private var loadJob: Job? = null

    private fun reportBootstrap(actionId: String, event: String, outcome: String? = null) {
        val fields = "event=$event phase=bootstrap sessionId=${safeDiagnosticId(diagnosticSessionId)} " +
            "actionId=${safeDiagnosticId(actionId)} parentActionId=-" +
            (outcome?.let { " outcome=$it" } ?: "")
        runCatching { diagnosticLogger.d { fields } }
    }

    fun accept(intent: SplashIntent) {
        when (intent) {
            SplashIntent.LoadBootstrap,
            SplashIntent.RetryBootstrap,
            -> loadBootstrap()
            SplashIntent.SkipOptionalUpdate -> {
                val optional = _state.value as? SplashState.OptionalUpdate ?: return
                _state.value = SplashState.Success(optional.output)
            }
        }
    }

    private fun loadBootstrap() {
        if (loadJob?.isActive == true) return

        _state.value = SplashState.Loading
        loadJob = scope.launch {
            val actionId = Uuid.random().toString()
            reportBootstrap(actionId, "bootstrap.started")
            bootstrap.fetch(diagnosticSessionId, actionId)
                .onSuccess { output ->
                    reportBootstrap(actionId, "bootstrap.completed", "success")
                    _state.value = when {
                        output.maintenance.enabled -> SplashState.Maintenance(
                            output.maintenance.title,
                            output.maintenance.message,
                        )
                        output.update.required -> SplashState.RequiredUpdate(output.update.storeUrl, output.update.minimumVersion)
                        output.update.optional -> SplashState.OptionalUpdate(output)
                        else -> SplashState.Success(output)
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    reportBootstrap(actionId, "bootstrap.completed", "failed")
                    // Bootstrap failures may contain server-provided text or request details.
                    // Never expose exception messages through the splash UI.
                    _state.value = SplashState.Failure("Unable to start the application", error as? AppFailure)
                }
        }
    }

    fun close() {
        loadJob?.cancel()
        scope.cancel()
    }
}
