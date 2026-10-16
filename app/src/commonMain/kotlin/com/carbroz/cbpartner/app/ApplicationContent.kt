package com.carbroz.cbpartner.app

import co.touchlab.kermit.Logger
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
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
import kotlinx.coroutines.flow.collect

@Composable
internal fun ApplicationContent(
    koin: Koin,
    runtimeMetadataProvider: RuntimeMetadataProvider,
    retainedSession: MutableState<DynamicSession?>,
) {
    MaterialTheme {
        val session = retainedSession.value
        if (session != null) {
            DynamicContent(koin, session, runtimeMetadataProvider)
        } else {
            BootstrapContent(koin) { destination, diagnosticId ->
                if (retainedSession.value == null) {
                    retainedSession.value = DynamicSession(diagnosticId, destination)
                }
            }
        }
    }
}

@Composable
private fun BootstrapContent(
    koin: Koin,
    onHandoff: (DynamicDestination, String) -> Unit,
) {
    val splashStore = remember(koin) { koin.get<SplashStore>() }
    val splashState by splashStore.state.collectAsState()
    DisposableEffect(splashStore) {
        onDispose { splashStore.close() }
    }
    LaunchedEffect(splashStore, splashState) {
        val success = splashState as? SplashState.Success
        if (success != null) {
            onHandoff(success.output.destination, splashStore.diagnosticSessionId)
            splashStore.close()
        }
    }
    SplashScreen(splashStore)
}

@Composable
private fun DynamicContent(
    koin: Koin,
    session: DynamicSession,
    runtimeMetadataProvider: RuntimeMetadataProvider,
) {
    val navigationState = session.navigationState
    val uriHandler = LocalUriHandler.current
    val flowLogger = remember(session) { Logger.withTag("CBPartnerFlow") }
    var effectError by remember(session) { mutableStateOf<String?>(null) }

    LaunchedEffect(session, uriHandler) {
        val lease = session.attachConsumer()
        try {
            session.workRevision.collect {
                while (true) {
                    val claim = session.claimNext(lease) ?: break
                    if (!session.markDispatchStarted(claim)) break
                    val effect = claim.effect
                    val traceContext = claim.trace
                    val operation = when (effect) {
                        is DynamicActionEffect.Navigate -> effect.operation.name.lowercase()
                        is DynamicActionEffect.Present -> "present"
                        DynamicActionEffect.Dismiss -> "dismiss"
                        is DynamicActionEffect.ExternalUri -> "external_uri"
                    }
                    var applied = false
                    try {
                        when (effect) {
                            is DynamicActionEffect.ExternalUri -> uriHandler.openUri(effect.uri)
                            is DynamicActionEffect.Navigate -> when (effect.operation) {
                                DynamicActionResult.NavigationOperation.Push ->
                                    navigationState.push(requireNotNull(effect.destination), traceContext?.actionId)
                                DynamicActionResult.NavigationOperation.Replace ->
                                    navigationState.replace(requireNotNull(effect.destination), traceContext?.actionId)
                                DynamicActionResult.NavigationOperation.Pop ->
                                    check(navigationState.pop()) { "Navigation unavailable" }
                            }
                            is DynamicActionEffect.Present ->
                                check(navigationState.present(effect.destination, traceContext?.actionId)) { "Presentation unavailable" }
                            DynamicActionEffect.Dismiss ->
                                check(navigationState.dismissPresentation()) { "Dismiss unavailable" }
                        }
                        applied = true
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        effectError = "Unable to complete this action"
                    }
                    if (!session.acknowledge(
                            claim,
                            if (applied) DynamicSession.EffectOutcome.Applied else DynamicSession.EffectOutcome.Rejected,
                        )) break
                    runCatching {
                        flowLogger.i {
                            "event=navigation.${if (applied) "applied" else "failed"} " +
                                "sessionId=${traceContext?.sessionId ?: "-"} " +
                                "actionId=${traceContext?.actionId ?: "-"} operation=$operation"
                        }
                    }
                }
            }
        } finally {
            session.detachConsumer(lease)
        }
    }
    val registry = remember(session) { dynamicRendererRegistry() }
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
            key(koin, entry) {
            DynamicContainer(
            entry = entry,
            runtimeState = session.runtimeState,
            contextValues = contextValues,
            storeFactory = {
                DynamicStore(
                    loadDynamicScreen = koin.get(),
                    actionExecutor = koin.get<DynamicActionExecutor>(),
                    validator = koin.get<DynamicFormValidator>(),
                    entry = entry,
                    runtimeState = session.runtimeState,
                    contextProvider = { contextValues },
                    commitActionResult = { changes, effects, trace ->
                        session.commit(changes, effects, trace)
                    },
                )
            },
            registry = registry,
            )
            }
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