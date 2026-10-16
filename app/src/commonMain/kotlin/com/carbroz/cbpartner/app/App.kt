package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.retain.RetainedEffect
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.data.di.bootstrapModule
import com.carbroz.cbpartner.data.di.dynamicDataModule
import com.carbroz.cbpartner.data.di.serializationModule
import com.carbroz.cbpartner.feature.dynamic.dynamicModule
import com.carbroz.cbpartner.feature.splash.splashModule
import com.carbroz.cbpartner.network.ApiBaseUrl
import com.carbroz.cbpartner.network.networkModule
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@Composable
fun App(
    runtimeMetadataProvider: RuntimeMetadataProvider,
    apiBaseUrl: ApiBaseUrl,
) {
    val retainedSession = retain { mutableStateOf<DynamicSession?>(null) }
    RetainedEffect(retainedSession) {
        onRetire { retainedSession.value?.close() }
    }
    val koin = remember(runtimeMetadataProvider, apiBaseUrl) {
        koinApplication {
            modules(
                module { single<RuntimeMetadataProvider> { runtimeMetadataProvider } },
                networkModule(apiBaseUrl),
                serializationModule,
                bootstrapModule,
                dynamicDataModule,
                dynamicModule,
                splashModule,
            )
        }
    }

    DisposableEffect(koin) {
        onDispose { koin.close() }
    }

    ApplicationContent(koin.koin, runtimeMetadataProvider, retainedSession)
}
