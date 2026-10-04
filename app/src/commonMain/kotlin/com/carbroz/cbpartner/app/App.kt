package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import org.koin.core.context.startKoin

private val koin by lazy {
    startKoin {
        modules(
            com.carbroz.cbpartner.data.di.networkModule,
            com.carbroz.cbpartner.data.di.bootstrapModule,
            com.carbroz.cbpartner.data.di.dynamicDataModule,
            com.carbroz.cbpartner.feature.dynamic.dynamicModule,
            com.carbroz.cbpartner.feature.splash.splashModule,
        )
    }.koin
}

@Composable
fun App() {
    ApplicationContent(koin)
}
