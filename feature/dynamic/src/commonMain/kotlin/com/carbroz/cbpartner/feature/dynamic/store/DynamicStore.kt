package com.carbroz.cbpartner.feature.dynamic.store

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicReferenceResolver
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import kotlinx.coroutines.CancellationException
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
    data object ClearMessage : DynamicIntent
    data class EffectFailed(val message: String) : DynamicIntent
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
    private val deliverEffects: suspend (List<DynamicActionEffect>) -> Unit = {},
) {
    private val _state = MutableStateFlow<DynamicState>(DynamicState.Initial)
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            DynamicIntent.ClearMessage -> currentScreen()?.let { reduce(it.copy(validationMessage = null)) }
            is DynamicIntent.EffectFailed -> reportEffectFailure(intent.message)
            is DynamicIntent.ValueChanged -> updateBinding(intent.key, intent.value)
            is DynamicIntent.Action -> handleAction(intent.action)
        }
    }

    private fun updateBinding(key: String, value: DynamicValue) {
        val current = currentScreen() ?: return
        val bindings = current.bindings + (key to value)
        entry.updateBindings(bindings)
        reduce(current.copy(bindings = bindings))
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
                            bindings = previous?.bindings ?: entry.bindings,
                            validationMessage = null,
                        ),
                    )
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
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
        if (actionJob?.isActive == true) return

        actionJob = scope.launch {
            val context = contextProvider()
            val stateValues = runtimeState.values.value
            val validationResult = runCatching {
                if (action.type.trim().equals("request", ignoreCase = true)) {
                    val flag = action.payload["validate"]
                    if (flag == null) false else {
                        val resolved = DynamicReferenceResolver.resolve(flag, current.bindings, context, stateValues)
                        (resolved as? DynamicValue.BooleanValue)?.value
                            ?: error("Invalid dynamic request validate flag")
                    }
                } else false
            }
            val validationError = validationResult.exceptionOrNull()
            if (validationError != null) {
                if (validationError is CancellationException) throw validationError
                reduce(current.copy(validationMessage = "Unable to process request"))
                return@launch
            }

            if (validationResult.getOrThrow()) {
                val screen = current.response?.data
                if (screen == null) {
                    reduce(current.copy(validationMessage = "Unable to validate form"))
                    return@launch
                }
                val message = validator.validate(screen, current.bindings)
                if (message != null) {
                    reduce(current.copy(validationMessage = message))
                    return@launch
                }
            }
            reduce(current.copy(validationMessage = null))
            val result = actionExecutor.execute(
                action = action,
                bindings = current.bindings,
                context = context,
                state = stateValues,
            )
            if (result.isSuccess) {
                applyActionResult(result.getOrThrow())
            } else {
                val error = result.exceptionOrNull()
                if (error is CancellationException) throw error
                val screen = currentScreen() ?: current
                reduce(screen.copy(validationMessage = "Unable to complete action"))
            }
        }
    }

    private suspend fun applyActionResult(result: DynamicActionResult) {
        runtimeState.setAll(result.stateChanges)
        if (result.effects.isNotEmpty()) {
            deliverEffects(result.effects.toList())
        }
    }

    private fun reportEffectFailure(message: String) {
        val screen = currentScreen() ?: return
        _state.value = DynamicState.Failure(message, screen)
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
        scope.cancel()
    }
}
