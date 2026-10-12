package com.carbroz.cbpartner.network

import com.carbroz.cbpartner.network.http.HttpClientBuilder
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.client.HttpClient
import org.koin.dsl.module

fun networkModule(apiBaseUrl: ApiBaseUrl) = module {
    single<HttpClient> {
        HttpClientBuilder()
            .protocol(apiBaseUrl.url.protocol)
            .host(apiBaseUrl.url.host)
            .port(apiBaseUrl.url.port)
            .build()
    }
    single { RequestHandler(get(), runtimeMetadataProvider = get()) }
}
