package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue

data class DynamicValidationResult(
    val errors: Map<String, String> = emptyMap(),
    val fallbackMessage: String? = null,
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}

class DynamicFormValidator {
    fun validate(
        screen: DynamicScreen,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue> = emptyMap(),
        state: Map<String, DynamicValue> = emptyMap(),
        suppressedBinding: String? = null,
    ): DynamicValidationResult =
        validateComponents(screen.template.components, bindings, context, state, suppressedBinding)

    fun validate(
        presentation: DynamicPresentation,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue> = emptyMap(),
        state: Map<String, DynamicValue> = emptyMap(),
    ): DynamicValidationResult =
        validateComponents(presentation.components, bindings, context, state)


    /** Generic, backend-authored policy: only the edited Input's error may clear. */
    fun clearsErrorOnEdit(screen: DynamicScreen, binding: String): Boolean =
        elements(screen.template.components)
            .filterIsInstance<InputElement>()
            .any { it.binding?.key == binding && it.validation?.clearOnEdit == true }

    private fun validateComponents(
        components: List<DynamicComponent>,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
        suppressedBinding: String? = null,
    ): DynamicValidationResult {
        val presentedBindings = validationPresentedBindings(components)
        val errors = linkedMapOf<String, String>()

        elements(components).forEach { element ->
            if (element is InputElement && element.binding?.key == suppressedBinding &&
                suppressedBinding != null
            ) return@forEach
            validateElement(element, bindings, context, state)?.let { (key, message) ->
                if (key !in errors) errors[key] = message
            }
        }

        val fallback = errors.entries.firstOrNull { it.key !in presentedBindings }?.value
        return DynamicValidationResult(errors, fallback)
    }

    /** Common traversal for validation and authored on-edit behavior. */
    private fun elements(components: List<DynamicComponent>): Sequence<DynamicElement> = sequence {
        components.forEach { component ->
            yieldAll(component.elements)
            component.sections.forEach { section ->
                yieldAll(section.elements)
                section.groups.forEach { group -> yieldAll(group.elements) }
            }
        }
    }

    private fun validationPresentedBindings(
        components: List<DynamicComponent>,
    ): Set<String> = buildSet {
        components.forEach { component ->
            component.sections.forEach { section ->
                (section as? FormFieldSection)
                    ?.validationPresentation
                    ?.binding
                    ?.let(::add)
            }
        }
    }

    private fun validateElement(
        element: DynamicElement,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): Pair<String, String>? =
        when (element) {
            is InputElement -> validateInput(element, bindings, context, state)
            else -> null
        }

    private fun validateInput(
        element: InputElement,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): Pair<String, String>? {
        val validation = element.validation ?: return null
        val key = element.binding?.key ?: return null
        val value = bindings[key] ?: DynamicValue.NullValue
        val text = (value as? DynamicValue.StringValue)?.value.orEmpty()

        if (validation.required == true && text.isBlank()) {
            return key to (validation.requiredMessage ?: validation.message ?: "This field is required")
        }
        if (text.isBlank()) return null

        validation.pattern?.let { pattern ->
            if (!Regex(pattern).matches(text)) {
                return key to (invalidMessage(validation, text))
            }
        }

        validation.equals?.let { comparison ->
            val actual = DynamicReferenceResolver.resolve(comparison.value, bindings, context, state)
            val expected = DynamicReferenceResolver.resolve(comparison.expected, bindings, context, state)
            if (actual == DynamicValue.NullValue || expected == DynamicValue.NullValue ||
                actual != expected
            ) {
                return key to invalidMessage(validation, text)
            }
        }

        return null
    }

    private fun invalidMessage(validation: DynamicValidation, text: String): String =
        validation.lengthMessages.firstOrNull { rule ->
            val minLength = rule.minLength
            val maxLength = rule.maxLength
            (minLength == null || text.length >= minLength) &&
                (maxLength == null || text.length <= maxLength)
        }?.message ?: validation.message ?: "Invalid value"
}
