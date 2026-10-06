package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
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
            .element("text", TextElement())
            .build()

        assertNotNull(registry.template("form_template"))
        assertNotNull(registry.component("stack_component"))
        assertNotNull(registry.section("stack_section"))
        assertNotNull(registry.group("stack_group"))
        assertNotNull(registry.element("text"))

        assertNull(registry.template("text"))
        assertNull(registry.element("stack_component"))
    }

    @Test
    fun unknownTypesAreNotFound() {
        val registry = DynamicRegistry.builder().build()

        assertNull(registry.template("unknown_template"))
        assertNull(registry.element("unknown_element"))
    }

    @Test
    fun duplicateRegistrationIsRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicRegistry.builder()
                .element("text", TextElement())
                .element("text", TextElement())
        }
    }
}
