
package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent as StackComponentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent as DividerContentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent as IconContentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.ImageContent as ImageContentModel
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement as ButtonElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement as DividerElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.IconBadgeElement as IconBadgeElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup as StackGroupModel
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection as StackSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection as ListSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection as FormFieldSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate as FormTemplateModel
import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.DividerContent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.IconContent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.ImageContent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ButtonElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.DividerElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.IconBadgeElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.IconElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ImageElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.InputElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import com.carbroz.cbpartner.feature.dynamic.sdui.group.StackGroup
import com.carbroz.cbpartner.feature.dynamic.sdui.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.sdui.section.ListSection
import com.carbroz.cbpartner.feature.dynamic.sdui.section.FormFieldSection
import com.carbroz.cbpartner.feature.dynamic.sdui.template.FormTemplate

fun dynamicRendererRegistry(): DynamicRegistry = DynamicRegistry.builder()
    .template(FormTemplateModel::class, FormTemplate())
    .component(StackComponentModel::class, StackComponent())
    .section(StackSectionModel::class, StackSection())
    .section(ListSectionModel::class, ListSection())
    .section(FormFieldSectionModel::class, FormFieldSection())
    .group(StackGroupModel::class, StackGroup())
    .element(TextElementModel::class, TextElement())
    .element(ImageElementModel::class, ImageElement())
    .element(InputElementModel::class, InputElement())
    .element(ButtonElementModel::class, ButtonElement())
    .element(DividerElementModel::class, DividerElement())
    .element(IconElementModel::class, IconElement())
    .element(IconBadgeElementModel::class, IconBadgeElement())
    .content(DividerContentModel::class, DividerContent())
    .content(IconContentModel::class, IconContent())
    .content(ImageContentModel::class, ImageContent())
    .build()
