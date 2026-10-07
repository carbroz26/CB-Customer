package com.carbroz.cbpartner.feature.dynamic.registry

class DynamicRegistry(
    private val templates: Map<String, TemplateRenderer> = emptyMap(),
    private val components: Map<String, ComponentRenderer> = emptyMap(),
    private val sections: Map<String, SectionRenderer> = emptyMap(),
    private val groups: Map<String, GroupRenderer> = emptyMap(),
    private val elements: Map<String, ElementRenderer> = emptyMap(),
    private val contents: Map<String, ElementContentRenderer> = emptyMap(),
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
            val key = type.normalizeType(); check(key.isNotEmpty()) { "Dynamic template type is blank" }; check(templates.put(key, renderer) == null) { "Dynamic template already registered: $type" }
        }

        fun component(type: String, renderer: ComponentRenderer) = apply {
            val key = type.normalizeType(); check(key.isNotEmpty()) { "Dynamic component type is blank" }; check(components.put(key, renderer) == null) { "Dynamic component already registered: $type" }
        }

        fun section(type: String, renderer: SectionRenderer) = apply {
            val key = type.normalizeType(); check(key.isNotEmpty()) { "Dynamic section type is blank" }; check(sections.put(key, renderer) == null) { "Dynamic section already registered: $type" }
        }

        fun group(type: String, renderer: GroupRenderer) = apply {
            val key = type.normalizeType(); check(key.isNotEmpty()) { "Dynamic group type is blank" }; check(groups.put(key, renderer) == null) { "Dynamic group already registered: $type" }
        }

        fun element(type: String, renderer: ElementRenderer) = apply {
            val key = type.normalizeType(); check(key.isNotEmpty()) { "Dynamic element type is blank" }; check(elements.put(key, renderer) == null) { "Dynamic element already registered: $type" }
        }

        fun content(type: String, renderer: ElementContentRenderer) = apply {
            val key = type.normalizeType(); check(key.isNotEmpty()) { "Dynamic content type is blank" }; check(contents.put(key, renderer) == null) { "Dynamic content type is blank" };
        }

        fun build(): DynamicRegistry = DynamicRegistry(
            templates = templates.toMap(),
            components = components.toMap(),
            sections = sections.toMap(),
            groups = groups.toMap(),
            elements = elements.toMap(),
            contents = contents.toMap(),
        )
    }
}

private fun String.normalizeType(): String = trim().lowercase()
