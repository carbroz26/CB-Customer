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
import androidx.compose.runtime.DisposableEffect
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
    storeFactory: () -> DynamicStore,
    registry: DynamicRegistry,
    modifier: Modifier = Modifier,
) {
    val store = remember(entry) { storeFactory() }
    val flowLogger = remember(entry) { Logger.withTag("CBPartnerFlow") }
    val state by store.state.collectAsState()
    val runtimeValues by runtimeState.values.collectAsState()

    LaunchedEffect(store) {
        store.accept(DynamicIntent.Load)
    }

    DisposableEffect(store) {
        onDispose { store.close() }
    }

    fun dispatchAction(action: DynamicAction, presentationId: String? = null) {
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
        store.accept(DynamicIntent.Action(action, presentationId))
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
                        bindings = current.screen.bindings,
                        contextValues = contextValues,
                        stateValues = runtimeValues,
                        validationErrors = current.screen.validationErrors,
                        focusedBinding = current.screen.focusedBinding,
                        onAction = { action -> dispatchAction(action) },
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
                            bindings = current.screen.bindings + current.screen.presentationBindings,
                            contextValues = contextValues,
                            stateValues = runtimeValues,
                            validationErrors = current.screen.presentationValidationErrors,
                            focusedBinding = current.screen.presentationFocusedBinding,
                            onAction = { action -> dispatchAction(action, presentation.id) },
                            onValueChange = { key, value ->
                                store.accept(
                                    DynamicIntent.PresentationValueChanged(
                                        presentationId = presentation.id,
                                        key = key,
                                        value = value,
                                    ),
                                )
                            },
                            onFocusChange = { key, focused ->
                                store.accept(
                                    DynamicIntent.PresentationFocusChanged(
                                        presentationId = presentation.id,
                                        key = key,
                                        focused = focused,
                                    ),
                                )
                            },
                            onDismiss = {
                                store.accept(DynamicIntent.DismissPresentation(presentation.id))
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
    }
}
