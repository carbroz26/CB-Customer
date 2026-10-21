package com.carbroz.cbpartner.feature.dynamic.store

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicReferenceResolver
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidationResult
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.uuid.Uuid

sealed interface DynamicIntent {
    data object Load : DynamicIntent
    data object Retry : DynamicIntent
    data object ClearMessage : DynamicIntent
    data class Action(
        val action: DynamicAction,
        val presentationId: String? = null,
    ) : DynamicIntent
    data class ValueChanged(val key: String, val value: DynamicValue) : DynamicIntent
    data class FocusChanged(val key: String, val focused: Boolean) : DynamicIntent
    data class PresentationValueChanged(
        val presentationId: String,
        val key: String,
        val value: DynamicValue,
    ) : DynamicIntent
    data class PresentationFocusChanged(
        val presentationId: String,
        val key: String,
        val focused: Boolean,
    ) : DynamicIntent
    data class DismissPresentation(val presentationId: String) : DynamicIntent
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
    private val commitActionResult: (
        Map<String, DynamicValue>,
        List<DynamicActionEffect>,
        TraceContext?,
    ) -> Boolean = { changes, _, _ ->
        runtimeState.setAll(changes)
        true
    },
) {
    private val _state = MutableStateFlow<DynamicState>(DynamicState.Initial)
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private val diagnosticLogger = Logger.withTag("CBPartnerFlow")
    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            DynamicIntent.ClearMessage -> currentScreen()?.let { current ->
                reduce(
                    current.copy(validationMessage = null),
                    persistValidationMessage = current.activePresentationId == null,
                )
            }
            is DynamicIntent.ValueChanged -> updateBinding(intent.key, intent.value)
            is DynamicIntent.FocusChanged -> updateFocus(intent.key, intent.focused)
            is DynamicIntent.PresentationValueChanged ->
                updatePresentationBinding(intent.presentationId, intent.key, intent.value)
            is DynamicIntent.PresentationFocusChanged ->
                updatePresentationFocus(intent.presentationId, intent.key, intent.focused)
            is DynamicIntent.DismissPresentation -> dismissPresentation(intent.presentationId)
            is DynamicIntent.Action -> handleAction(intent.action, intent.presentationId)
        }
    }

    private fun updateBinding(key: String, value: DynamicValue) {
        val current = currentScreen() ?: return
        val bindings = current.bindings + (key to value)
        entry.updateBindings(bindings)

        var next = current.copy(bindings = bindings)
        if (current.validationErrors.isNotEmpty()) {
            next = revalidateScreen(next)
        }
        reduce(next)
    }

    private fun updateFocus(key: String, focused: Boolean) {
        val current = currentScreen() ?: return
        val nextFocused = when {
            focused -> key
            current.focusedBinding == key -> null
            else -> current.focusedBinding
        }
        reduce(current.copy(focusedBinding = nextFocused))
    }

    private fun updatePresentationBinding(
        presentationId: String,
        key: String,
        value: DynamicValue,
    ) {
        val current = currentScreen() ?: return
        if (current.activePresentationId != presentationId) return
        val presentation = findPresentation(current, presentationId) ?: return
        val local = key in presentation.initialBindings

        var next = if (local) {
            current.copy(
                presentationBindings = current.presentationBindings + (key to value),
            )
        } else {
            val bindings = current.bindings + (key to value)
            entry.updateBindings(bindings)
            current.copy(bindings = bindings)
        }

        if (!local && current.validationErrors.isNotEmpty()) {
            next = revalidateScreen(next)
            entry.updateValidationErrors(next.validationErrors)
            entry.updateValidationMessage(next.validationMessage)
        }
        if (current.presentationValidationErrors.isNotEmpty()) {
            next = revalidatePresentation(next, presentation)
        }

        reduce(next, persistValidationMessage = false)
    }

    private fun updatePresentationFocus(
        presentationId: String,
        key: String,
        focused: Boolean,
    ) {
        val current = currentScreen() ?: return
        if (current.activePresentationId != presentationId) return
        val nextFocused = when {
            focused -> key
            current.presentationFocusedBinding == key -> null
            else -> current.presentationFocusedBinding
        }
        reduce(
            current.copy(presentationFocusedBinding = nextFocused),
            persistValidationMessage = false,
        )
    }

    private fun dismissPresentation(presentationId: String) {
        val current = currentScreen() ?: return
        if (current.activePresentationId != presentationId) return
        closeActivePresentation()
    }

    private fun load() {
        if (currentScreen()?.response != null || loadJob?.isActive == true) return
        entry.screenResponse?.let { response ->
            val bindings = seedInitialBindings(response, entry.bindings)
            entry.updateBindings(bindings)
            reduce(
                DynamicScreenState(
                    response = response,
                    bindings = bindings,
                    validationMessage = entry.validationMessage,
                    validationErrors = entry.validationErrors,
                ),
            )
            return
        }

        _state.value = DynamicState.Loading
        loadJob = scope.launch {
            val actionId = Uuid.random().toString()
            reportScreenLoad(actionId, "screen_load.started")
            loadDynamicScreen(
                destination = entry.destination,
                sessionId = entry.diagnosticSessionId,
                actionId = actionId,
                parentActionId = entry.parentActionId,
            )
                .onSuccess { response ->
                    coroutineContext.ensureActive()
                    reportScreenLoad(actionId, "screen_load.completed", "success")
                    entry.updateScreenResponse(response)
                    val bindings = seedInitialBindings(response, entry.bindings)
                    entry.updateBindings(bindings)
                    _state.value = DynamicState.Success(
                        DynamicScreenState(
                            response = response,
                            bindings = bindings,
                        ),
                    )
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    reportScreenLoad(actionId, "screen_load.completed", "failed")
                    val current = currentScreen() ?: DynamicScreenState()
                    _state.value = DynamicState.Failure(
                        "Unable to load dynamic screen",
                        current,
                    )
                }
        }
    }

    private fun seedInitialBindings(
        response: DynamicResponse,
        existing: Map<String, DynamicValue>,
    ): Map<String, DynamicValue> =
        response.data?.initialBindings.orEmpty() + existing

    private fun handleAction(action: DynamicAction, presentationId: String?) {
        if (currentScreen() == null || actionJob?.isActive == true) return

        actionJob = scope.launch {
            val current = currentScreen() ?: return@launch
            val traceContext = entry.diagnosticSessionId?.let { session ->
                TraceContext(session, Uuid.random().toString(), entry.parentActionId)
            }
            reportAction(traceContext, "action.started", action.type)

            val executionBindings = bindingsForOrigin(current, presentationId)
            if (executionBindings == null) {
                reportAction(traceContext, "action.completed", action.type, "rejected")
                return@launch
            }

            val context = contextProvider()
            val stateValues = runtimeState.values.value
            val validateFlag = runCatching {
                if (action.type.trim().equals("request", ignoreCase = true)) {
                    val flag = action.payload["validate"]
                    if (flag == null) {
                        false
                    } else {
                        val resolved = DynamicReferenceResolver.resolve(
                            flag,
                            executionBindings,
                            context,
                            stateValues,
                        )
                        (resolved as? DynamicValue.BooleanValue)?.value
                            ?: error("Invalid dynamic request validate flag")
                    }
                } else {
                    false
                }
            }
            val validationFlagError = validateFlag.exceptionOrNull()
            if (validationFlagError != null) {
                if (validationFlagError is CancellationException) throw validationFlagError
                reportAction(traceContext, "action.completed", action.type, "validation_rejected")
                reduceForOrigin(
                    current.copy(validationMessage = "Unable to process request"),
                    presentationId,
                )
                return@launch
            }

            if (validateFlag.getOrThrow()) {
                val validationResult = runCatching {
                    validateScope(
                        current = current,
                        presentationId = presentationId,
                        bindings = executionBindings,
                        context = context,
                        state = stateValues,
                    )
                }
                val validationError = validationResult.exceptionOrNull()
                if (validationError != null) {
                    if (validationError is CancellationException) throw validationError
                    reportAction(
                        traceContext,
                        "action.completed",
                        action.type,
                        "validation_rejected",
                    )
                    reduceForOrigin(
                        current.copy(validationMessage = "Unable to validate form"),
                        presentationId,
                    )
                    return@launch
                }

                val checked = validationResult.getOrThrow()
                val validatedState = applyValidationResult(current, presentationId, checked)
                reduceForOrigin(validatedState, presentationId)
                if (!checked.isValid) {
                    reportAction(
                        traceContext,
                        "action.completed",
                        action.type,
                        "validation_rejected",
                    )
                    return@launch
                }
            } else {
                reduceForOrigin(current.copy(validationMessage = null), presentationId)
            }

            val latest = currentScreen() ?: return@launch
            val latestBindings = bindingsForOrigin(latest, presentationId) ?: return@launch
            val result = actionExecutor.execute(
                action = action,
                bindings = latestBindings,
                context = context,
                state = stateValues,
                traceContext = traceContext,
            )
            if (result.isSuccess) {
                coroutineContext.ensureActive()
                val accepted = applyActionResult(result.getOrThrow(), traceContext)
                reportAction(
                    traceContext,
                    "action.completed",
                    action.type,
                    if (accepted) "success" else "rejected",
                )
                if (!accepted) {
                    val screen = currentScreen() ?: latest
                    reduceForOrigin(
                        screen.copy(validationMessage = "Unable to complete action"),
                        presentationId,
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                if (error is CancellationException) throw error
                reportAction(traceContext, "action.completed", action.type, "failed")
                val screen = currentScreen() ?: latest
                reduceForOrigin(
                    screen.copy(validationMessage = "Unable to complete action"),
                    presentationId,
                )
            }
        }
    }

    private fun validateScope(
        current: DynamicScreenState,
        presentationId: String?,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValidationResult {
        if (presentationId != null) {
            val presentation = findPresentation(current, presentationId)
                ?: error("Unable to resolve Dynamic presentation")
            return validator.validate(presentation, bindings, context, state)
        }
        val screen = current.response?.data
            ?: error("Unable to resolve Dynamic screen")
        return validator.validate(screen, bindings, context, state)
    }

    private fun applyValidationResult(
        current: DynamicScreenState,
        presentationId: String?,
        result: DynamicValidationResult,
    ): DynamicScreenState =
        if (presentationId == null) {
            current.copy(
                validationErrors = result.errors,
                validationMessage = result.fallbackMessage,
            )
        } else {
            current.copy(
                presentationValidationErrors = result.errors,
                validationMessage = result.fallbackMessage,
            )
        }

    private fun revalidateScreen(current: DynamicScreenState): DynamicScreenState {
        val screen = current.response?.data ?: return current
        return runCatching {
            validator.validate(
                screen = screen,
                bindings = current.bindings,
                context = contextProvider(),
                state = runtimeState.values.value,
            )
        }.fold(
            onSuccess = { result ->
                current.copy(
                    validationErrors = result.errors,
                    validationMessage = result.fallbackMessage,
                )
            },
            onFailure = {
                current.copy(validationMessage = "Unable to validate form")
            },
        )
    }

    private fun revalidatePresentation(
        current: DynamicScreenState,
        presentation: DynamicPresentation,
    ): DynamicScreenState =
        runCatching {
            validator.validate(
                presentation = presentation,
                bindings = current.bindings + current.presentationBindings,
                context = contextProvider(),
                state = runtimeState.values.value,
            )
        }.fold(
            onSuccess = { result ->
                current.copy(
                    presentationValidationErrors = result.errors,
                    validationMessage = result.fallbackMessage,
                )
            },
            onFailure = {
                current.copy(validationMessage = "Unable to validate form")
            },
        )

    private fun bindingsForOrigin(
        current: DynamicScreenState,
        presentationId: String?,
    ): Map<String, DynamicValue>? {
        if (presentationId == null) return current.bindings
        if (current.activePresentationId != presentationId) return null
        return current.bindings + current.presentationBindings
    }

    private fun applyActionResult(
        result: DynamicActionResult,
        traceContext: TraceContext?,
    ): Boolean = when (result) {
        is DynamicActionResult.Present -> openPresentation(result.presentationId)
        DynamicActionResult.Dismiss -> closeActivePresentation()
        else -> commitActionResult(result.stateChanges, result.effects, traceContext)
    }

    private fun openPresentation(presentationId: String): Boolean {
        val current = currentScreen() ?: return false
        if (current.activePresentationId != null) return false
        val presentation = findPresentation(current, presentationId) ?: return false
        reduce(
            current.copy(
                activePresentationId = presentationId,
                presentationBindings = presentation.initialBindings.toMap(),
                presentationValidationErrors = emptyMap(),
                presentationFocusedBinding = null,
            ),
            persistValidationMessage = false,
        )
        return true
    }

    private fun closeActivePresentation(): Boolean {
        val current = currentScreen() ?: return false
        if (current.activePresentationId == null) return false
        reduce(
            current.copy(
                validationMessage = entry.validationMessage,
                activePresentationId = null,
                presentationBindings = emptyMap(),
                presentationValidationErrors = emptyMap(),
                presentationFocusedBinding = null,
            ),
            persistValidationMessage = false,
        )
        return true
    }

    private fun findPresentation(
        current: DynamicScreenState,
        presentationId: String,
    ): DynamicPresentation? =
        current.response?.data?.presentations?.firstOrNull { it.id == presentationId }

    private fun currentScreen(): DynamicScreenState? = when (val value = _state.value) {
        DynamicState.Initial, DynamicState.Loading -> null
        is DynamicState.Success -> value.screen
        is DynamicState.Failure -> value.screen
    }

    private fun reduceForOrigin(
        screen: DynamicScreenState,
        presentationId: String?,
    ) {
        reduce(screen, persistValidationMessage = presentationId == null)
    }

    private fun reduce(
        screen: DynamicScreenState,
        persistValidationMessage: Boolean = true,
    ) {
        if (persistValidationMessage) {
            entry.updateValidationMessage(screen.validationMessage)
        }
        entry.updateValidationErrors(screen.validationErrors)
        _state.value = DynamicState.Success(screen)
    }

    private fun reportAction(
        trace: TraceContext?,
        event: String,
        type: String,
        outcome: String? = null,
    ) {
        val safeType = when (type.trim().lowercase()) {
            "request", "navigate", "present", "dismiss", "external_uri", "state", "sequence" ->
                type.trim().lowercase()
            else -> "other"
        }
        val fields = "event=" + event +
            " sessionId=" + safeDiagnosticId(trace?.sessionId) +
            " actionId=" + safeDiagnosticId(trace?.actionId) +
            " parentActionId=" + safeDiagnosticId(trace?.parentActionId) +
            " type=" + safeType +
            (outcome?.let { " outcome=" + it } ?: "")
        runCatching { diagnosticLogger.i { fields } }
    }

    private fun reportScreenLoad(
        actionId: String,
        event: String,
        outcome: String? = null,
    ) {
        val fields = "event=" + event +
            " phase=screen_load sessionId=" + safeDiagnosticId(entry.diagnosticSessionId) +
            " actionId=" + safeDiagnosticId(actionId) +
            " parentActionId=" + safeDiagnosticId(entry.parentActionId) +
            (outcome?.let { " outcome=" + it } ?: "")
        runCatching { diagnosticLogger.d { fields } }
    }

    fun close() {
        loadJob?.cancel()
        actionJob?.cancel()
        scope.cancel()
    }
}
