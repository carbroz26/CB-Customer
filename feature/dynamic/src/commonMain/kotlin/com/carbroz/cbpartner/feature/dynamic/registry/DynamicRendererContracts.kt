package com.carbroz.cbpartner.feature.dynamic.registry

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.StackSection
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext

fun interface TemplateRenderer {
    @Composable
    fun render(template: FormTemplate, context: DynamicRenderContext)
}

fun interface ComponentRenderer {
    @Composable
    fun render(component: StackComponent, context: DynamicRenderContext)
}

fun interface SectionRenderer {
    @Composable
    fun render(section: StackSection, context: DynamicRenderContext)
}

fun interface GroupRenderer {
    @Composable
    fun render(group: StackGroup, context: DynamicRenderContext)
}

fun interface ElementRenderer {
    @Composable
    fun render(element: DynamicElement, context: DynamicRenderContext)
}
