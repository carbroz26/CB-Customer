package com.carbroz.cbpartner.feature.splash

import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import com.carbroz.cbpartner.domain.model.bootstrap.BootstrapOutput
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
    data class Failure(val message: String) : SplashState
}

class SplashStore(
    private val bootstrap: ApplicationBootstrap,
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
            bootstrap()
                .onSuccess { output -> _state.value = SplashState.Success(output) }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _state.value = SplashState.Failure(
                        error.message ?: "Unable to start the application",
                    )
                }
        }
    }

    fun close() {
        loadJob?.cancel()
        scope.cancel()
    }
}
