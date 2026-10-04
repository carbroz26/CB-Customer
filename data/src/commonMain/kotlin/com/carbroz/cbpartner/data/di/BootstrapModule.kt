package com.carbroz.cbpartner.data.di

import com.carbroz.cbpartner.data.bootstrap.DefaultApplicationBootstrap
import com.carbroz.cbpartner.domain.bootstrap.ApplicationBootstrap
import org.koin.dsl.module

val bootstrapModule = module {
    single<ApplicationBootstrap> { DefaultApplicationBootstrap(get()) }
}
