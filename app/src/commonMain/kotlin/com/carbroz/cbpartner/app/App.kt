package com.carbroz.cbpartner.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.carbroz.cbpartner.data.di.bootstrapModule
import com.carbroz.cbpartner.data.di.dynamicDataModule
import com.carbroz.cbpartner.data.di.networkModule
import com.carbroz.cbpartner.feature.dynamic.DynamicContainer
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.store.DynamicEffect
import com.carbroz.cbpartner.feature.dynamic.store.DynamicStore
import com.carbroz.cbpartner.feature.splash.SplashScreen
import com.carbroz.cbpartner.feature.splash.SplashState
import com.carbroz.cbpartner.feature.splash.SplashStore
import com.carbroz.cbpartner.feature.splash.splashModule
import com.carbroz.cbpartner.navigation.DynamicNavigationHost
import com.carbroz.cbpartner.navigation.DynamicNavigationState
import org.koin.core.context.startKoin

private val koin by lazy {
    startKoin {
        modules(
            networkModule,
            bootstrapModule,
            dynamicDataModule,
            splashModule,
        )
    }.koin
}

@Composable
fun App() {
    val splashStore = remember { koin.get<SplashStore>() }
    val splashState by splashStore.state.collectAsState()

    MaterialTheme {
        when (val state = splashState) {
            is SplashState.Success -> {
                val initialDestination = state.output.startup.nextScreen
                val navigationState = remember(initialDestination) {
                    DynamicNavigationState(initialDestination)
                }
                val registry = remember { DynamicRegistry().registerDefaults() }
                val dynamicStore = remember(initialDestination) {
                    DynamicStore(
                        loadDynamicScreen = koin.get(),
                        executeDynamicAction = koin.get(),
                        destination = initialDestination,
                    )
                }

                LaunchedEffect(dynamicStore) {
                    dynamicStore.effects.collect { effect ->
                        when (effect) {
                            is DynamicEffect.Navigate -> navigationState.push(effect.destination)
                            else -> Unit
                        }
                        dynamicStore.clearEffect()
                    }
                }

                DisposableEffect(dynamicStore) {
                    onDispose { dynamicStore.close() }
                }

                DynamicNavigationHost(state = navigationState) { destination ->
                    DynamicContainer(
                        destination = destination,
                        store = dynamicStore,
                        registry = registry,
                    )
                }
            }

            else -> SplashScreen(store = splashStore)
        }
    }
}
