
package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import kotlin.reflect.KClass

class DynamicRegistry private constructor(
    private val templates: Map<KClass<*>, TemplateRenderer<*>>,
    private val components: Map<KClass<*>, ComponentRenderer<*>>,
    private val sections: Map<KClass<*>, SectionRenderer<*>>,
    private val groups: Map<KClass<*>, GroupRenderer<*>>,
    private val elements: Map<KClass<*>, ElementRenderer<*>>,
    private val contents: Map<KClass<*>, ElementContentRenderer<*>>,
) {
    fun <T : Any> template(model: T): TemplateRenderer<T>? =
        templates.lookup(model::class)

    fun <T : Any> component(model: T): ComponentRenderer<T>? =
        components.lookup(model::class)

    fun <T : Any> section(model: T): SectionRenderer<T>? =
        sections.lookup(model::class)

    fun <T : Any> group(model: T): GroupRenderer<T>? =
        groups.lookup(model::class)

    fun <T : DynamicElement> element(model: T): ElementRenderer<T>? =
        elements.lookup(model::class)

    fun <T : ElementContent> content(model: T): ElementContentRenderer<T>? =
        contents.lookup(model::class)

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val templates = linkedMapOf<KClass<*>, TemplateRenderer<*>>()
        private val components = linkedMapOf<KClass<*>, ComponentRenderer<*>>()
        private val sections = linkedMapOf<KClass<*>, SectionRenderer<*>>()
        private val groups = linkedMapOf<KClass<*>, GroupRenderer<*>>()
        private val elements = linkedMapOf<KClass<*>, ElementRenderer<*>>()
        private val contents = linkedMapOf<KClass<*>, ElementContentRenderer<*>>()

        fun <T : DynamicTemplate> template(
            type: KClass<T>,
            renderer: TemplateRenderer<T>,
        ) = apply {
            register(templates, "template", type, renderer)
        }

        fun <T : DynamicComponent> component(
            type: KClass<T>,
            renderer: ComponentRenderer<T>,
        ) = apply {
            register(components, "component", type, renderer)
        }

        fun <T : DynamicSection> section(
            type: KClass<T>,
            renderer: SectionRenderer<T>,
        ) = apply {
            register(sections, "section", type, renderer)
        }

        fun <T : DynamicGroup> group(
            type: KClass<T>,
            renderer: GroupRenderer<T>,
        ) = apply {
            register(groups, "group", type, renderer)
        }

        fun <T : DynamicElement> element(
            type: KClass<T>,
            renderer: ElementRenderer<T>,
        ) = apply {
            register(elements, "element", type, renderer)
        }

        fun <T : ElementContent> content(
            type: KClass<T>,
            renderer: ElementContentRenderer<T>,
        ) = apply {
            register(contents, "content", type, renderer)
        }

        fun build(): DynamicRegistry = DynamicRegistry(
            templates = templates.toMap(),
            components = components.toMap(),
            sections = sections.toMap(),
            groups = groups.toMap(),
            elements = elements.toMap(),
            contents = contents.toMap(),
        )

        private fun <T : Any, R : Any> register(
            target: MutableMap<KClass<*>, R>,
            category: String,
            type: KClass<T>,
            renderer: R,
        ) {
            check(type !in target) {
                "Dynamic $category already registered: ${type.qualifiedName}"
            }
            target[type] = renderer
        }
    }
}

private fun <T : Any, R : Any> Map<KClass<*>, R>.lookup(
    type: KClass<out T>,
): R? = this[type]

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Map<KClass<*>, TemplateRenderer<*>>.lookup(
    type: KClass<out T>,
): TemplateRenderer<T>? = this[type] as? TemplateRenderer<T>

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Map<KClass<*>, ComponentRenderer<*>>.lookup(
    type: KClass<out T>,
): ComponentRenderer<T>? = this[type] as? ComponentRenderer<T>

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Map<KClass<*>, SectionRenderer<*>>.lookup(
    type: KClass<out T>,
): SectionRenderer<T>? = this[type] as? SectionRenderer<T>

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Map<KClass<*>, GroupRenderer<*>>.lookup(
    type: KClass<out T>,
): GroupRenderer<T>? = this[type] as? GroupRenderer<T>

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Map<KClass<*>, ElementRenderer<*>>.lookup(
    type: KClass<out T>,
): ElementRenderer<T>? = this[type] as? ElementRenderer<T>

@Suppress("UNCHECKED_CAST")
private fun <T : Any> Map<KClass<*>, ElementContentRenderer<*>>.lookup(
    type: KClass<out T>,
): ElementContentRenderer<T>? = this[type] as? ElementContentRenderer<T>
