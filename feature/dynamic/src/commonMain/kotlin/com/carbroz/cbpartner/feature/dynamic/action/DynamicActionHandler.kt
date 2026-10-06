package com.carbroz.cbpartner.feature.dynamic.action

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.navigation.DynamicNavigationState

interface DynamicActionHandler {
    val type: String
    suspend fun handle(action: DynamicAction, navigation: DynamicNavigationState, bindings: Map<String,String>, context: Map<String,String>, previousResponse: DynamicValue?): DynamicActionResult
}