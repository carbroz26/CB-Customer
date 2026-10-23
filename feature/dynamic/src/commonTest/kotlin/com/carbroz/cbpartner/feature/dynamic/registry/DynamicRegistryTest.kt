
package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent as StackComponentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent as DividerContentModel
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement as DividerElementModel
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent as IconContentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.ImageContent as ImageContentModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup as StackGroupModel
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection as StackSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection as ListSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection as FormFieldSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldControlSurface
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate as FormTemplateModel
import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.DividerContent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.IconContent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.ImageContent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ButtonElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.DividerElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.IconElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ImageElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.InputElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import com.carbroz.cbpartner.feature.dynamic.sdui.group.StackGroup
import com.carbroz.cbpartner.feature.dynamic.sdui.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.sdui.section.ListSection
import com.carbroz.cbpartner.feature.dynamic.sdui.section.FormFieldSection
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
        val dividerContent = DividerContentModel()

        val registry = DynamicRegistry.builder()
            .template(FormTemplateModel::class, FormTemplate())
            .component(StackComponentModel::class, StackComponent())
            .section(StackSectionModel::class, StackSection())
            .section(FormFieldSectionModel::class, FormFieldSection())
            .group(StackGroupModel::class, StackGroup())
            .element(TextElementModel::class, TextElement())
            .element(DividerElementModel::class, DividerElement())
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
            .content(ImageContentModel::class, ImageContent())
            .content(DividerContentModel::class, DividerContent())
            .build()

        assertNotNull(registry.template(template))
        assertNotNull(registry.component(StackComponentModel(id = "component")))
        assertNotNull(registry.section(StackSectionModel(id = "section")))
        val field = FormFieldSectionModel(
            id = "first_name",
            label = TextElementModel(id = "label", text = "First name"),
            controlInput = InputElementModel(id = "firstName", binding = DynamicBinding("firstName")),
            inputBinding = "firstName",
            controlSurface = FormFieldControlSurface(height = 56.0, fillMaxWidth = true),
            horizontalAlignment = DynamicArrangement.Start,
            fillMaxWidth = true,
        )
        assertNotNull(registry.section(field))
        assertNotNull(registry.group(StackGroupModel(id = "group")))
        assertNotNull(registry.element(textElement))
        assertNotNull(registry.element(DividerElementModel(id = "divider", orientation = "vertical", height = 28.0, thickness = 1.0, color = "#D1D5DB")))
        assertNotNull(
            registry.element(
                com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement(
                    id = "image",
                    url = "https://example.com/image.png",
                ),
            ),
        )
        assertNotNull(
            registry.element(
                com.carbroz.cbpartner.domain.model.dynamic.element.InputElement(id = "input"),
            ),
        )
        assertNotNull(
            registry.element(
                com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement(id = "button"),
            ),
        )
        assertNotNull(
            registry.element(
                com.carbroz.cbpartner.domain.model.dynamic.element.IconElement(
                    id = "icon",
                    url = "https://example.com/icon.png",
                ),
            ),
        )
        assertNotNull(registry.content(iconContent))
        assertNotNull(registry.content(dividerContent))
        assertNotNull(registry.content(ImageContentModel(source = DynamicAssetSource.Resource(DynamicValue.StringValue("images/logo")))))

        val templateOnlyRegistry = DynamicRegistry.builder()
            .template(FormTemplateModel::class, FormTemplate())
            .build()

        assertNotNull(templateOnlyRegistry.template(template))
        assertNull(templateOnlyRegistry.element(textElement))
        assertNull(templateOnlyRegistry.content(iconContent))
        assertNull(templateOnlyRegistry.content(dividerContent))
        assertNull(templateOnlyRegistry.content(ImageContentModel(source = DynamicAssetSource.Resource(DynamicValue.StringValue("images/logo")))))
    }

    @Test
    fun productionRegistryKeepsDividerElementAndDividerContentDistinct() {
        val registry = dynamicRendererRegistry()
        val divider = DividerElementModel(
            id = "phone_divider",
            orientation = "vertical",
            height = 28.0,
            thickness = 1.0,
            color = "#D1D5DB",
        )
        assertNotNull(registry.element(divider))
        assertNotNull(registry.content(DividerContentModel()))
        assertNotNull(registry.content(ImageContentModel(source = DynamicAssetSource.Resource(DynamicValue.StringValue("images/logo")))))
    }

    @Test
    fun productionRegistryIncludesExactlyOneGenericListSectionRenderer() {
        val registry = dynamicRendererRegistry()
        assertNotNull(registry.section(ListSectionModel(id = "country_list")))
        val builder = DynamicRegistry.builder().section(ListSectionModel::class, ListSection())
        assertFailsWith<IllegalStateException> {
            builder.section(ListSectionModel::class, ListSection())
        }
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
