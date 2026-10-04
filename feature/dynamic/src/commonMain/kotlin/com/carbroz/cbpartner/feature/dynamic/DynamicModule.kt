package com.carbroz.cbpartner.feature.dynamic

import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import org.koin.dsl.module

val dynamicModule = module {
    factory { LoadDynamicScreenUseCase(get()) }
    factory { DynamicActionExecutor(get()) }
}
