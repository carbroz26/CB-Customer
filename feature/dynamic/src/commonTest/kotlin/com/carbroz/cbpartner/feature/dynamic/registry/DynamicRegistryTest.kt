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
import kotlin.test.assertSame

class DynamicRegistryTest {
    @Test
    fun registrationsRemainSeparatedByCategory() {
        val registry = DynamicRegistry.builder()
            .template("form_template", FormTemplateRenderer())
            .component("stack_component", StackComponent())
            .section("stack_section", StackSection())
            .group("stack_group", StackGroup())
            .element("text", TextElement())
            .element("image", ImageElement())
            .element("input", InputElement())
            .element("button", ButtonElement())
            .element("icon", IconElement())
            .content("icon", IconContent())
            .build()

        assertNotNull(registry.template("form_template"))
        assertNotNull(registry.component("stack_component"))
        assertNotNull(registry.section("stack_section"))
        assertNotNull(registry.group("stack_group"))
        assertNotNull(registry.element("text"))
        assertNotNull(registry.element("image"))
        assertNotNull(registry.element("input"))
        assertNotNull(registry.element("button"))
        assertNotNull(registry.element("icon"))
        assertNotNull(registry.content("icon"))

        assertNull(registry.template("text"))
        assertNull(registry.element("unknown_element"))
        assertNull(registry.content("unknown_content"))
    }

    @Test
    fun elementRegistrationKeysAreNormalizedForRegistrationAndLookup() {
        val original = ElementRenderer { _, _ -> }
        val registry = DynamicRegistry.builder()
            .element(" TEXT ", original)
            .build()

        assertSame(original, registry.element("text"))
        assertSame(original, registry.element(" TEXT "))
    }

    @Test
    fun duplicateNormalizedElementKeyIsRejectedWithoutReplacingTheFirstRegistration() {
        val original = ElementRenderer { _, _ -> }
        val builder = DynamicRegistry.builder()
            .element("text", original)

        assertFailsWith<IllegalStateException> {
            builder.element(" TEXT ", ElementRenderer { _, _ -> })
        }

        assertSame(original, builder.build().element("text"))
    }

    @Test
    fun blankElementKeyIsRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicRegistry.builder().element("  ", ElementRenderer { _, _ -> })
        }
    }

    @Test
    fun duplicateRegistrationInOtherCategoriesDoesNotReplaceExistingRenderer() {
        val original = FormTemplateRenderer()
        val builder = DynamicRegistry.builder()
            .template("form_template", original)

        assertFailsWith<IllegalStateException> {
            builder.template(" FORM_TEMPLATE ", FormTemplateRenderer())
        }

        assertSame(original, builder.build().template("form_template"))
    }
}
