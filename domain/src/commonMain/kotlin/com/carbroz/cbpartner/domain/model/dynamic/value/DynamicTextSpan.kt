package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicTextSpan(
    val text: String,
    val color: String? = null,
    val fontWeight: Int? = null,
    val underline: Boolean = false,
    val action: DynamicAction? = null,
)
