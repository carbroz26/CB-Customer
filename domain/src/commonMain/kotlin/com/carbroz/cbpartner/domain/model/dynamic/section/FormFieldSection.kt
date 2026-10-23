package com.carbroz.cbpartner.domain.model.dynamic.section

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicFocusPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation

/** The whole input control surface; its author owns all dimensions and colors. */
data class FormFieldControlSurface(
    val height: Double,
    val fillMaxWidth: Boolean,
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
    val shape: DynamicShape? = null,
) {
    init {
        require(height.isFinite() && height > 0.0) {
            "Form field surface height must be positive and finite"
        }
    }
}

/**
 * One reusable label + one bound input (direct or composite) + conditional error message.
 * Validation is calculated by DynamicFormValidator, never by this Section.
 */
data class FormFieldSection(
    val id: String,
    val label: TextElement,
    val controlInput: InputElement? = null,
    val controlGroup: DynamicGroup? = null,
    val inputBinding: String,
    val controlSurface: FormFieldControlSurface,
    val horizontalAlignment: DynamicArrangement,
    val fillMaxWidth: Boolean,
    val padding: DynamicPadding? = null,
    val focusPresentation: DynamicFocusPresentation? = null,
    val validationPresentation: DynamicValidationPresentation? = null,
) : DynamicSection {
    init {
        require(id.isNotBlank()) { "Form field ID cannot be blank" }
        require((controlInput == null) != (controlGroup == null)) {
            "Form field requires exactly one direct input or a composite control Group"
        }
        require(inputBinding.isNotBlank()) { "Form field input binding cannot be blank" }
        require(focusPresentation == null || focusPresentation.binding == inputBinding) {
            "Form field focus binding must match input binding"
        }
        require(validationPresentation == null || validationPresentation.binding == inputBinding) {
            "Form field validation binding must match input binding"
        }
        val input = requireNotNull(
            controlInput ?: controlGroup?.elements?.filterIsInstance<InputElement>()?.singleOrNull(),
        ) { "Form field needs exactly one bound input" }
        require(input.binding?.key == inputBinding) { "Form field needs exactly one matching bound input" }
        require(input.validation == null || validationPresentation != null) {
            "Validated FormFieldSection requires inline validation presentation"
        }
    }

    /** Existing Validator and Button-ID traversals remain aware of direct children. */
    override val elements: List<DynamicElement>
        get() = listOfNotNull(label, controlInput)

    override val groups: List<DynamicGroup>
        get() = listOfNotNull(controlGroup)
}
