package com.carbroz.cbpartner.feature.dynamic.store

import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.ToastPresentation
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicListItems
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicReferenceResolver
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicCountdownClock
import com.carbroz.cbpartner.feature.dynamic.runtime.countdownState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.time.TimeSource
import kotlin.uuid.Uuid

sealed interface DynamicIntent {
    data object Load : DynamicIntent
    data object Retry : DynamicIntent
    data object ClearMessage : DynamicIntent
    data class RetryResponseDialog(val generation: Long) : DynamicIntent
    data class DismissResponseDialog(val generation: Long) : DynamicIntent
    data class Action(
        val action: DynamicAction,
        val presentationId: String? = null,
        val buttonId: String? = null,
        val presentationGeneration: Long? = null,
    ) : DynamicIntent
    data class ValueChanged(val key: String, val value: DynamicValue) : DynamicIntent
    data class FocusChanged(val key: String, val focused: Boolean) : DynamicIntent
    data class PresentationValueChanged(
        val presentationId: String,
        val key: String,
        val value: DynamicValue,
        val presentationGeneration: Long? = null,
    ) : DynamicIntent
    data class PresentationFocusChanged(
        val presentationId: String,
        val key: String,
        val focused: Boolean,
        val presentationGeneration: Long? = null,
    ) : DynamicIntent
    /** The row identity is rechecked against the latest entry/presentation/catalog snapshot. */
    data class ListItemSelected(
        val entryId: Long,
        val presentationId: String?,
        val presentationGeneration: Long?,
        val listSectionId: String,
        val itemKey: String,
    ) : DynamicIntent

    data class DismissPresentation(
        val presentationId: String,
        val presentationGeneration: Long? = null,
    ) : DynamicIntent
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
    private val listCatalog: DynamicListCatalog = DynamicListCatalog { null },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val timeSource: TimeSource = TimeSource.Monotonic,
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
    private val listItems = DynamicListItems(listCatalog)
    private var loadJob: Job? = null
    private var countdownJob: Job? = null
    private var actionJob: Job? = null
    private var requestPresentationId: String? = null
    private var requestPresentationGeneration: Long? = null
    private var closed = false

    fun accept(intent: DynamicIntent) {
        if (closed) return
        when (intent) {
            DynamicIntent.Load, DynamicIntent.Retry -> load()
            is DynamicIntent.RetryResponseDialog -> retryResponseDialog(intent.generation)
            is DynamicIntent.DismissResponseDialog -> dismissResponseDialog(intent.generation)
            DynamicIntent.ClearMessage -> currentScreen()?.let { current ->
                reduce(
                    current.copy(validationMessage = null),
                    persistValidationMessage = current.activePresentationId == null,
                )
            }
            is DynamicIntent.ValueChanged -> updateBinding(intent.key, intent.value)
            is DynamicIntent.FocusChanged -> updateFocus(intent.key, intent.focused)
            is DynamicIntent.PresentationValueChanged ->
                updatePresentationBinding(intent.presentationId, intent.key, intent.value, intent.presentationGeneration)
            is DynamicIntent.PresentationFocusChanged ->
                updatePresentationFocus(intent.presentationId, intent.key, intent.focused, intent.presentationGeneration)
            is DynamicIntent.ListItemSelected -> selectListItem(intent)
            is DynamicIntent.DismissPresentation ->
                dismissPresentation(intent.presentationId, intent.presentationGeneration)
            is DynamicIntent.Action -> handleAction(
                intent.action,
                intent.presentationId,
                intent.buttonId,
                intent.presentationGeneration,
            )
        }
    }

    /** Atomic screen-binding selection; stale rows never mutate bindings or dismiss overlays. */
    private fun selectListItem(intent: DynamicIntent.ListItemSelected) {
        if (intent.entryId != entry.entryId || actionJob?.isActive == true) return
        val current = currentScreen() ?: return
        if (intent.presentationId == null) {
            if (current.activePresentationId != null || intent.presentationGeneration != null) return
        } else if (intent.presentationGeneration == null ||
            !matchesCurrentPresentation(current, intent.presentationId, intent.presentationGeneration)
        ) return

        val components = if (intent.presentationId == null) {
            val template = current.response?.data?.template ?: return
            template.header?.components.orEmpty() +
                template.components + template.footer?.components.orEmpty()
        } else {
            findPresentation(current, intent.presentationId)?.components ?: return
        }
        val sections = components.flatMap { component -> component.sections }
        val list = sections.filterIsInstance<ListSection>()
            .filter { it.id == intent.listSectionId }
            .singleOrNull() ?: return
        val selection = list.selection ?: return
        val bindings = bindingsForOrigin(current, intent.presentationId) ?: return

        // Evaluation performs required-field/duplicate-key checks for the complete source.
        val changes = runCatching {
            val row = listItems.evaluate(list, bindings).singleOrNull { it.key == intent.itemKey }
                ?: return@runCatching null
            selection.result.mapValues { (_, field) ->
                requireNotNull(row.item[field]) { "Dynamic list result field missing" }
            }
        }.getOrNull() ?: return
        val updatedBindings = current.bindings + changes
        val dismiss = intent.presentationId != null && selection.dismissOnSelect
        var next = current.copy(
            bindings = updatedBindings,
            activePresentationId = if (dismiss) null else current.activePresentationId,
            presentationBindings = if (dismiss) emptyMap() else current.presentationBindings,
            presentationValidationErrors = if (dismiss) emptyMap() else current.presentationValidationErrors,
            presentationFocusedBinding = if (dismiss) null else current.presentationFocusedBinding,
            validationMessage = if (dismiss) entry.validationMessage else current.validationMessage,
        )
        if (current.validationErrors.isNotEmpty()) next = revalidateScreen(next)
        entry.updateBindings(updatedBindings)
        reduce(next, persistValidationMessage = intent.presentationId == null)
    }

    private fun updateBinding(key: String, value: DynamicValue) {
        val current = currentScreen() ?: return
        val bindings = current.bindings + (key to value)
        entry.updateBindings(bindings)

        var next = current.copy(bindings = bindings)
        if (current.validationErrors.isNotEmpty()) {
            val clearEditedError = current.response?.data?.let {
                validator.clearsErrorOnEdit(it, key)
            } == true
            next = revalidateScreen(next, suppressedBinding = key.takeIf { clearEditedError })
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
        presentationGeneration: Long?,
    ) {
        val current = currentScreen() ?: return
        if (!matchesCurrentPresentation(current, presentationId, presentationGeneration)) return
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
        presentationGeneration: Long?,
    ) {
        val current = currentScreen() ?: return
        if (!matchesCurrentPresentation(current, presentationId, presentationGeneration)) return
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

    private fun dismissPresentation(presentationId: String, presentationGeneration: Long?) {
        val current = currentScreen() ?: return
        if (!matchesCurrentPresentation(current, presentationId, presentationGeneration)) return
        closeActivePresentation()
    }

    private fun matchesCurrentPresentation(
        current: DynamicScreenState,
        id: String,
        generation: Long?,
    ): Boolean = current.activePresentationId == id &&
        (generation == null || current.presentationGeneration == generation)

    /** Retains operation identity and never automatically retries a mutation. */
    private fun retryResponseDialog(generation: Long) {
        val before = _state.value
        val current = currentScreen() ?: return
        if (current.dialogGeneration != generation ||
            (current.dialogFailure == null && current.activeResponseDialog == null)) return
        val action = current.retryAction
        val origin = current.retryPresentationId
        val originGeneration = current.retryPresentationGeneration
        val cleaned = current.copy(
            dialogFailure = null, activeResponseDialog = null, retryAction = null,
            retryPresentationId = null, retryPresentationGeneration = null,
        )
        if (before is DynamicState.Failure && cleaned.response == null) {
            _state.value = DynamicState.Initial
            load()
        } else {
            reduce(cleaned)
            if (action != null) handleAction(action, origin, null, originGeneration)
        }
    }

    private fun dismissResponseDialog(generation: Long) {
        val current = currentScreen() ?: return
        if (current.dialogGeneration != generation) return
        reduce(current.copy(
            dialogFailure = null, activeResponseDialog = null, retryAction = null,
            retryPresentationId = null, retryPresentationGeneration = null,
        ))
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
            startCountdowns(response)
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
                    startCountdowns(response)
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    reportScreenLoad(actionId, "screen_load.completed", "failed")
                    val current = currentScreen() ?: DynamicScreenState()
                    _state.value = DynamicState.Failure(
                        "Unable to load dynamic screen",
                        current.copy(
                            dialogFailure = error as? AppFailure,
                            dialogGeneration = current.dialogGeneration + if (error is AppFailure) 1 else 0,
                        ),
                    )
                }
        }
    }

    /** One countdown job per screen entry, based on retained backend seconds. */
    private fun startCountdowns(response: DynamicResponse) {
        countdownJob?.cancel()
        val definitions = response.data?.countdowns.orEmpty()
        if (definitions.isEmpty()) return
        val retained = runtimeState.values.value
        val clocks = definitions.associate { definition ->
            val raw = DynamicReferenceResolver.resolvePath(retained, definition.durationStatePath)
            val seconds = (raw as? DynamicValue.NumberValue)?.value
            val clock = seconds?.takeIf {
                it.isFinite() && it >= 0.0 && it <= 86400.0 && it % 1.0 == 0.0
            }?.toLong()?.let { DynamicCountdownClock(it, timeSource.markNow()) }
            definition.id to clock
        }
        fun publish() {
            val current = _state.value as? DynamicState.Success ?: return
            val values = countdownState(clocks)
            if (current.screen.transientState != values) {
                _state.value = DynamicState.Success(current.screen.copy(transientState = values))
            }
        }
        publish()
        if (clocks.values.all { it == null || it.remainingSeconds() == 0L }) return
        countdownJob = scope.launch {
            while (isActive) {
                delay(250)
                publish()
                if (clocks.values.all { it == null || it.remainingSeconds() == 0L }) break
            }
        }
    }

    private fun resolvedState(screen: DynamicScreenState): Map<String, DynamicValue> =
        runtimeState.values.value + screen.transientState

    private fun actionEnabled(action: DynamicAction, screen: DynamicScreenState): Boolean {
        val condition = action.payload["enabledWhen"] ?: return true
        return runCatching {
            (DynamicReferenceResolver.resolve(
                condition,
                screen.bindings,
                contextProvider(),
                resolvedState(screen),
            ) as? DynamicValue.BooleanValue)?.value == true
        }.getOrDefault(false)
    }

    private fun seedInitialBindings(
        response: DynamicResponse,
        existing: Map<String, DynamicValue>,
    ): Map<String, DynamicValue> =
        response.data?.initialBindings.orEmpty() + existing

    private fun handleAction(
        action: DynamicAction,
        presentationId: String?,
        buttonId: String?,
        presentationGeneration: Long?,
    ) {
        val acceptedScreen = currentScreen() ?: return
        if (!actionEnabled(action, acceptedScreen)) return
        if (actionJob?.isActive == true) return
        if (presentationId != null &&
            (acceptedScreen.activePresentationId != presentationId ||
                (presentationGeneration != null &&
                    acceptedScreen.presentationGeneration != presentationGeneration))
        ) return
        if (presentationId == null && presentationGeneration != null) return

        val origin = if (buttonId != null) {
            if (buttonId.isBlank() || action.type.trim().lowercase() != "request") return
            if (presentationId != null && presentationGeneration == null) return
            val components = if (presentationId == null) {
                val template = acceptedScreen.response?.data?.template ?: return
                template.header?.components.orEmpty() +
                    template.components + template.footer?.components.orEmpty()
            } else {
                findPresentation(acceptedScreen, presentationId)?.components ?: return
            }
            val button = findButton(components, buttonId)
            if (button?.loading == null || button.actions["onClick"] != action) return
            DynamicPendingButton(buttonId, presentationId, presentationGeneration)
        } else {
            null
        }

        // Capture Presentation-open identity before scheduling on any dispatcher.
        val acceptedGeneration = if (presentationId != null) acceptedScreen.presentationGeneration else null
        if (presentationId != null && action.type.trim().lowercase() == "request") {
            requestPresentationId = presentationId
            requestPresentationGeneration = acceptedGeneration
        }

        actionJob = scope.launch {
            try {
                val current = currentScreen() ?: return@launch
                if (!matchesPresentation(current, presentationId, acceptedGeneration)) return@launch
                if (!actionEnabled(action, current)) return@launch
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
                val stateValues = resolvedState(current)
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
                if (!matchesPresentation(latest, presentationId, acceptedGeneration)) return@launch
                val latestBindings = bindingsForOrigin(latest, presentationId) ?: return@launch
                if (origin != null) {
                    val active = currentScreen() ?: return@launch
                    if (!matchesPresentation(active, presentationId, acceptedGeneration)) return@launch
                    reduceForOrigin(active.copy(pendingButton = origin), presentationId)
                }

                if (!actionEnabled(action, latest)) return@launch
                val result = actionExecutor.execute(
                    action = action,
                    bindings = latestBindings,
                    context = context,
                    state = stateValues,
                    traceContext = traceContext,
                )
                if (result.isSuccess) {
                    coroutineContext.ensureActive()
                    if (!matchesPresentation(currentScreen(), presentationId, acceptedGeneration)) return@launch
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
                    coroutineContext.ensureActive()
                    if (!matchesPresentation(currentScreen(), presentationId, acceptedGeneration)) return@launch
                    reportAction(traceContext, "action.completed", action.type, "failed")
                    val screen = currentScreen() ?: latest
                    val failure = error as? AppFailure
                    if (failure != null && (failure.category in setOf(
                        AppFailure.Category.CONNECTION,
                        AppFailure.Category.SERVER,
                        AppFailure.Category.SECURE_CONNECTION,
                        AppFailure.Category.UNKNOWN,
                    ) || failure.presentation != null)) {
                        reduceForOrigin(
                            screen.copy(
                                dialogFailure = failure,
                                activeResponseDialog = failure.presentation,
                                retryAction = action,
                                retryPresentationId = presentationId,
                                retryPresentationGeneration = acceptedGeneration,
                                dialogGeneration = screen.dialogGeneration + 1,
                                validationMessage = null,
                            ),
                            presentationId,
                        )
                    } else {
                        reduceForOrigin(
                            screen.copy(validationMessage = "Unable to complete action"),
                            presentationId,
                        )
                    }
                }
            } finally {
                if (requestPresentationId == presentationId &&
                    requestPresentationGeneration == acceptedGeneration) {
                    requestPresentationId = null
                    requestPresentationGeneration = null
                }
                if (!closed && origin != null) {
                    val active = currentScreen()
                    if (active?.pendingButton == origin &&
                        matchesPresentation(active, presentationId, acceptedGeneration)) {
                        reduceForOrigin(active.copy(pendingButton = null), presentationId)
                    }
                }
            }
        }
    }

    private fun matchesPresentation(
        current: DynamicScreenState?,
        presentationId: String?,
        generation: Long?,
    ): Boolean {
        if (current == null) return false
        if (presentationId == null) return true
        return current.activePresentationId == presentationId &&
            current.presentationGeneration == generation
    }

    /** Lookup for the supported static Button tree only; repeated ListSection rows are deferred. */
    private fun findButton(components: List<DynamicComponent>, id: String): ButtonElement? {
        val matches = mutableListOf<ButtonElement>()
        components.forEach { component ->
            matches += component.elements.filterIsInstance<ButtonElement>().filter { it.id == id }
            component.sections.forEach { section ->
                matches += section.elements.filterIsInstance<ButtonElement>().filter { it.id == id }
                section.groups.forEach { group ->
                    matches += group.elements.filterIsInstance<ButtonElement>().filter { it.id == id }
                }
            }
        }
        // Defensive runtime guard for direct-domain callers that bypass DTO mapping.
        return matches.singleOrNull()
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

    private fun revalidateScreen(
        current: DynamicScreenState,
        suppressedBinding: String? = null,
    ): DynamicScreenState {
        val screen = current.response?.data ?: return current
        return runCatching {
            validator.validate(
                screen = screen,
                bindings = current.bindings,
                context = contextProvider(),
                state = resolvedState(current),
                suppressedBinding = suppressedBinding,
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
        is DynamicActionResult.Present -> openPresentation(result.presentationId, traceContext)
        is DynamicActionResult.SuccessPresentation -> {
            val screen = currentScreen()
            val toast = screen?.let { findPresentation(it, result.presentationId) as? ToastPresentation }
            if (screen == null || toast == null) false
            else emitToast(toast, result.stateChanges, traceContext)
        }
        is DynamicActionResult.Navigate -> {
            val effects = if (result.successPresentationId == null) {
                result.effects
            } else {
                val source = currentScreen()
                val toast = source?.let {
                    findPresentation(it, result.successPresentationId) as? ToastPresentation
                } ?: return false
                // Evaluate the authored presentation against the original Login screen,
                // before the navigation effect changes the active destination.
                listOf(
                    DynamicActionEffect.Navigate(result.destination, result.operation),
                    toastEffect(toast, result.stateChanges),
                )
            }
            val accepted = commitActionResult(result.stateChanges, effects, traceContext)
            if (accepted && result.stateChanges.isNotEmpty()) {
                val response = currentScreen()?.response
                if (response?.data?.countdowns?.any {
                        it.durationStatePath in result.stateChanges
                    } == true) {
                    startCountdowns(response)
                }
            }
            accepted
        }
        DynamicActionResult.Dismiss -> closeActivePresentation()
        is DynamicActionResult.PresentResponse -> when (val presentation = result.presentation) {
            is ToastPresentation -> emitToast(presentation, result.stateChanges, traceContext)
            is DialogPresentation -> {
                val current = currentScreen()
                if (current == null) false
                else {
                    val accepted = commitActionResult(result.stateChanges, emptyList(), traceContext)
                    if (accepted) {
                        reduce(current.copy(
                            activeResponseDialog = presentation,
                            dialogFailure = null,
                            dialogGeneration = current.dialogGeneration + 1,
                        ))
                    }
                    accepted
                }
            }
            else -> false
        }
        else -> {
            val accepted = commitActionResult(result.stateChanges, result.effects, traceContext)
            if (accepted && result.stateChanges.isNotEmpty()) {
                val response = currentScreen()?.response
                if (response?.data?.countdowns?.any { it.durationStatePath in result.stateChanges } == true) {
                    startCountdowns(response)
                }
            }
            accepted
        }
    }

    /** Existing present contract: Toast emits an ordered effect without occupying a modal slot. */
    private fun openPresentation(presentationId: String, traceContext: TraceContext?): Boolean {
        val current = currentScreen() ?: return false
        if (current.activePresentationId != null) return false
        val presentation = findPresentation(current, presentationId) ?: return false
        if (presentation is ToastPresentation) return emitToast(presentation, emptyMap(), traceContext)
        reduce(
            current.copy(
                activePresentationId = presentationId,
                presentationGeneration = current.presentationGeneration + 1,
                presentationBindings = presentation.initialBindings.toMap(),
                presentationValidationErrors = emptyMap(),
                presentationFocusedBinding = null,
            ),
            persistValidationMessage = false,
        )
        return true
    }

    /** Commit state before app Toast effect becomes visible; never send a second network request. */
    private fun emitToast(
        presentation: ToastPresentation,
        changes: Map<String, DynamicValue>,
        trace: TraceContext?,
    ): Boolean {
        if (currentScreen() == null) return false
        return commitActionResult(
            changes,
            listOf(toastEffect(presentation, changes)),
            trace,
        ).also { accepted ->
            if (accepted && changes.isNotEmpty()) {
                val response = currentScreen()?.response
                if (response?.data?.countdowns?.any { it.durationStatePath in changes } == true) {
                    startCountdowns(response)
                }
            }
        }
    }

    /** One immutable source-screen snapshot, shared by Resend and Login navigation. */
    private fun toastEffect(
        presentation: ToastPresentation,
        changes: Map<String, DynamicValue>,
    ): DynamicActionEffect.Toast {
        val source = requireNotNull(currentScreen())
        return DynamicActionEffect.Toast(
            presentation = presentation,
            bindings = (
                source.bindings + source.presentationBindings + presentation.initialBindings
            ).toMap(),
            contextValues = contextProvider().toMap(),
            stateValues = (resolvedState(source) + changes).toMap(),
        )
    }

    private fun closeActivePresentation(): Boolean {
        val current = currentScreen() ?: return false
        if (current.activePresentationId == null) return false
        if (requestPresentationId == current.activePresentationId &&
            requestPresentationGeneration == current.presentationGeneration) {
            // Cancelling request ownership prevents stale completion/effect commits.
            actionJob?.cancel()
        }
        reduce(
            current.copy(
                validationMessage = entry.validationMessage,
                activePresentationId = null,
                pendingButton = if (current.pendingButton?.presentationId != null) null else current.pendingButton,
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
        if (closed) return
        closed = true
        loadJob?.cancel()
        actionJob?.cancel()
        countdownJob?.cancel()
        val current = currentScreen()
        if (current?.pendingButton != null) {
            _state.value = DynamicState.Success(current.copy(pendingButton = null))
        }
        scope.cancel()
    }
}
