package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent

sealed interface DynamicElement {
    val id: String
    val semanticRole: String?
    val leadingContent: List<ElementContent>
    val trailingContent: List<ElementContent>
    val actions: Map<String, DynamicAction>
}
