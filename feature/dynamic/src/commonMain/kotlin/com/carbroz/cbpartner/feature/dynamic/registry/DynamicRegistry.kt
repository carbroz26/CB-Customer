package com.carbroz.cbpartner.feature.dynamic.registry

class DynamicRegistry private constructor(
    private val templates: Map<String, TemplateRenderer>,
    private val components: Map<String, ComponentRenderer>,
    private val sections: Map<String, SectionRenderer>,
    private val groups: Map<String, GroupRenderer>,
    private val texts: Map<String, TextElementRenderer>,
    private val images: Map<String, ImageElementRenderer>,
    private val inputs: Map<String, InputElementRenderer>,
    private val buttons: Map<String, ButtonElementRenderer>,
    private val icons: Map<String, IconElementRenderer>,
    private val contents: Map<String, ElementContentRenderer>,
) {
    fun template(type: String): TemplateRenderer? = templates[type.normalizeType()]
    fun component(type: String): ComponentRenderer? = components[type.normalizeType()]
    fun section(type: String): SectionRenderer? = sections[type.normalizeType()]
    fun group(type: String): GroupRenderer? = groups[type.normalizeType()]
    fun text(type: String): TextElementRenderer? = texts[type.normalizeType()]
    fun image(type: String): ImageElementRenderer? = images[type.normalizeType()]
    fun input(type: String): InputElementRenderer? = inputs[type.normalizeType()]
    fun button(type: String): ButtonElementRenderer? = buttons[type.normalizeType()]
    fun icon(type: String): IconElementRenderer? = icons[type.normalizeType()]
    fun content(type: String): ElementContentRenderer? = contents[type.normalizeType()]

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder {
        private val templates = linkedMapOf<String, TemplateRenderer>()
        private val components = linkedMapOf<String, ComponentRenderer>()
        private val sections = linkedMapOf<String, SectionRenderer>()
        private val groups = linkedMapOf<String, GroupRenderer>()
        private val texts = linkedMapOf<String, TextElementRenderer>()
        private val images = linkedMapOf<String, ImageElementRenderer>()
        private val inputs = linkedMapOf<String, InputElementRenderer>()
        private val buttons = linkedMapOf<String, ButtonElementRenderer>()
        private val icons = linkedMapOf<String, IconElementRenderer>()
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

        fun text(type: String, renderer: TextElementRenderer) = apply {
            register(texts, "text element", type, renderer)
        }

        fun image(type: String, renderer: ImageElementRenderer) = apply {
            register(images, "image element", type, renderer)
        }

        fun input(type: String, renderer: InputElementRenderer) = apply {
            register(inputs, "input element", type, renderer)
        }

        fun button(type: String, renderer: ButtonElementRenderer) = apply {
            register(buttons, "button element", type, renderer)
        }

        fun icon(type: String, renderer: IconElementRenderer) = apply {
            register(icons, "icon element", type, renderer)
        }

        fun content(type: String, renderer: ElementContentRenderer) = apply {
            register(contents, "content", type, renderer)
        }

        fun build(): DynamicRegistry = DynamicRegistry(
            templates = templates.toMap(),
            components = components.toMap(),
            sections = sections.toMap(),
            groups = groups.toMap(),
            texts = texts.toMap(),
            images = images.toMap(),
            inputs = inputs.toMap(),
            buttons = buttons.toMap(),
            icons = icons.toMap(),
            contents = contents.toMap(),
        )

        private fun <T> register(
            target: MutableMap<String, T>,
            category: String,
            type: String,
            value: T,
        ) {
            val key = type.normalizeType()
            check(key.isNotEmpty()) { "Dynamic ${category} type is blank" }
            check(key !in target) { "Dynamic ${category} already registered: ${type}" }
            target[key] = value
        }
    }
}

private fun String.normalizeType(): String = trim().lowercase()
