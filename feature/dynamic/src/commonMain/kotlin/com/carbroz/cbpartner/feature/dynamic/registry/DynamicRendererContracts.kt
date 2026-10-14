package com.carbroz.cbpartner.feature.dynamic.registry

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext

fun interface TemplateRenderer<T : Any> {
    @Composable
    fun render(template: T, context: DynamicRenderContext)
}

fun interface ComponentRenderer<T : Any> {
    @Composable fun render(component: T, context: DynamicRenderContext)
}

fun interface SectionRenderer<T : Any> {
    @Composable fun render(section: T, context: DynamicRenderContext)
}

fun interface GroupRenderer<T : Any> {
    @Composable fun render(group: T, context: DynamicRenderContext)
}

fun interface ElementRenderer<T : DynamicElement> {
    @Composable fun render(element: T, context: DynamicRenderContext)
}

fun interface ElementContentRenderer<T : ElementContent> {
    @Composable fun render(content: T, context: DynamicRenderContext)
}
