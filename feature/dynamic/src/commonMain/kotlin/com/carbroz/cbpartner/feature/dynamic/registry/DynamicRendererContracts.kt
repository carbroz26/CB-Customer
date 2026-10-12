package com.carbroz.cbpartner.feature.dynamic.registry

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import kotlin.reflect.KClass

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

fun interface ElementRenderer<T : DynamicElement> {
    @Composable fun render(element: T, context: DynamicRenderContext)
}

/**
 * Adapts a strongly typed renderer to the common registry boundary.
 * The runtime class check protects the single controlled generic cast.
 */
internal class CheckedElementRenderer<T : DynamicElement>(
    private val modelClass: KClass<T>,
    private val renderer: ElementRenderer<T>,
) : ElementRenderer<DynamicElement> {
    internal fun checkedElement(element: DynamicElement): T {
        check(modelClass.isInstance(element)) {
            "Dynamic element renderer type mismatch: expected $modelClass but received ${element::class}"
        }
        @Suppress("UNCHECKED_CAST")
        return element as T
    }

    @Composable
    override fun render(element: DynamicElement, context: DynamicRenderContext) {
        renderer.render(checkedElement(element), context)
    }
}

fun interface ElementContentRenderer {
    @Composable fun render(content: ElementContent, context: DynamicRenderContext)
}
