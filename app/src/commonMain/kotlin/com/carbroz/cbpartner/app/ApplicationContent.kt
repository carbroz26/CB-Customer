package com.carbroz.cbpartner.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.DynamicContainer
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
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
    runtimeMetadataProvider: RuntimeMetadataProvider,
) {
    val navigationState = remember(initialDestination) { DynamicNavigationState(initialDestination) }
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

    DynamicNavigationHost(state = navigationState) { entry ->
        DynamicContainer(
            entry = entry,
            navigation = navigationState,
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
                )
            },
            registry = registry,
        )
    }
}