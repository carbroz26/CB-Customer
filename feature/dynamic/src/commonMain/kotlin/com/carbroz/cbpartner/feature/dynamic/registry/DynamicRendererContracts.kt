package com.carbroz.cbpartner.feature.dynamic.registry

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext

fun interface TemplateRenderer {
    @Composable fun render(template: FormTemplate, context: DynamicRenderContext)
}

fun interface ComponentRenderer {
    @Composable fun render(component: StackComponent, context: DynamicRenderContext)
}

fun interface SectionRenderer {
    @Composable fun render(section: StackSection, context: DynamicRenderContext)
}

fun interface GroupRenderer {
    @Composable fun render(group: StackGroup, context: DynamicRenderContext)
}

fun interface TextElementRenderer {
    @Composable fun render(element: TextElement, context: DynamicRenderContext)
}

fun interface ImageElementRenderer {
    @Composable fun render(element: ImageElement, context: DynamicRenderContext)
}

fun interface InputElementRenderer {
    @Composable fun render(element: InputElement, context: DynamicRenderContext)
}

fun interface ButtonElementRenderer {
    @Composable fun render(element: ButtonElement, context: DynamicRenderContext)
}

fun interface IconElementRenderer {
    @Composable fun render(element: IconElement, context: DynamicRenderContext)
}

fun interface ElementContentRenderer {
    @Composable fun render(content: ElementContent, context: DynamicRenderContext)
}
