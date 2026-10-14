package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface DynamicIntent {
    data object Load : DynamicIntent
    data object Retry : DynamicIntent
    data class Action(val action: DynamicAction) : DynamicIntent
    data class ValueChanged(val key: String, val value: DynamicValue) : DynamicIntent
}

sealed interface DynamicState {
    data object Initial : DynamicState
    data object Loading : DynamicState
    data class Success(val screen: DynamicScreenState) : DynamicState
    data class Failure(val message: String, val screen: DynamicScreenState) : DynamicState
}

class DynamicStore(
    private val loadDynamicScreen: LoadDynamicScreenUseCase,
    private val actionExecutor: DynamicActionExecutor,
    private val validator: DynamicFormValidator,
    private val entry: DynamicNavigationEntry,
    private val runtimeState: DynamicRuntimeState,
    private val contextProvider: () -> Map<String, DynamicValue> = { emptyMap() },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) {
    private val _state = MutableStateFlow<DynamicState>(DynamicState.Initial)
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private val _effects = Channel<DynamicActionEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            is DynamicIntent.ValueChanged -> updateBinding(intent.key, intent.value)
            is DynamicIntent.Action -> handleAction(intent.action)
        }
    }

    private fun updateBinding(key: String, value: DynamicValue) {
        val current = currentScreen() ?: return
        reduce(current.copy(bindings = current.bindings + (key to value)))
    }

    private fun load() {
        if (currentScreen()?.response != null || loadJob?.isActive == true) return
        _state.value = DynamicState.Loading
        loadJob = scope.launch {
            loadDynamicScreen(entry.destination)
                .onSuccess { response ->
                    val previous = currentScreen()
                    _state.value = DynamicState.Success(
                        DynamicScreenState(
                            response = response,
                            bindings = previous?.bindings.orEmpty(),
                            validationMessage = null,
                        ),
                    )
                }
                .onFailure { error ->
                    val current = currentScreen() ?: DynamicScreenState()
                    _state.value = DynamicState.Failure(
                        error.message ?: "Unable to load dynamic screen",
                        current,
                    )
                }
        }
    }

    private fun handleAction(action: DynamicAction) {
        val current = currentScreen() ?: return
        if (requiresValidation(action)) {
            val screen = current.response?.data
            if (screen != null) {
                val message = validator.validate(screen, current.bindings)
                if (message != null) {
                    reduce(current.copy(validationMessage = message))
                    return
                }
                reduce(current.copy(validationMessage = null))
            }
        }

        if (actionJob?.isActive == true) return
        actionJob = scope.launch {
            val result = actionExecutor.execute(
                action = action,
                bindings = current.bindings,
                context = contextProvider(),
                state = runtimeState.values.value,
            )
            if (result.isSuccess) {
                applyActionResult(result.getOrThrow())
            } else {
                val error = result.exceptionOrNull()
                val screen = currentScreen() ?: current
                _state.value = DynamicState.Failure(
                    error?.message ?: "Unable to execute dynamic action",
                    screen,
                )
            }
        }
    }

    private fun requiresValidation(action: DynamicAction): Boolean =
        (action.payload["validate"] as? DynamicValue.BooleanValue)?.value == true

    private suspend fun applyActionResult(result: DynamicActionResult) {
        runtimeState.setAll(result.stateChanges)
        result.effects.forEach(::emitEffect)
    }

    private suspend fun emitEffect(effect: DynamicActionEffect) {
        try {
            _effects.send(effect)
        } catch (_: ClosedSendChannelException) {
            // Disposal closes the channel to release any suspended senders.
        }
    }

    private fun currentScreen(): DynamicScreenState? = when (val value = _state.value) {
        DynamicState.Initial, DynamicState.Loading -> null
        is DynamicState.Success -> value.screen
        is DynamicState.Failure -> value.screen
    }

    private fun reduce(screen: DynamicScreenState) {
        _state.value = DynamicState.Success(screen)
    }

    fun close() {
        loadJob?.cancel()
        actionJob?.cancel()
        _effects.close()
        scope.cancel()
    }
}
