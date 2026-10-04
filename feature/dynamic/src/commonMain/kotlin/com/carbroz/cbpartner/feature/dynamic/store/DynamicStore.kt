package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.ExecuteDynamicActionUseCase
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.LoadDynamicScreenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DynamicIntent {
    data object Load : DynamicIntent
    data object Retry : DynamicIntent
    data class Action(val action: DynamicAction) : DynamicIntent
    data class ValueChanged(val key: String, val value: String) : DynamicIntent
}

sealed interface DynamicEffect {
    data class Navigate(val destination: DynamicDestination) : DynamicEffect
    data class ExternalUri(val uri: String) : DynamicEffect
}

sealed interface DynamicState {
    data object Initial : DynamicState
    data object Loading : DynamicState
    data class Success(val response: DynamicResponse) : DynamicState
    data class Failure(val message: String) : DynamicState
}

class DynamicStore(
    private val loadDynamicScreen: LoadDynamicScreenUseCase,
    private val executeDynamicAction: ExecuteDynamicActionUseCase,
    destination: DynamicDestination,
    private val contextProvider: () -> Map<String, String> = { emptyMap() },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _state = MutableStateFlow<DynamicState>(DynamicState.Initial)
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private val _effects = MutableStateFlow<DynamicEffect?>(null)
    val effects: StateFlow<DynamicEffect?> = _effects.asStateFlow()

    private val bindings = mutableMapOf<String, String>()
    private var currentDestination = destination
    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun updateDestination(destination: DynamicDestination) {
        currentDestination = destination
        bindings.clear()
        loadJob?.cancel()
        actionJob?.cancel()
        _state.value = DynamicState.Initial
        _effects.value = null
    }

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            is DynamicIntent.ValueChanged -> bindings[intent.key] = intent.value
            is DynamicIntent.Action -> handleAction(intent.action)
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return

        _state.value = DynamicState.Loading
        loadJob = scope.launch {
            loadDynamicScreen(currentDestination)
                .onSuccess { _state.value = DynamicState.Success(it) }
                .onFailure { _state.value = DynamicState.Failure(it.message ?: "Unable to load dynamic screen") }
        }
    }

    private fun handleAction(action: DynamicAction) {
        if (actionJob?.isActive == true) return

        actionJob = scope.launch {
            executeDynamicAction(
                action = action,
                bindings = bindings.toMap(),
                context = contextProvider(),
            ).onSuccess { result ->
                _effects.value = when (result) {
                    is DynamicActionResult.Navigate -> DynamicEffect.Navigate(result.destination)
                    is DynamicActionResult.ExternalUri -> DynamicEffect.ExternalUri(result.uri)
                }
            }.onFailure { error ->
                _state.value = DynamicState.Failure(
                    error.message ?: "Unable to execute dynamic action",
                )
            }
        }
    }

    fun clearEffect() {
        _effects.value = null
    }

    fun close() {
        loadJob?.cancel()
        actionJob?.cancel()
        scope.cancel()
    }
}
