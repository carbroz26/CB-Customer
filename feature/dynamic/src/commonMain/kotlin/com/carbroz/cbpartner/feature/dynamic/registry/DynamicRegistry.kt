package com.carbroz.cbpartner.feature.dynamic.registry

class DynamicRegistry(
    private val templates: Map<String, TemplateRenderer> = emptyMap(),
    private val components: Map<String, ComponentRenderer> = emptyMap(),
    private val sections: Map<String, SectionRenderer> = emptyMap(),
    private val groups: Map<String, GroupRenderer> = emptyMap(),
    private val elements: Map<String, ElementRenderer> = emptyMap(),
) {
    fun template(type: String): TemplateRenderer? = templates[type]
    fun component(type: String): ComponentRenderer? = components[type]
    fun section(type: String): SectionRenderer? = sections[type]
    fun group(type: String): GroupRenderer? = groups[type]
    fun element(type: String): ElementRenderer? = elements[type]

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val templates = linkedMapOf<String, TemplateRenderer>()
        private val components = linkedMapOf<String, ComponentRenderer>()
        private val sections = linkedMapOf<String, SectionRenderer>()
        private val groups = linkedMapOf<String, GroupRenderer>()
        private val elements = linkedMapOf<String, ElementRenderer>()

        fun template(type: String, renderer: TemplateRenderer) = apply {
            check(templates.put(type, renderer) == null) { "Dynamic template already registered: $type" }
        }

        fun component(type: String, renderer: ComponentRenderer) = apply {
            check(components.put(type, renderer) == null) { "Dynamic component already registered: $type" }
        }

        fun section(type: String, renderer: SectionRenderer) = apply {
            check(sections.put(type, renderer) == null) { "Dynamic section already registered: $type" }
        }

        fun group(type: String, renderer: GroupRenderer) = apply {
            check(groups.put(type, renderer) == null) { "Dynamic group already registered: $type" }
        }

        fun element(type: String, renderer: ElementRenderer) = apply {
            check(elements.put(type, renderer) == null) { "Dynamic element already registered: $type" }
        }

        fun build(): DynamicRegistry = DynamicRegistry(
            templates = templates.toMap(),
            components = components.toMap(),
            sections = sections.toMap(),
            groups = groups.toMap(),
            elements = elements.toMap(),
        )
    }
}
