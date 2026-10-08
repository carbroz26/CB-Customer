package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicInputPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding

data class InputElement(
    val id: String,
    val semanticRole: String? = null,
    val placeholder: String? = null,
    val keyboardType: String = "text",
    val maxLength: Int? = null,
    val weight: Double? = null,
    val width: Double? = null,
    val maxWidth: Double? = null,
    val fillMaxWidth: Boolean = false,
    val padding: DynamicPadding? = null,
    val validation: DynamicValidation? = null,
    val binding: DynamicBinding? = null,
    val presentation: DynamicInputPresentation? = null,
    val leadingContent: List<ElementContent> = emptyList(),
    val trailingContent: List<ElementContent> = emptyList(),
    val actions: Map<String, DynamicAction> = emptyMap(),
)