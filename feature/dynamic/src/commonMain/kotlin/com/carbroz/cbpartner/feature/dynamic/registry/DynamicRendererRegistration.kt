package com.carbroz.cbpartner.feature.dynamic.registry

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
    .textElement("text", TextElement())
    .imageElement("image", ImageElement())
    .inputElement("input", InputElement())
    .buttonElement("button", ButtonElement())
    .iconElement("icon", IconElement())
    .content("divider", DividerContent())
    .content("icon", IconContent())
    .build()