package com.carbroz.cbpartner.data.di

import com.carbroz.cbpartner.data.dynamic.catalog.PartnerCountryCatalog
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.data.dynamic.DynamicRepositoryImpl
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.remote.DynamicRemoteDataSource
import com.carbroz.cbpartner.domain.repository.DynamicRepository
import org.koin.dsl.module

val dynamicDataModule = module {
    single<DynamicListCatalog> { PartnerCountryCatalog() }
    single { DynamicResponseMapper(get()) }
    single { DynamicRemoteDataSource(get()) }
    single<DynamicRepository> {
        DynamicRepositoryImpl(
            remoteDataSource = get(),
            json = get(),
            mapper = get(),
        )
    }
}
