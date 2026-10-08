package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.IconContent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ButtonElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.IconElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ImageElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.InputElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import com.carbroz.cbpartner.feature.dynamic.sdui.group.StackGroup
import com.carbroz.cbpartner.feature.dynamic.sdui.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.sdui.template.FormTemplateRenderer
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DynamicRegistryTest {
    @Test
    fun registrationsRemainSeparatedByCategory() {
        val registry = DynamicRegistry.builder()
            .template("form_template", FormTemplateRenderer())
            .component("stack_component", StackComponent())
            .section("stack_section", StackSection())
            .group("stack_group", StackGroup())
            .textElement("text", TextElement())
            .imageElement("image", ImageElement())
            .inputElement("input", InputElement())
            .buttonElement("button", ButtonElement())
            .iconElement("icon", IconElement())
            .content("icon", IconContent())
            .build()

        assertNotNull(registry.template("form_template"))
        assertNotNull(registry.component("stack_component"))
        assertNotNull(registry.section("stack_section"))
        assertNotNull(registry.group("stack_group"))
        assertNotNull(registry.textElement("text"))
        assertNotNull(registry.imageElement("image"))
        assertNotNull(registry.inputElement("input"))
        assertNotNull(registry.buttonElement("button"))
        assertNotNull(registry.iconElement("icon"))
        assertNotNull(registry.content("icon"))

        assertNull(registry.template("text"))
        assertNull(registry.textElement("stack_component"))
    }

    @Test
    fun unknownTypesAreNotFound() {
        val registry = DynamicRegistry.builder().build()

        assertNull(registry.template("unknown_template"))
        assertNull(registry.textElement("unknown_element"))
        assertNull(registry.content("unknown_content"))
    }

    @Test
    fun duplicateRegistrationIsRejectedForConcreteElementCategory() {
        assertFailsWith<IllegalStateException> {
            DynamicRegistry.builder()
                .textElement("text", TextElement())
                .textElement("text", TextElement())
        }
    }
}