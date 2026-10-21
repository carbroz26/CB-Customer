package com.carbroz.cbpartner.feature.dynamic

import com.carbroz.cbpartner.feature.dynamic.action.DismissActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionExecutor
import com.carbroz.cbpartner.feature.dynamic.action.DynamicActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.ExternalUriActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.NavigateActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.PresentActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.RequestActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.StateActionHandler
import com.carbroz.cbpartner.feature.dynamic.action.SequenceActionHandler
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicFormValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidator
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicValidatorRegistry
import com.carbroz.cbpartner.feature.dynamic.usecase.LoadDynamicScreenUseCase
import org.koin.dsl.bind
import org.koin.dsl.module

val dynamicModule = module {
    factory { LoadDynamicScreenUseCase(get()) }
    factory { DynamicValidatorRegistry(getAll<DynamicValidator>()) }
    factory { DynamicFormValidator(get()) }
    factory { NavigateActionHandler() } bind DynamicActionHandler::class
    factory { RequestActionHandler(get()) } bind DynamicActionHandler::class
    factory { ExternalUriActionHandler() } bind DynamicActionHandler::class
    factory { DismissActionHandler() } bind DynamicActionHandler::class
    factory { PresentActionHandler() } bind DynamicActionHandler::class
    factory { StateActionHandler() } bind DynamicActionHandler::class
    factory { SequenceActionHandler() } bind DynamicActionHandler::class
    factory { DynamicActionExecutor(getAll<DynamicActionHandler>()) }
}
