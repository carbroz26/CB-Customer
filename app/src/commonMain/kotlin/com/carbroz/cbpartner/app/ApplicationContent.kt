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
import com.carbroz.cbpartner.core.diagnostics.safeDiagnosticId
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.error.AppFailure
import com.carbroz.cbpartner.feature.dynamic.presentation.DynamicPresentationHost
import com.carbroz.cbpartner.feature.splash.SplashIntent
import com.carbroz.cbpartner.feature.splash.isTrustedUpdateUrl
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
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
    trustedAssetOrigin: String,
) {
    var dialogTemplates by remember { mutableStateOf<Map<String, DialogPresentation>>(emptyMap()) }
    LaunchedEffect(Unit) { dialogTemplates = loadPackagedDialogTemplates() }
    MaterialTheme {
        val session = retainedSession.value
        if (session != null) {
            DynamicContent(koin, session, runtimeMetadataProvider, trustedAssetOrigin, dialogTemplates)
        } else {
            BootstrapContent(koin, runtimeMetadataProvider, trustedAssetOrigin, dialogTemplates) { destination, diagnosticId ->
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
    runtimeMetadataProvider: RuntimeMetadataProvider,
    trustedAssetOrigin: String,
    dialogTemplates: Map<String, DialogPresentation>,
    onHandoff: (DynamicDestination, String) -> Unit,
) {
    val splashStore = remember(koin) { koin.get<SplashStore>() }
    val splashState by splashStore.state.collectAsState()
    val uriHandler = LocalUriHandler.current
    val registry = remember { dynamicRendererRegistry() }
    DisposableEffect(splashStore) { onDispose { splashStore.close() } }
    LaunchedEffect(splashStore, splashState) {
        (splashState as? SplashState.Success)?.let { success ->
            onHandoff(success.output.destination, splashStore.diagnosticSessionId)
            splashStore.close()
        }
    }
    SplashScreen(splashStore)
    val kind = when (val state = splashState) {
        is SplashState.Maintenance -> "maintenance"
        is SplashState.RequiredUpdate -> "required_update"
        is SplashState.OptionalUpdate -> "optional_update"
        is SplashState.Failure -> when (state.failure?.category) {
            AppFailure.Category.CONNECTION -> "connection"
            AppFailure.Category.SECURE_CONNECTION -> "secure_connection"
            else -> "server_error"
        }
        else -> null
    }
    val template = kind?.let(dialogTemplates::get)
    if (template != null) {
        val state = splashState
        val appVersion = runtimeMetadataProvider.current().appVersion
        val trustedUrl = when (state) {
            is SplashState.RequiredUpdate -> state.storeUrl
            is SplashState.OptionalUpdate -> state.output.update.storeUrl
            else -> null
        }?.takeIf(::isTrustedUpdateUrl)
        val values: Map<String, DynamicValue> = when (state) {
            is SplashState.Maintenance -> mapOf(
                "title" to DynamicValue.StringValue(state.title?.takeIf { it.isNotBlank() && it.length <= 120 } ?: "App Under Maintenance"),
                "message" to DynamicValue.StringValue(state.message?.takeIf { it.isNotBlank() && it.length <= 240 } ?: "Please try again in a few minutes."),
            )
            is SplashState.RequiredUpdate -> mapOf(
                "currentVersion" to DynamicValue.StringValue("Current version $appVersion"),
                "targetVersion" to DynamicValue.StringValue("Required version ${state.minimumVersion}"),
            )
            is SplashState.OptionalUpdate -> mapOf(
                "currentVersion" to DynamicValue.StringValue("Current version $appVersion"),
                "targetVersion" to DynamicValue.StringValue("Latest version ${state.output.update.latestVersion}"),
            )
            else -> emptyMap()
        }
        val presentation = template.copy(
            initialBindings = template.initialBindings + values +
                (trustedUrl?.let { mapOf("storeUrl" to DynamicValue.StringValue(it)) } ?: emptyMap()),
            dismissOnBack = state is SplashState.OptionalUpdate,
            dismissOnOutsideClick = state is SplashState.OptionalUpdate,
        )
        val entry = AppDialogEntry(
            presentation = presentation,
            onAction = { action ->
                when (action.type.lowercase()) {
                    "dismiss" -> when (state) {
                        is SplashState.OptionalUpdate -> splashStore.accept(SplashIntent.SkipOptionalUpdate)
                        is SplashState.RequiredUpdate -> Unit
                        else -> splashStore.accept(SplashIntent.RetryBootstrap)
                    }
                    "external_uri" -> if (trustedUrl != null) {
                        try { uriHandler.openUri(trustedUrl) } catch (_: Exception) { /* Stay on dialog. */ }
                    }
                    else -> Unit
                }
            },
            onDismiss = {
                if (state is SplashState.OptionalUpdate) {
                    splashStore.accept(SplashIntent.SkipOptionalUpdate)
                }
            },
        )
        DynamicPresentationHost(
            presentation = entry.presentation,
            registry = registry,
            trustedAssetOrigin = trustedAssetOrigin,
            bindings = entry.presentation.initialBindings,
            contextValues = emptyMap(),
            stateValues = emptyMap(),
            validationErrors = emptyMap(),
            focusedBinding = null,
            onAction = entry.onAction,
            onValueChange = { _, _ -> },
            onFocusChange = { _, _ -> },
            onDismiss = entry.onDismiss,
        )
    }
}

@Composable
private fun DynamicContent(
    koin: Koin,
    session: DynamicSession,
    runtimeMetadataProvider: RuntimeMetadataProvider,
    trustedAssetOrigin: String,
    dialogTemplates: Map<String, DialogPresentation>,
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
                                "sessionId=${safeDiagnosticId(traceContext?.sessionId)} " +
                                "actionId=${safeDiagnosticId(traceContext?.actionId)} operation=$operation"
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
            listCatalog = koin.get<DynamicListCatalog>(),
            trustedAssetOrigin = trustedAssetOrigin,
            storeFactory = {
                DynamicStore(
                    loadDynamicScreen = koin.get(),
                    actionExecutor = koin.get<DynamicActionExecutor>(),
                    validator = koin.get<DynamicFormValidator>(),
                    listCatalog = koin.get<DynamicListCatalog>(),
                    entry = entry,
                    runtimeState = session.runtimeState,
                    contextProvider = { contextValues },
                    commitActionResult = { changes, effects, trace ->
                        session.commit(changes, effects, trace)
                    },
                )
            },
            registry = registry,
            fallbackDialogs = dialogTemplates,
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