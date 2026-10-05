package com.carbroz.cbpartner.feature.dynamic.registry

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTemplate
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext

fun interface TemplateRenderer {
    @Composable
    fun render(template: DynamicTemplate, context: DynamicRenderContext)
}

fun interface ComponentRenderer {
    @Composable
    fun render(component: DynamicComponent, context: DynamicRenderContext)
}

fun interface SectionRenderer {
    @Composable
    fun render(section: DynamicSection, context: DynamicRenderContext)
}

fun interface GroupRenderer {
    @Composable
    fun render(group: DynamicGroup, context: DynamicRenderContext)
}

fun interface ElementRenderer {
    @Composable
    fun render(element: DynamicElement, context: DynamicRenderContext)
}
