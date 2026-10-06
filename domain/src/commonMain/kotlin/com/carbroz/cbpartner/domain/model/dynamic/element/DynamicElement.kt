package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

sealed interface DynamicElement {
    val id: String
    val semanticRole: String?
    val actions: Map<String, DynamicAction>
}