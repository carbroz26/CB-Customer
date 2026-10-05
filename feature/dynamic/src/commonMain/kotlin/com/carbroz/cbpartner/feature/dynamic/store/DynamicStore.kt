package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationState
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
    data class Action(val action: DynamicAction, val navigation: DynamicNavigationState) : DynamicIntent
    data class ValueChanged(val key: String, val value: String) : DynamicIntent
}

sealed interface DynamicEffect {
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
    private val actionExecutor: DynamicActionExecutor,
    destination: DynamicDestination,
    private val contextProvider: () -> Map<String, String> = { emptyMap() },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _state = MutableStateFlow<DynamicState>(DynamicState.Initial)
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private val _bindings = MutableStateFlow<Map<String, String>>(emptyMap())
    val bindings: StateFlow<Map<String, String>> = _bindings.asStateFlow()

    private val _effects = MutableStateFlow<DynamicEffect?>(null)
    val effects: StateFlow<DynamicEffect?> = _effects.asStateFlow()

    private var currentDestination = destination
    private var previousResponseData: DynamicValue? = null
    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun updateDestination(destination: DynamicDestination) {
        if (currentDestination == destination) return
        currentDestination = destination
        _bindings.value = emptyMap()
        previousResponseData = null
        loadJob?.cancel()
        actionJob?.cancel()
        _state.value = DynamicState.Initial
        _effects.value = null
    }

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            is DynamicIntent.ValueChanged -> _bindings.value = _bindings.value + (intent.key to intent.value)
            is DynamicIntent.Action -> handleAction(intent.action, intent.navigation)
        }
    }

    private fun load() {
        if (loadJob?.isActive == true) return
        _state.value = DynamicState.Loading
        loadJob = scope.launch {
            loadDynamicScreen(currentDestination)
                .onSuccess { response ->
                    previousResponseData = response.asResponseReference()
                    _state.value = DynamicState.Success(response)
                }
                .onFailure { error -> _state.value = DynamicState.Failure(error.message ?: "Unable to load dynamic screen") }
        }
    }

    private fun handleAction(action: DynamicAction, navigation: DynamicNavigationState) {
        if (actionJob?.isActive == true) return
        actionJob = scope.launch {
            actionExecutor.execute(
                action = action,
                navigation = navigation,
                bindings = _bindings.value,
                context = contextProvider(),
                previousResponse = previousResponseData,
            ).onSuccess { result ->
                when (result) {
                    DynamicActionResult.Completed -> Unit
                    is DynamicActionResult.Navigate -> result.response?.let { previousResponseData = it.asResponseReference() }
                    is DynamicActionResult.ExternalUri -> _effects.value = DynamicEffect.ExternalUri(result.uri)
                    is DynamicActionResult.StateChanged -> _bindings.value = _bindings.value + (result.key to result.value)
                }
            }.onFailure { error -> _state.value = DynamicState.Failure(error.message ?: "Unable to execute dynamic action") }
        }
    }

    private fun DynamicResponse.asResponseReference(): DynamicValue? =
        dataPayload?.let { DynamicValue.ObjectValue(mapOf("data" to it)) }

    fun clearEffect() { _effects.value = null }

    fun close() {
        loadJob?.cancel()
        actionJob?.cancel()
        scope.cancel()
    }
}
