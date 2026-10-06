package com.carbroz.cbpartner.domain.model.dynamic.value

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

data class DynamicTextSpan(
    val text: String,
    val color: String? = null,
    val fontWeight: Int? = null,
    val underline: Boolean = false,
    val action: DynamicAction? = null,
)

data class DynamicInputPresentation(
    val type: String,
    val count: Int? = null,
    val spacing: Double? = null,
    val segmentWidth: Double? = null,
    val segmentHeight: Double? = null,
)