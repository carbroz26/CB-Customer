package com.carbroz.cbpartner.domain.model.dynamic.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

sealed interface ElementContent {
    val id: String?
    val semanticRole: String?
    val actions: Map<String, DynamicAction>
}
