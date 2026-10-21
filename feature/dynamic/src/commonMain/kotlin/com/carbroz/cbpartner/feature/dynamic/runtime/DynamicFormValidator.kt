package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
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

class DynamicFormValidator(
    private val validators: DynamicValidatorRegistry = DynamicValidatorRegistry(emptyList()),
) {
    fun validate(
        screen: DynamicScreen,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue> = emptyMap(),
        state: Map<String, DynamicValue> = emptyMap(),
    ): DynamicValidationResult =
        validateComponents(screen.template.components, bindings, context, state)

    fun validate(
        presentation: DynamicPresentation,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue> = emptyMap(),
        state: Map<String, DynamicValue> = emptyMap(),
    ): DynamicValidationResult =
        validateComponents(presentation.components, bindings, context, state)

    private fun validateComponents(
        components: List<DynamicComponent>,
        bindings: Map<String, DynamicValue>,
        context: Map<String, DynamicValue>,
        state: Map<String, DynamicValue>,
    ): DynamicValidationResult {
        val presentedBindings = validationPresentedBindings(components)
        val errors = linkedMapOf<String, String>()

        components.forEach { component ->
            component.elements.forEach { element ->
                validateElement(element, bindings, context, state)?.let { (key, message) ->
                    errors.putIfAbsent(key, message)
                }
            }
            component.sections.forEach { section ->
                section.elements.forEach { element ->
                    validateElement(element, bindings, context, state)?.let { (key, message) ->
                        errors.putIfAbsent(key, message)
                    }
                }
                section.groups.forEach { group ->
                    group.elements.forEach { element ->
                        validateElement(element, bindings, context, state)?.let { (key, message) ->
                            errors.putIfAbsent(key, message)
                        }
                    }
                }
            }
        }

        val fallback = errors.entries.firstOrNull { it.key !in presentedBindings }?.value
        return DynamicValidationResult(errors, fallback)
    }

    private fun validationPresentedBindings(
        components: List<DynamicComponent>,
    ): Set<String> = buildSet {
        components.forEach { component ->
            component.sections.forEach { section ->
                section.groups.forEach { group ->
                    (group as? StackGroup)
                        ?.validationPresentation
                        ?.binding
                        ?.let(::add)
                }
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
            return key to (validation.message ?: "This field is required")
        }
        if (text.isBlank()) return null

        validation.pattern?.let { pattern ->
            if (!Regex(pattern).matches(text)) {
                return key to (validation.message ?: "Invalid value")
            }
        }

        validation.validator?.let { spec ->
            val arguments = spec.arguments.mapValues { (_, argument) ->
                DynamicReferenceResolver.resolve(argument, bindings, context, state)
            }
            val validator = validators.requireValidator(spec.key)
            if (!validator.isValid(value, arguments)) {
                return key to (validation.message ?: "Invalid value")
            }
        }

        return null
    }
}
