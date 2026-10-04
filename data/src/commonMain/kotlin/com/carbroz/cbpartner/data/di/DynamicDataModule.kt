package com.carbroz.cbpartner.data.di

import com.carbroz.cbpartner.data.dynamic.DynamicRepositoryImpl
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.remote.DynamicRemoteDataSource
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dynamicDataModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single { DynamicResponseMapper() }
    single { DynamicRemoteDataSource(get()) }
    single<DynamicRepository> {
        DynamicRepositoryImpl(
            remoteDataSource = get(),
            json = get(),
            mapper = get(),
        )
    }
}
