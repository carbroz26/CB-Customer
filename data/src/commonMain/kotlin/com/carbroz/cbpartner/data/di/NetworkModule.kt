package com.carbroz.cbpartner.data.di

import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * Data-layer infrastructure definitions.
 *
 * The application composition root owns assembly of this module; the data layer
 * only owns the definitions required to construct its infrastructure.
 */
val networkModule = module {
    single { HttpClient() }
}
