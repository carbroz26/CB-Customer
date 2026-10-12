package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement as ButtonElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
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
import kotlin.test.assertTrue

class DynamicRegistryTest {
    @Test
    fun registrationsRemainSeparatedByCategoryAndElementsResolveByModelClass() {
        val registry = DynamicRegistry.builder()
            .template("form_template", FormTemplateRenderer())
            .component("stack_component", StackComponent())
            .section("stack_section", StackSection())
            .group("stack_group", StackGroup())
            .element("text", TextElementModel::class, TextElement())
            .element("image", ImageElementModel::class, ImageElement())
            .element("input", InputElementModel::class, InputElement())
            .element("button", ButtonElementModel::class, ButtonElement())
            .element("icon", IconElementModel::class, IconElement())
            .content("icon", IconContent())
            .build()

        assertNotNull(registry.template("form_template"))
        assertNotNull(registry.component("stack_component"))
        assertNotNull(registry.section("stack_section"))
        assertNotNull(registry.group("stack_group"))
        assertNotNull(registry.element(TextElementModel("text")))
        assertNotNull(registry.element(ImageElementModel("image", url = "image.png")))
        assertNotNull(registry.element(InputElementModel("input")))
        assertNotNull(registry.element(ButtonElementModel("button")))
        assertNotNull(registry.element(IconElementModel("icon", url = "icon.png")))
        assertNotNull(registry.content("icon"))

        assertNull(registry.template("text"))
        assertNull(registry.content("unknown_content"))
    }

    @Test
    fun unknownElementModelsFailWithDeterministicMissingRendererDiagnostic() {
        val registry = DynamicRegistry.builder().build()

        val error = assertFailsWith<IllegalStateException> {
            registry.element(UnknownElement)
        }
        assertTrue(error.message.orEmpty().contains("No Dynamic element renderer registered"))
    }

    @Test
    fun duplicateNormalizedElementKeyIsRejectedWithoutReplacingTheFirstRegistration() {
        val builder = DynamicRegistry.builder()
            .element("text", TextElementModel::class, TextElement())

        assertFailsWith<IllegalStateException> {
            builder.element(" TEXT ", ImageElementModel::class, ImageElement())
        }

        val registry = builder.build()
        val originalRenderer = registry.element(TextElementModel("still-registered"))
        assertSame(originalRenderer, registry.element(TextElementModel("same-model")))
        assertFailsWith<IllegalStateException> {
            registry.element(ImageElementModel("not-registered", url = "image.png"))
        }
    }

    @Test
    fun duplicateElementModelClassIsRejectedEvenWhenKeyDiffers() {
        val builder = DynamicRegistry.builder()
            .element("text", TextElementModel::class, TextElement())

        assertFailsWith<IllegalStateException> {
            builder.element("alternate_text", TextElementModel::class, TextElement())
        }

        assertNotNull(builder.build().element(TextElementModel("still-registered")))
    }

    @Test
    fun blankElementKeyIsRejected() {
        assertFailsWith<IllegalStateException> {
            DynamicRegistry.builder().element("  ", TextElementModel::class, TextElement())
        }
    }

    @Test
    fun checkedAdapterRejectsAModelClassMismatch() {
        val adapter = CheckedElementRenderer(
            TextElementModel::class,
            ElementRenderer<TextElementModel> { _, _ -> },
        )

        assertFailsWith<IllegalStateException> {
            adapter.checkedElement(ButtonElementModel("wrong-model"))
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

    private object UnknownElement : DynamicElement
}
