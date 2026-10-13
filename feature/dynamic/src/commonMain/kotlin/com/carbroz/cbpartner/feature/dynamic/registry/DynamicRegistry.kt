package com.carbroz.cbpartner.feature.dynamic.registry

class DynamicRegistry private constructor(
    private val templates: Map<String, TemplateRenderer>,
    private val components: Map<String, ComponentRenderer>,
    private val sections: Map<String, SectionRenderer>,
    private val groups: Map<String, GroupRenderer>,
    private val elements: Map<String, ElementRenderer>,
    private val contents: Map<String, ElementContentRenderer>,
) {
    fun template(type: String): TemplateRenderer? = templates[type.normalizeType()]
    fun component(type: String): ComponentRenderer? = components[type.normalizeType()]
    fun section(type: String): SectionRenderer? = sections[type.normalizeType()]
    fun group(type: String): GroupRenderer? = groups[type.normalizeType()]
    fun element(type: String): ElementRenderer? = elements[type.normalizeType()]
    fun content(type: String): ElementContentRenderer? = contents[type.normalizeType()]

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val templates = linkedMapOf<String, TemplateRenderer>()
        private val components = linkedMapOf<String, ComponentRenderer>()
        private val sections = linkedMapOf<String, SectionRenderer>()
        private val groups = linkedMapOf<String, GroupRenderer>()
        private val elements = linkedMapOf<String, ElementRenderer>()
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

        fun element(type: String, renderer: ElementRenderer) = apply {
            register(elements, "element", type, renderer)
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
