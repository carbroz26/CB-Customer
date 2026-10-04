package com.carbroz.cbpartner.feature.dynamic

import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionHandler
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.ExecuteDynamicActionUseCase
import com.carbroz.cbpartner.feature.dynamic.domain.usecase.LoadDynamicScreenUseCase
import org.koin.dsl.module

val dynamicModule = module {
    factory { DynamicActionHandler() }
    factory { LoadDynamicScreenUseCase(get()) }
    factory { ExecuteDynamicActionUseCase(get(), get()) }
}
