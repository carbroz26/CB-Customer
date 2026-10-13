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
    fun registrationsRemainSeparatedByCategoryAndUseTypedElementRenderers() {
        val registry = DynamicRegistry.builder()
            .template("form_template", FormTemplateRenderer())
            .component("stack_component", StackComponent())
            .section("stack_section", StackSection())
            .group("stack_group", StackGroup())
            .text("text", TextElement())
            .image("image", ImageElement())
            .input("input", InputElement())
            .button("button", ButtonElement())
            .icon("icon", IconElement())
            .content("icon", IconContent())
            .build()

        assertNotNull(registry.template("form_template"))
        assertNotNull(registry.component("stack_component"))
        assertNotNull(registry.section("stack_section"))
        assertNotNull(registry.group("stack_group"))
        assertNotNull(registry.text("text"))
        assertNotNull(registry.image("image"))
        assertNotNull(registry.input("input"))
        assertNotNull(registry.button("button"))
        assertNotNull(registry.icon("icon"))
        assertNotNull(registry.content("icon"))

        assertNull(registry.template("text"))
        assertNull(registry.text("unknown_text"))
        assertNull(registry.image("unknown_image"))
        assertNull(registry.input("unknown_input"))
        assertNull(registry.button("unknown_button"))
        assertNull(registry.icon("unknown_icon"))
        assertNull(registry.content("unknown_content"))
    }

    @Test
    fun elementRegistrationKeysAreNormalizedForRegistrationAndLookup() {
        val original = TextElementRenderer { _, _ -> }
        val registry = DynamicRegistry.builder()
            .text(" TEXT ", original)
            .build()

        assertSame(original, registry.text("text"))
        assertSame(original, registry.text(" TEXT "))
    }

    @Test
    fun duplicateNormalizedElementKeyIsRejectedWithoutReplacingTheFirstRegistration() {
        val original = TextElementRenderer { _, _ -> }
        val builder = DynamicRegistry.builder()
            .text("text", original)

        assertFailsWith<IllegalStateException> {
            builder.text(" TEXT ", TextElementRenderer { _, _ -> })
        }

        assertSame(original, builder.build().text("text"))
    }

    @Test
    fun blankElementKeyIsRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicRegistry.builder().text("  ", TextElementRenderer { _, _ -> })
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
