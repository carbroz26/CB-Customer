package com.carbroz.cbpartner.feature.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.presentation.DynamicPresentationHost
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.renderer.DynamicRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.store.DynamicIntent
import com.carbroz.cbpartner.feature.dynamic.store.DynamicState
import com.carbroz.cbpartner.feature.dynamic.store.DynamicStore
import com.carbroz.cbpartner.navigation.DynamicNavigationEntry

@Composable
fun DynamicContainer(
    entry: DynamicNavigationEntry,
    runtimeState: DynamicRuntimeState,
    contextValues: Map<String, DynamicValue>,
    trustedAssetOrigin: String? = null,
    listCatalog: DynamicListCatalog? = null,
    storeFactory: () -> DynamicStore,
    registry: DynamicRegistry,
    fallbackDialogs: Map<String, DialogPresentation> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    // The entry owns an active dialog and retry origin across configuration changes.
    val store = retain(entry) { storeFactory() }
    val flowLogger = remember(entry) { Logger.withTag("CBPartnerFlow") }
    val state by store.state.collectAsState()
    val runtimeValues by runtimeState.values.collectAsState()

    LaunchedEffect(store) {
        if (store.state.value is DynamicState.Initial) store.accept(DynamicIntent.Load)
    }

    RetainedEffect(store) {
        onRetire { store.close() }
    }

    fun dispatchAction(
        action: DynamicAction,
        presentationId: String? = null,
        buttonId: String? = null,
        presentationGeneration: Long? = null,
    ) {
        val safeType = when (action.type.trim().lowercase()) {
            "request", "navigate", "present", "dismiss", "external_uri", "state", "sequence" ->
                action.type.trim().lowercase()
            else -> "other"
        }
        runCatching {
            flowLogger.d {
                "event=action.dispatched sessionId=" +
                    safeDiagnosticId(entry.diagnosticSessionId) +
                    " type=" + safeType
            }
        }
        store.accept(DynamicIntent.Action(action, presentationId, buttonId, presentationGeneration))
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            DynamicState.Initial, DynamicState.Loading -> CircularProgressIndicator()
            is DynamicState.Success -> {
                val response = current.screen.response
                if (response != null) {
                    DynamicRenderer(
                        registry = registry,
                        trustedAssetOrigin = trustedAssetOrigin,
                        listCatalog = listCatalog,
                        onListItemSelected = { sectionId, itemKey ->
                            store.accept(DynamicIntent.ListItemSelected(
                                entry.entryId, null, null, sectionId, itemKey,
                            ))
                        },
                        bindings = current.screen.bindings,
                        contextValues = contextValues,
                        stateValues = runtimeValues + current.screen.transientState,
                        validationErrors = current.screen.validationErrors,
                        focusedBinding = current.screen.focusedBinding,
                        pendingButtonId = current.screen.pendingButton?.takeIf {
                            it.presentationId == null
                        }?.id,
                        onAction = { action -> dispatchAction(action) },
                        onButtonAction = { action, buttonId ->
                            dispatchAction(action, buttonId = buttonId)
                        },
                        onValueChange = { key, value ->
                            store.accept(DynamicIntent.ValueChanged(key, value))
                        },
                        onFocusChange = { key, focused ->
                            store.accept(DynamicIntent.FocusChanged(key, focused))
                        },
                    ).Render(response, entry.destination) {
                        runCatching {
                            flowLogger.i {
                                "event=render.committed sessionId=" +
                                    safeDiagnosticId(entry.diagnosticSessionId) +
                                    " parentActionId=" + safeDiagnosticId(entry.parentActionId) +
                                    " outcome=success"
                            }
                        }
                    }

                    val presentation = current.screen.activePresentationId?.let { presentationId ->
                        response.data?.presentations?.firstOrNull { it.id == presentationId }
                    }
                    if (presentation != null) {
                        DynamicPresentationHost(
                            presentation = presentation,
                            registry = registry,
                            trustedAssetOrigin = trustedAssetOrigin,
                            listCatalog = listCatalog,
                            onListItemSelected = { sectionId, itemKey ->
                                store.accept(DynamicIntent.ListItemSelected(
                                    entry.entryId,
                                    presentation.id,
                                    current.screen.presentationGeneration,
                                    sectionId,
                                    itemKey,
                                ))
                            },
                            bindings = current.screen.bindings + current.screen.presentationBindings,
                            contextValues = contextValues,
                            stateValues = runtimeValues + current.screen.transientState,
                            validationErrors = current.screen.presentationValidationErrors,
                            focusedBinding = current.screen.presentationFocusedBinding,
                            pendingButtonId = current.screen.pendingButton?.takeIf {
                                it.presentationId == presentation.id &&
                                    it.presentationGeneration == current.screen.presentationGeneration
                            }?.id,
                            onAction = { action ->
                                dispatchAction(
                                    action,
                                    presentation.id,
                                    presentationGeneration = current.screen.presentationGeneration,
                                )
                            },
                            onButtonAction = { action, buttonId ->
                                dispatchAction(
                                    action,
                                    presentation.id,
                                    buttonId,
                                    current.screen.presentationGeneration,
                                )
                            },
                            onValueChange = { key, value ->
                                store.accept(
                                    DynamicIntent.PresentationValueChanged(
                                        presentationId = presentation.id,
                                        key = key,
                                        value = value,
                                        presentationGeneration = current.screen.presentationGeneration,
                                    ),
                                )
                            },
                            onFocusChange = { key, focused ->
                                store.accept(
                                    DynamicIntent.PresentationFocusChanged(
                                        presentationId = presentation.id,
                                        key = key,
                                        focused = focused,
                                        presentationGeneration = current.screen.presentationGeneration,
                                    ),
                                )
                            },
                            onDismiss = {
                                store.accept(
                                    DynamicIntent.DismissPresentation(
                                        presentation.id,
                                        current.screen.presentationGeneration,
                                    ),
                                )
                            },
                        )
                    }
                }

                current.screen.validationMessage?.let { message ->
                    Surface(modifier = Modifier.align(Alignment.BottomCenter)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(message, modifier = Modifier.weight(1f))
                            TextButton(onClick = { store.accept(DynamicIntent.ClearMessage) }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }
            is DynamicState.Failure -> Text(current.message)
        }
        // This additional presentation uses the same renderer as screen-defined
        // Country Picker, without moving screen-owned actions into an app singleton.
        val activeScreen = when (val current = state) {
            is DynamicState.Success -> current.screen
            is DynamicState.Failure -> current.screen
            else -> null
        }
        val failure = activeScreen?.dialogFailure
        val key = when (failure?.category) {
            AppFailure.Category.CONNECTION -> "connection"
            AppFailure.Category.SERVER -> "server_error"
            AppFailure.Category.SECURE_CONNECTION -> "secure_connection"
            AppFailure.Category.UNKNOWN -> "server_error"
            else -> null
        }
        // When the screen itself failed to load, Cancel/Close would strand the user.
        // Reuse the same safe Retry-only JSON composition as blocked Bootstrap.
        val safeFallbackKey = if (state is DynamicState.Failure &&
            key in setOf("server_error", "secure_connection")
        ) key + "_startup" else key
        val shown = activeScreen?.activeResponseDialog ?: failure?.presentation
            ?: safeFallbackKey?.let(fallbackDialogs::get)
        if (activeScreen != null && shown != null) {
            val generation = activeScreen.dialogGeneration
            val dialog = if (state is DynamicState.Failure) shown.copy(
                dismissOnBack = false,
                dismissOnOutsideClick = false,
            ) else shown
            DynamicPresentationHost(
                presentation = dialog,
                registry = registry,
                trustedAssetOrigin = trustedAssetOrigin,
                listCatalog = listCatalog,
                bindings = activeScreen.bindings + dialog.initialBindings,
                contextValues = contextValues,
                stateValues = runtimeValues + activeScreen.transientState,
                validationErrors = emptyMap(),
                focusedBinding = null,
                onAction = { action ->
                    if (action.type.equals("dismiss", ignoreCase = true)) {
                        val intent = (action.payload["dialogIntent"] as? DynamicValue.StringValue)?.value
                        store.accept(
                            if (intent == "retry" && failure != null) DynamicIntent.RetryResponseDialog(generation)
                            else DynamicIntent.DismissResponseDialog(generation),
                        )
                    } else {
                        dispatchAction(action)
                    }
                },
                onValueChange = { _, _ -> },
                onFocusChange = { _, _ -> },
                onDismiss = { store.accept(DynamicIntent.DismissResponseDialog(generation)) },
            )
        }
    }
}
