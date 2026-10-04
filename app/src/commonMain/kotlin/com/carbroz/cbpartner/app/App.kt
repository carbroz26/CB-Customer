package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.data.di.bootstrapModule
import com.carbroz.cbpartner.data.di.dynamicDataModule
import com.carbroz.cbpartner.data.di.networkModule
import com.carbroz.cbpartner.feature.dynamic.dynamicModule
import com.carbroz.cbpartner.feature.splash.splashModule
import org.koin.core.context.startKoin

private val koin by lazy {
    startKoin {
        modules(
            networkModule,
            bootstrapModule,
            dynamicDataModule,
            dynamicModule,
            splashModule,
        )
    }.koin
}

@Composable
fun App() {
    ApplicationContent(koin)
}
