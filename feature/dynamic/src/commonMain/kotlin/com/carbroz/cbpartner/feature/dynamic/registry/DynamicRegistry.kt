package com.carbroz.cbpartner.feature.dynamic.registry

class DynamicRegistry(
    private val templates: Map<String, TemplateRenderer> = emptyMap(),
    private val components: Map<String, ComponentRenderer> = emptyMap(),
    private val sections: Map<String, SectionRenderer> = emptyMap(),
    private val groups: Map<String, GroupRenderer> = emptyMap(),
    private val textElements: Map<String, TextElementRenderer> = emptyMap(),
    private val imageElements: Map<String, ImageElementRenderer> = emptyMap(),
    private val inputElements: Map<String, InputElementRenderer> = emptyMap(),
    private val buttonElements: Map<String, ButtonElementRenderer> = emptyMap(),
    private val iconElements: Map<String, IconElementRenderer> = emptyMap(),
    private val contents: Map<String, ElementContentRenderer> = emptyMap(),
) {
    fun template(type: String): TemplateRenderer? = templates[type.normalizeType()]
    fun component(type: String): ComponentRenderer? = components[type.normalizeType()]
    fun section(type: String): SectionRenderer? = sections[type.normalizeType()]
    fun group(type: String): GroupRenderer? = groups[type.normalizeType()]
    fun textElement(type: String): TextElementRenderer? = textElements[type.normalizeType()]
    fun imageElement(type: String): ImageElementRenderer? = imageElements[type.normalizeType()]
    fun inputElement(type: String): InputElementRenderer? = inputElements[type.normalizeType()]
    fun buttonElement(type: String): ButtonElementRenderer? = buttonElements[type.normalizeType()]
    fun iconElement(type: String): IconElementRenderer? = iconElements[type.normalizeType()]
    fun content(type: String): ElementContentRenderer? = contents[type.normalizeType()]

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val templates = linkedMapOf<String, TemplateRenderer>()
        private val components = linkedMapOf<String, ComponentRenderer>()
        private val sections = linkedMapOf<String, SectionRenderer>()
        private val groups = linkedMapOf<String, GroupRenderer>()
        private val textElements = linkedMapOf<String, TextElementRenderer>()
        private val imageElements = linkedMapOf<String, ImageElementRenderer>()
        private val inputElements = linkedMapOf<String, InputElementRenderer>()
        private val buttonElements = linkedMapOf<String, ButtonElementRenderer>()
        private val iconElements = linkedMapOf<String, IconElementRenderer>()
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

        fun textElement(type: String, renderer: TextElementRenderer) = apply {
            register(textElements, "text element", type, renderer)
        }

        fun imageElement(type: String, renderer: ImageElementRenderer) = apply {
            register(imageElements, "image element", type, renderer)
        }

        fun inputElement(type: String, renderer: InputElementRenderer) = apply {
            register(inputElements, "input element", type, renderer)
        }

        fun buttonElement(type: String, renderer: ButtonElementRenderer) = apply {
            register(buttonElements, "button element", type, renderer)
        }

        fun iconElement(type: String, renderer: IconElementRenderer) = apply {
            register(iconElements, "icon element", type, renderer)
        }

        fun content(type: String, renderer: ElementContentRenderer) = apply {
            register(contents, "content", type, renderer)
        }

        fun build(): DynamicRegistry = DynamicRegistry(
            templates = templates.toMap(),
            components = components.toMap(),
            sections = sections.toMap(),
            groups = groups.toMap(),
            textElements = textElements.toMap(),
            imageElements = imageElements.toMap(),
            inputElements = inputElements.toMap(),
            buttonElements = buttonElements.toMap(),
            iconElements = iconElements.toMap(),
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
            check(target.put(key, value) == null) { "Dynamic $category already registered: $type" }
        }
    }
}

private fun String.normalizeType(): String = trim().lowercase()