package com.carbroz.cbpartner.domain.model.dynamic.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

sealed interface ElementContent {
    val id: String?
    val semanticRole: String?
    val spacing: DynamicSpacing?
    val actions: Map<String, DynamicAction>
}
