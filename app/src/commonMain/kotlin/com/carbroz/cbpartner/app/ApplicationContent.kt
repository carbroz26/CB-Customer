package com.carbroz.cbpartner.app

import co.touchlab.kermit.Logger
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.carbroz.cbpartner.core.diagnostics.TraceContext
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.DynamicContainer
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionEffect
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionResult
import com.carbroz.cbpartner.feature.dynamic.registry.dynamicRendererRegistry
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRuntimeState
import com.carbroz.cbpartner.feature.dynamic.store.DynamicStore
import com.carbroz.cbpartner.feature.splash.SplashScreen
import com.carbroz.cbpartner.feature.splash.SplashState
import com.carbroz.cbpartner.feature.splash.SplashStore
import com.carbroz.cbpartner.navigation.DynamicNavigationHost
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import org.koin.core.Koin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel

@Composable
fun ApplicationContent(
    koin: Koin,
    runtimeMetadataProvider: RuntimeMetadataProvider,
) {
    val splashStore = remember { koin.get<SplashStore>() }
    val splashState by splashStore.state.collectAsState()

    DisposableEffect(splashStore) {
        onDispose { splashStore.close() }
    }

    // Bootstrap has completed; its startup scope must not outlive the handoff.
    LaunchedEffect(splashStore, splashState) {
        if (splashState is SplashState.Success) splashStore.close()
    }

    MaterialTheme {
        when (val state = splashState) {
            is SplashState.Success -> DynamicContent(
                koin = koin,
                initialDestination = state.output.destination,
                diagnosticSessionId = splashStore.diagnosticSessionId,
                runtimeMetadataProvider = runtimeMetadataProvider,
            )
            else -> SplashScreen(store = splashStore)
        }
    }
}

@Composable
private fun DynamicContent(
    koin: Koin,
    initialDestination: DynamicDestination,
    diagnosticSessionId: String,
    runtimeMetadataProvider: RuntimeMetadataProvider,
) {
    val navigationState = remember(initialDestination, diagnosticSessionId) {
        DynamicNavigationState(initialDestination, diagnosticSessionId)
    }
    val effectBatches = remember(navigationState) {
        Channel<Pair<TraceContext?, List<DynamicActionEffect>>>(Channel.UNLIMITED)
    }
    val uriHandler = LocalUriHandler.current
    val flowLogger = remember(navigationState) { Logger.withTag("CBPartnerFlow") }
    var effectError by remember(navigationState) { mutableStateOf<String?>(null) }

    DisposableEffect(effectBatches) {
        onDispose { effectBatches.close() }
    }

    LaunchedEffect(effectBatches, navigationState, uriHandler) {
        for ((traceContext, batch) in effectBatches) {
            var failed = false
            for (effect in batch) {
                val operation = when (effect) {
                    is DynamicActionEffect.Navigate -> effect.operation.name.lowercase()
                    is DynamicActionEffect.Present -> "present"
                    DynamicActionEffect.Dismiss -> "dismiss"
                    is DynamicActionEffect.ExternalUri -> "external_uri"
                }
                try {
                    when (effect) {
                        is DynamicActionEffect.ExternalUri -> uriHandler.openUri(effect.uri)
                        is DynamicActionEffect.Navigate -> when (effect.operation) {
                            DynamicActionResult.NavigationOperation.Push -> navigationState.push(requireNotNull(effect.destination), traceContext?.actionId)
                            DynamicActionResult.NavigationOperation.Replace -> navigationState.replace(requireNotNull(effect.destination), traceContext?.actionId)
                            DynamicActionResult.NavigationOperation.Pop -> check(navigationState.pop()) { "Navigation unavailable" }
                        }
                        is DynamicActionEffect.Present -> check(navigationState.present(effect.destination, traceContext?.actionId)) { "Presentation unavailable" }
                        DynamicActionEffect.Dismiss -> check(navigationState.dismissPresentation()) { "Dismiss unavailable" }
                    }
                    runCatching {
                        flowLogger.i {
                            "event=navigation.applied sessionId=${traceContext?.sessionId ?: "-"} " +
                                "actionId=${traceContext?.actionId ?: "-"} operation=$operation"
                        }
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    // Keep the message UI-safe; never render exception text or an external URI.
                    failed = true
                    runCatching {
                        flowLogger.w {
                            "event=navigation.failed sessionId=${traceContext?.sessionId ?: "-"} " +
                                "actionId=${traceContext?.actionId ?: "-"} operation=$operation"
                        }
                    }
                }
            }
            effectError = if (failed) "Unable to complete this action" else null
        }
    }
    val runtimeState = remember { DynamicRuntimeState() }
    val registry = remember { dynamicRendererRegistry() }
    val metadata = runtimeMetadataProvider.current()
    val contextValues = remember(metadata) {
        mapOf(
            "platform" to DynamicValue.StringValue(metadata.platform),
            "appVersion" to DynamicValue.StringValue(metadata.appVersion),
            "buildNumber" to DynamicValue.StringValue(metadata.buildNumber),
            "deviceId" to DynamicValue.StringValue(metadata.deviceId),
        )
    }

    Box {
        DynamicNavigationHost(state = navigationState) { entry ->
            DynamicContainer(
            entry = entry,
            runtimeState = runtimeState,
            contextValues = contextValues,
            storeFactory = {
                DynamicStore(
                    loadDynamicScreen = koin.get(),
                    actionExecutor = koin.get<DynamicActionExecutor>(),
                    validator = koin.get<DynamicFormValidator>(),
                    entry = entry,
                    runtimeState = runtimeState,
                    contextProvider = { contextValues },
                    deliverEffects = { effects -> effectBatches.send(null to effects) },
                    deliverCorrelatedEffects = { trace, effects -> effectBatches.send(trace to effects) },
                )
            },
            registry = registry,
            )
        }
        effectError?.let { message ->
            Column(modifier = androidx.compose.ui.Modifier.align(Alignment.TopCenter)) {
                Text(message)
                TextButton(onClick = { effectError = null }) {
                    Text("Dismiss")
                }
            }
        }
    }
}