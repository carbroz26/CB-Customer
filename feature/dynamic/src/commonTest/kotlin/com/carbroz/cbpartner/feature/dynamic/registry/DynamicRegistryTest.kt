
package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent as StackComponentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent as IconContentModel
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup as StackGroupModel
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection as StackSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate as FormTemplateModel
import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.IconContent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ButtonElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.IconElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ImageElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.InputElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import com.carbroz.cbpartner.feature.dynamic.sdui.group.StackGroup
import com.carbroz.cbpartner.feature.dynamic.sdui.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.sdui.template.FormTemplate
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class DynamicRegistryTest {

    @Test
    fun registrationsRemainSeparatedByCategory() {
        val template = FormTemplateModel(id = "template")
        val textElement = TextElementModel(id = "text")
        val iconContent = IconContentModel(url = "https://example.com/icon.png")

        val registry = DynamicRegistry.builder()
            .template(FormTemplateModel::class, FormTemplate())
            .component(StackComponentModel::class, StackComponent())
            .section(StackSectionModel::class, StackSection())
            .group(StackGroupModel::class, StackGroup())
            .element(TextElementModel::class, TextElement())
            .element(
                com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement::class,
                ImageElement(),
            )
            .element(
                com.carbroz.cbpartner.domain.model.dynamic.element.InputElement::class,
                InputElement(),
            )
            .element(
                com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement::class,
                ButtonElement(),
            )
            .element(
                com.carbroz.cbpartner.domain.model.dynamic.element.IconElement::class,
                IconElement(),
            )
            .content(IconContentModel::class, IconContent())
            .build()

        assertNotNull(registry.template(template))
        assertNotNull(registry.component(StackComponentModel(id = "component")))
        assertNotNull(registry.section(StackSectionModel(id = "section")))
        assertNotNull(registry.group(StackGroupModel(id = "group")))
        assertNotNull(registry.element(textElement))
        assertNotNull(registry.content(iconContent))

        val templateOnlyRegistry = DynamicRegistry.builder()
            .template(FormTemplateModel::class, FormTemplate())
            .build()

        assertNotNull(templateOnlyRegistry.template(template))
        assertNull(templateOnlyRegistry.element(textElement))
        assertNull(templateOnlyRegistry.content(iconContent))
    }

    @Test
    fun rendererLookupUsesTheRegisteredModelClass() {
        val renderer = TextElement()
        val registry = DynamicRegistry.builder()
            .element(TextElementModel::class, renderer)
            .build()

        assertSame(
            renderer,
            registry.element(TextElementModel(id = "first")),
        )
        assertSame(
            renderer,
            registry.element(TextElementModel(id = "second")),
        )
    }

    @Test
    fun duplicateRegistrationForTheSameModelClassIsRejected() {
        val original = FormTemplate()
        val builder = DynamicRegistry.builder()
            .template(FormTemplateModel::class, original)

        assertFailsWith<IllegalStateException> {
            builder.template(
                FormTemplateModel::class,
                FormTemplate(),
            )
        }

        assertSame(
            original,
            builder.build().template(FormTemplateModel(id = "template")),
        )
    }

    @Test
    fun duplicateRegistrationIsRejectedIndependentlyPerCategory() {
        val original = TextElement()
        val builder = DynamicRegistry.builder()
            .element(TextElementModel::class, original)
            .template(FormTemplateModel::class, FormTemplate())

        assertFailsWith<IllegalStateException> {
            builder.element(TextElementModel::class, TextElement())
        }

        assertSame(
            original,
            builder.build().element(TextElementModel(id = "text")),
        )
        assertNotNull(
            builder.build().template(FormTemplateModel(id = "template")),
        )
    }
}
