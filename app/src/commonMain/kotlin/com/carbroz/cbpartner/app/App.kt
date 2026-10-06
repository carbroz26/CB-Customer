package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.core.runtime.RuntimeMetadataProvider
import com.carbroz.cbpartner.data.di.bootstrapModule
import com.carbroz.cbpartner.data.di.dynamicDataModule
import com.carbroz.cbpartner.data.di.serializationModule
import com.carbroz.cbpartner.feature.dynamic.dynamicModule
import com.carbroz.cbpartner.feature.splash.splashModule
import com.carbroz.cbpartner.network.networkModule
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Composable
fun App(runtimeMetadataProvider: RuntimeMetadataProvider) {
    val koin = startKoin {
        modules(
            module { single<RuntimeMetadataProvider> { runtimeMetadataProvider } },
            networkModule,
            serializationModule,
            bootstrapModule,
            dynamicDataModule,
            dynamicModule,
            splashModule,
        )
    }.koin

    ApplicationContent(koin, runtimeMetadataProvider)
}
