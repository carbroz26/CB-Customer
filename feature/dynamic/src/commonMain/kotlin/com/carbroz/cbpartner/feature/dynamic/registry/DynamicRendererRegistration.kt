package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ButtonElement
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
    .element("text", TextElement())
    .element("image", ImageElement())
    .element("input", InputElement())
    .element("button", ButtonElement())
    .build()
