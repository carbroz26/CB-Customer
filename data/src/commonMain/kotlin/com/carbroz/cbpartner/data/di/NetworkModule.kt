package com.carbroz.cbpartner.data.di

import com.carbroz.cbpartner.data.network.KtorClientBuilder
import com.carbroz.cbpartner.data.network.NetworkConfig
import com.carbroz.cbpartner.data.network.RemoteDataSource
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single {
        NetworkConfig(
            baseUrl = "http://localhost:3000",
        )
    }
    single { KtorClientBuilder(get()).build() }
    single { RemoteDataSource(get()) }
}
