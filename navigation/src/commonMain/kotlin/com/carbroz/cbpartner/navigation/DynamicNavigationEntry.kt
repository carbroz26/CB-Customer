package com.carbroz.cbpartner.navigation

import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

class DynamicNavigationEntry(
    val destination: DynamicDestination,
) {
    var response: DynamicResponse? = null
    var bindings: Map<String, DynamicValue> = emptyMap()
    var validationMessage: String? = null
}
