package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape

data class ButtonElement(
    override val id: String,
    override val semanticRole: String? = null,
    val text: String = "",
    val height: Double? = null,
    val fillMaxWidth: Boolean = false,
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val textColor: String? = null,
    val background: DynamicBackground? = null,
    val shape: DynamicShape? = null,
    val leading: List<DynamicElement> = emptyList(),
    val trailing: List<DynamicElement> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement