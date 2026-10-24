package com.carbroz.cbpartner.feature.dynamic.sdui.section

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldControlSurface
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicFocusPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationMessageStyle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation
import kotlin.test.Test
import kotlin.test.assertEquals

/** Renderer-independent border priority and error-style wiring. */
class FormFieldSectionContractTest {
    @Test
    fun otpLikeUnlabeledFieldUsesOnlySegmentStyleWithoutOuterErrorBorder() {
        val input = InputElement(id = "segments", binding = DynamicBinding("otp"))
        val field = FormFieldSection(
            id = "otp", label = null, controlInput = input, inputBinding = "otp",
            controlSurface = FormFieldControlSurface(56.0, true),
            horizontalAlignment = DynamicArrangement.Center, fillMaxWidth = true,
            validationPresentation = DynamicValidationPresentation(
                binding = "otp", errorBorder = null,
                messageStyle = DynamicValidationMessageStyle(fontSize = 14.0),
            ),
        )
        assertEquals(listOf(input), field.elements)
        assertEquals(null, field.resolvedControlBorder(false, true))
        assertEquals(null, field.resolvedControlBorder(true, true))
    }

    @Test
    fun fieldBorderPriorityIsErrorThenFocusThenNormal() {
        val normal = DynamicBorder(width = 1.0, color = "#CBD5E1")
        val focus = DynamicBorder(width = 1.0, color = "#0A3A8D")
        val error = DynamicBorder(width = 1.0, color = "#DC2626")
        val field = FormFieldSection(
            id = "reusable",
            label = TextElement(id = "label"),
            controlInput = InputElement(id = "input", binding = DynamicBinding("field")),
            inputBinding = "field",
            controlSurface = FormFieldControlSurface(56.0, true, border = normal),
            horizontalAlignment = DynamicArrangement.Start,
            fillMaxWidth = true,
            focusPresentation = DynamicFocusPresentation("field", focus),
            validationPresentation = DynamicValidationPresentation(
                "field", error,
                DynamicValidationMessageStyle(fontSize = 14.0, lineHeight = 20.0),
            ),
        )
        assertEquals(normal, field.resolvedControlBorder(false, false))
        assertEquals(focus, field.resolvedControlBorder(true, false))
        assertEquals(error, field.resolvedControlBorder(false, true))
        assertEquals(error, field.resolvedControlBorder(true, true))
        assertEquals(56.0, field.controlSurface.height)
        assertEquals(14.0, field.validationPresentation?.messageStyle?.fontSize)
        assertEquals(20.0, field.validationPresentation?.messageStyle?.lineHeight)
    }
}
