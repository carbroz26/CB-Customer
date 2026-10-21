package com.carbroz.cbpartner.feature.dynamic.store

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
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
    data class PresentationValueChanged(
        val presentationId: String,
        val key: String,
        val value: DynamicValue,
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
    private val commitActionResult: (Map<String, DynamicValue>, List<DynamicActionEffect>, TraceContext?) -> Boolean =
        { changes, _, _ -> runtimeState.setAll(changes); true },
) {
    private val _state = MutableStateFlow<DynamicState>(DynamicState.Initial)
    val state: StateFlow<DynamicState> = _state.asStateFlow()

    private val diagnosticLogger = Logger.withTag("CBPartnerFlow")

    private fun reportAction(trace: TraceContext?, event: String, type: String, outcome: String? = null) {
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

    private fun reportScreenLoad(actionId: String, event: String, outcome: String? = null) {
        val fields = "event=" + event +
            " phase=screen_load sessionId=" + safeDiagnosticId(entry.diagnosticSessionId) +
            " actionId=" + safeDiagnosticId(actionId) +
            " parentActionId=" + safeDiagnosticId(entry.parentActionId) +
            (outcome?.let { " outcome=" + it } ?: "")
        runCatching { diagnosticLogger.d { fields } }
    }

    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun accept(intent: DynamicIntent) {
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            DynamicIntent.ClearMessage -> currentScreen()?.let { reduce(it.copy(validationMessage = null)) }
            is DynamicIntent.ValueChanged -> updateBinding(intent.key, intent.value)
            is DynamicIntent.PresentationValueChanged ->
                updatePresentationBinding(intent.presentationId, intent.key, intent.value)
            is DynamicIntent.DismissPresentation -> dismissPresentation(intent.presentationId)
            is DynamicIntent.Action -> handleAction(intent.action, intent.presentationId)
        }
    }

    private fun updateBinding(key: String, value: DynamicValue) {
        val current = currentScreen() ?: return
        val bindings = current.bindings + (key to value)
        entry.updateBindings(bindings)
        reduce(current.copy(bindings = bindings))
    }

    private fun updatePresentationBinding(
        presentationId: String,
        key: String,
        value: DynamicValue,
    ) {
        val current = currentScreen() ?: return
        if (current.activePresentationId != presentationId) return
        val presentation = findPresentation(current, presentationId) ?: return

        if (key in presentation.initialBindings) {
            reduce(current.copy(presentationBindings = current.presentationBindings + (key to value)))
        } else {
            updateBinding(key, value)
        }
    }

    private fun dismissPresentation(presentationId: String) {
        val current = currentScreen() ?: return
        if (current.activePresentationId != presentationId) return
        reduce(
            current.copy(
                activePresentationId = null,
                presentationBindings = emptyMap(),
            ),
        )
    }

    private fun load() {
        if (currentScreen()?.response != null || loadJob?.isActive == true) return
        entry.screenResponse?.let { response ->
            reduce(DynamicScreenState(response, entry.bindings, entry.validationMessage))
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
                    reportScreenLoad(actionId, "screen_load.completed", "failed")
                    val current = currentScreen() ?: DynamicScreenState()
                    _state.value = DynamicState.Failure(
                        "Unable to load dynamic screen",
                        current,
                    )
                }
        }
    }

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
            val validationResult = runCatching {
                if (action.type.trim().equals("request", ignoreCase = true)) {
                    val flag = action.payload["validate"]
                    if (flag == null) false else {
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
            val validationError = validationResult.exceptionOrNull()
            if (validationError != null) {
                if (validationError is CancellationException) throw validationError
                reportAction(traceContext, "action.completed", action.type, "validation_rejected")
                reduce(current.copy(validationMessage = "Unable to process request"))
                return@launch
            }

            if (validationResult.getOrThrow()) {
                val message = validationMessage(current, presentationId, executionBindings)
                if (message != null) {
                    reportAction(traceContext, "action.completed", action.type, "validation_rejected")
                    reduce(current.copy(validationMessage = message))
                    return@launch
                }
            }

            reduce(current.copy(validationMessage = null))
            val result = actionExecutor.execute(
                action = action,
                bindings = executionBindings,
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
                    val screen = currentScreen() ?: current
                    reduce(screen.copy(validationMessage = "Unable to complete action"))
                }
            } else {
                val error = result.exceptionOrNull()
                if (error is CancellationException) throw error
                reportAction(traceContext, "action.completed", action.type, "failed")
                val screen = currentScreen() ?: current
                reduce(screen.copy(validationMessage = "Unable to complete action"))
            }
        }
    }

    private fun validationMessage(
        current: DynamicScreenState,
        presentationId: String?,
        bindings: Map<String, DynamicValue>,
    ): String? {
        if (presentationId != null) {
            val presentation = findPresentation(current, presentationId)
                ?: return "Unable to validate form"
            return validator.validate(presentation, bindings)
        }
        val screen = current.response?.data ?: return "Unable to validate form"
        return validator.validate(screen, bindings)
    }

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
            ),
        )
        return true
    }

    private fun closeActivePresentation(): Boolean {
        val current = currentScreen() ?: return false
        if (current.activePresentationId == null) return false
        reduce(
            current.copy(
                activePresentationId = null,
                presentationBindings = emptyMap(),
            ),
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

    private fun reduce(screen: DynamicScreenState) {
        entry.updateValidationMessage(screen.validationMessage)
        _state.value = DynamicState.Success(screen)
    }

    fun close() {
        loadJob?.cancel()
        actionJob?.cancel()
        scope.cancel()
    }
}
