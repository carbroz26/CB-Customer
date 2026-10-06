package com.carbroz.cbpartner.domain.model.dynamic

sealed interface DynamicElement {
    val id: String
    val semanticRole: String?
    val actions: Map<String, DynamicAction>
}
