package com.carbroz.cbpartner.feature.splash

import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
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

sealed interface SplashIntent {
    data object LoadBootstrap : SplashIntent
    data object RetryBootstrap : SplashIntent
}

sealed interface SplashState {
    data object Initial : SplashState
    data object Loading : SplashState
    data class Success(val output: BootstrapOutput) : SplashState
    data class Maintenance(val title: String?, val message: String?) : SplashState
    data class RequiredUpdate(val storeUrl: String?) : SplashState
    data class Failure(val message: String) : SplashState
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

    private var loadJob: Job? = null

    fun accept(intent: SplashIntent) {
        when (intent) {
            SplashIntent.LoadBootstrap,
            SplashIntent.RetryBootstrap,
            -> loadBootstrap()
        }
    }

    private fun loadBootstrap() {
        if (loadJob?.isActive == true) return

        _state.value = SplashState.Loading
        loadJob = scope.launch {
            bootstrap.fetch()
                .onSuccess { output ->
                    _state.value = when {
                        output.maintenance.enabled -> SplashState.Maintenance(
                            output.maintenance.title,
                            output.maintenance.message,
                        )
                        output.update.required -> SplashState.RequiredUpdate(output.update.storeUrl)
                        else -> SplashState.Success(output)
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    // Bootstrap failures may contain server-provided text or request details.
                    // Never expose exception messages through the splash UI.
                    _state.value = SplashState.Failure("Unable to start the application")
                }
        }
    }

    fun close() {
        loadJob?.cancel()
        scope.cancel()
    }
}
