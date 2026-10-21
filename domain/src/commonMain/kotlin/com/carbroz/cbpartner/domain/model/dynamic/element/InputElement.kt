package com.carbroz.cbpartner.domain.model.dynamic.element

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicInputPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationStyle

data class InputElement(
    val id: String,
    val semanticRole: String? = null,
    val placeholder: String? = null,
    val keyboardType: String = "text",
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val textColor: String? = null,
    val textAlign: String? = null,
    val maxLength: Int? = null,
    override val weight: Double? = null,
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
    val height: Double? = null,
    val singleLine: Boolean? = null,
    val inputFilter: String? = null,
    val placeholderColor: String? = null,
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
    val focusBorder: DynamicBorder? = null,
    val shape: DynamicShape? = null,
    val validationStyle: DynamicValidationStyle? = null,
) : DynamicElement
