package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement as ButtonElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.DividerContent
import com.carbroz.cbpartner.feature.dynamic.sdui.content.IconContent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ButtonElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.IconElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ImageElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.InputElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import com.carbroz.cbpartner.feature.dynamic.sdui.group.StackGroup
import com.carbroz.cbpartner.feature.dynamic.sdui.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.sdui.template.FormTemplateRenderer

fun dynamicRendererRegistry(): DynamicRegistry = DynamicRegistry.builder()
    .template("form_template", FormTemplateRenderer())
    .component("stack_component", StackComponent())
    .section("stack_section", StackSection())
    .group("stack_group", StackGroup())
    .element("text", TextElementModel::class, TextElement())
    .element("image", ImageElementModel::class, ImageElement())
    .element("input", InputElementModel::class, InputElement())
    .element("button", ButtonElementModel::class, ButtonElement())
    .element("icon", IconElementModel::class, IconElement())
    .content("divider", DividerContent())
    .content("icon", IconContent())
    .build()