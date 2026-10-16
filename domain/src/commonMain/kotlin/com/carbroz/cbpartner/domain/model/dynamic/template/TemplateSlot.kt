package com.carbroz.cbpartner.domain.model.dynamic.template

import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding

/** Shared optional header/footer content; never a separate Dynamic category. */
data class TemplateSlot(
    val padding: DynamicPadding? = null,
    val components: List<DynamicComponent> = emptyList(),
)
