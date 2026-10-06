package com.carbroz.cbpartner.network

import com.carbroz.cbpartner.network.http.HttpClientBuilder
import com.carbroz.cbpartner.network.http.RequestHandler
import io.ktor.client.HttpClient
import io.ktor.http.URLProtocol
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        HttpClientBuilder()
            .protocol(URLProtocol.HTTP)
            .host("localhost")
            .port(3000)
            .build()
    }
    single { RequestHandler(get(), runtimeMetadataProvider = get()) }
}
