package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DynamicRegistryTest {
    @Test
    fun registrationsRemainSeparatedByCategory() {
        val registry = DynamicRegistry().registerDefaults()

        assertNotNull(registry.getTemplate("stack_template"))
        assertNotNull(registry.getComponent("stack_component"))
        assertNotNull(registry.getSection("stack_section"))
        assertNotNull(registry.getGroup("stack_group"))
        assertNotNull(registry.getElement("text"))
        assertNotNull(registry.getElement("image"))

        assertNull(registry.getTemplate("text"))
        assertNull(registry.getElement("stack_template"))
        assertNull(registry.getTemplate("form_template"))
        assertNull(registry.getElement("input"))
        assertNull(registry.getElement("button"))
    }

    @Test
    fun unknownTypesAreNotFound() {
        val registry = DynamicRegistry().registerDefaults()

        assertNull(registry.getTemplate("unknown_template"))
        assertNull(registry.getElement("unknown_element"))
    }

    @Test
    fun duplicateRegistrationIsRejected() {
        val registry = DynamicRegistry().registerDefaults()

        assertFailsWith<IllegalStateException> {
            registry.registerElement("text", TextElement())
        }
    }
}
