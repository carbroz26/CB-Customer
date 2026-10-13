package com.carbroz.cbpartner.feature.dynamic.registry

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
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

fun interface ElementRenderer {
    @Composable fun render(element: DynamicElement, context: DynamicRenderContext)
}

fun interface ElementContentRenderer {
    @Composable fun render(content: ElementContent, context: DynamicRenderContext)
}
