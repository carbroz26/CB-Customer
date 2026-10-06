package com.carbroz.cbpartner.feature.dynamic.runtime

import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.InputElement

class DynamicFormValidator {
    fun validate(screen: DynamicScreen, bindings: Map<String, String>): String? {
        screen.template.components.forEach { component ->
            component.elements.forEach { element -> validateElement(element, bindings)?.let { return it } }
            component.sections.forEach { section ->
                section.elements.forEach { element -> validateElement(element, bindings)?.let { return it } }
                section.groups.forEach { group ->
                    group.elements.forEach { element -> validateElement(element, bindings)?.let { return it } }
                }
            }
        }
        return null
    }

    private fun validateElement(element: DynamicElement, bindings: Map<String, String>): String? {
        if (element !is InputElement) return null
        val validation = element.validation ?: return null
        val key = element.binding?.key ?: return null
        val value = bindings[key].orEmpty()
        if (validation.required == true && value.isBlank()) return validation.message ?: "This field is required"
        val pattern = validation.pattern
        if (pattern != null && value.isNotBlank() && !Regex(pattern).matches(value)) {
            return validation.message ?: "Invalid value"
        }
        return null
    }
}
