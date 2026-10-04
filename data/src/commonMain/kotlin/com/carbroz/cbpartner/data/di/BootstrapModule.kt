package com.carbroz.cbpartner.data.di

import com.carbroz.cbpartner.data.bootstrap.BootstrapRepositoryImpl
import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.bootstrap.remote.BootstrapRemoteDataSource
import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import com.carbroz.cbpartner.domain.repository.BootstrapRepository
import org.koin.dsl.module

val bootstrapModule = module {
    single { BootstrapRemoteDataSource(get()) }
    single { BootstrapMapper() }
    single<BootstrapRepository> { BootstrapRepositoryImpl(get(), get(), get()) }
    single<ApplicationBootstrap> {
        object : ApplicationBootstrap {
            override suspend fun invoke() = get<BootstrapRepository>().fetch()
        }
    }
}
