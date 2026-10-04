package com.carbroz.cbpartner.app

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.data.di.bootstrapModule
import com.carbroz.cbpartner.data.di.dynamicDataModule
import com.carbroz.cbpartner.data.di.serializationModule
import com.carbroz.cbpartner.feature.dynamic.dynamicModule
import com.carbroz.cbpartner.feature.splash.splashModule
import com.carbroz.cbpartner.network.networkModule
import org.koin.core.context.startKoin

private val koin by lazy {
    startKoin {
        modules(
            networkModule,
            serializationModule,
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
