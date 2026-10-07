package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
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
    data class ValueChanged(val key: String, val value: DynamicValue) : DynamicIntent
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
    private val entry: DynamicNavigationEntry,
    private val runtimeState: DynamicRuntimeState,
    private val contextProvider: () -> Map<String, DynamicValue> = { emptyMap() },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val validator = DynamicFormValidator()
    private val screenState = DynamicScreenState()
    private val _state = MutableStateFlow<DynamicState>(
        screenState.response?.let(DynamicState::Success) ?: DynamicState.Initial,
    )
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private val _context = MutableStateFlow(contextProvider())
    val context: StateFlow<Map<String, DynamicValue>> = _context.asStateFlow()

    private val _bindings = MutableStateFlow(screenState.bindings)
    val bindings: StateFlow<Map<String, DynamicValue>> = _bindings.asStateFlow()

    val runtimeValues: StateFlow<Map<String, DynamicValue>> = runtimeState.values

    private val _effects = MutableStateFlow<DynamicEffect?>(null)
    val effects: StateFlow<DynamicEffect?> = _effects.asStateFlow()

    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            is DynamicIntent.ValueChanged -> updateBinding(intent.key, intent.value)
            is DynamicIntent.Action -> handleAction(intent.action, intent.navigation)
        }
    }

    private fun updateBinding(key: String, value: DynamicValue) {
        screenState.bindings = screenState.bindings + (key to value)
        _bindings.value = screenState.bindings
    }

    private fun load() {
        if (screenState.response != null || loadJob?.isActive == true) return
        _state.value = DynamicState.Loading
        loadJob = scope.launch {
            loadDynamicScreen(entry.destination).onSuccess { response ->
                screenState.response = response
                _state.value = DynamicState.Success(response)
            }.onFailure { error ->
                _state.value = DynamicState.Failure(error.message ?: "Unable to load dynamic screen")
            }
        }
    }

    private fun handleAction(action: DynamicAction, navigation: DynamicNavigationState) {
        val shouldValidate = (action.payload["validate"] as? DynamicValue.BooleanValue)?.value == true
        val screen = screenState.response?.data
        if (shouldValidate && screen != null) {
            validator.validate(screen, screenState.bindings)?.let { message ->
                screenState.validationMessage = message
                _state.value = DynamicState.Failure(message)
                return
            }
            screenState.validationMessage = null
        }
        if (actionJob?.isActive == true) return
        actionJob = scope.launch {
            actionExecutor.execute(
                action,
                navigation,
                screenState.bindings,
                _context.value,
                runtimeState.values.value,
            ).onSuccess(::applyActionResult)
                .onFailure { error ->
                    _state.value = DynamicState.Failure(
                        error.message ?: "Unable to execute dynamic action",
                    )
                }
        }
    }

    private fun applyActionResult(result: DynamicActionResult) {
        when (result) {
            DynamicActionResult.Completed -> Unit
            is DynamicActionResult.Navigate -> runtimeState.setAll(result.stateChanges)
            is DynamicActionResult.ExternalUri -> _effects.value = result
            is DynamicActionResult.StateChanged -> runtimeState.setAll(result.changes)
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
