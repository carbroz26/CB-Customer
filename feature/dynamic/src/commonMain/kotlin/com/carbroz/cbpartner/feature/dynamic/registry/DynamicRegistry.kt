package com.carbroz.cbpartner.feature.dynamic.registry

import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import kotlin.reflect.KClass

class DynamicRegistry private constructor(
    private val templates: Map<String, TemplateRenderer>,
    private val components: Map<String, ComponentRenderer>,
    private val sections: Map<String, SectionRenderer>,
    private val groups: Map<String, GroupRenderer>,
    private val elements: Map<KClass<out DynamicElement>, ElementRenderer<DynamicElement>>,
    private val contents: Map<String, ElementContentRenderer>,
) {
    fun template(type: String): TemplateRenderer? = templates[type.normalizeType()]
    fun component(type: String): ComponentRenderer? = components[type.normalizeType()]
    fun section(type: String): SectionRenderer? = sections[type.normalizeType()]
    fun group(type: String): GroupRenderer? = groups[type.normalizeType()]

    fun element(element: DynamicElement): ElementRenderer<DynamicElement> =
        elements[element::class]
            ?: error("No Dynamic element renderer registered for model class: ${element::class}")

    fun content(type: String): ElementContentRenderer? = contents[type.normalizeType()]

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val templates = linkedMapOf<String, TemplateRenderer>()
        private val components = linkedMapOf<String, ComponentRenderer>()
        private val sections = linkedMapOf<String, SectionRenderer>()
        private val groups = linkedMapOf<String, GroupRenderer>()
        private val elements = linkedMapOf<KClass<out DynamicElement>, ElementRenderer<DynamicElement>>()
        private val elementTypes = linkedSetOf<String>()
        private val contents = linkedMapOf<String, ElementContentRenderer>()

        fun template(type: String, renderer: TemplateRenderer) = apply {
            register(templates, "template", type, renderer)
        }

        fun component(type: String, renderer: ComponentRenderer) = apply {
            register(components, "component", type, renderer)
        }

        fun section(type: String, renderer: SectionRenderer) = apply {
            register(sections, "section", type, renderer)
        }

        fun group(type: String, renderer: GroupRenderer) = apply {
            register(groups, "group", type, renderer)
        }

        fun <T : DynamicElement> element(
            type: String,
            modelClass: KClass<T>,
            renderer: ElementRenderer<T>,
        ) = apply {
            val key = type.normalizeType()
            check(key.isNotEmpty()) { "Dynamic element type is blank" }
            check(key !in elementTypes) { "Dynamic element type already registered: $type" }
            check(modelClass !in elements) {
                "Dynamic element model class already registered: $modelClass"
            }

            val adapter: ElementRenderer<DynamicElement> =
                CheckedElementRenderer(modelClass, renderer)
            elementTypes += key
            elements[modelClass] = adapter
        }

        fun content(type: String, renderer: ElementContentRenderer) = apply {
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

        private fun <T> register(
            target: MutableMap<String, T>,
            category: String,
            type: String,
            value: T,
        ) {
            val key = type.normalizeType()
            check(key.isNotEmpty()) { "Dynamic $category type is blank" }
            check(key !in target) { "Dynamic $category already registered: $type" }
            target[key] = value
        }
    }
}

private fun String.normalizeType(): String = trim().lowercase()
