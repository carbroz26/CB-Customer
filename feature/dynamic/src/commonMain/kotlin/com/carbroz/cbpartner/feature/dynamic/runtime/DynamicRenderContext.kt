package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry

class DynamicRenderContext(
    val registry: DynamicRegistry,
    val bindings: Map<String, DynamicValue> = emptyMap(),
    val contextValues: Map<String, DynamicValue> = emptyMap(),
    val stateValues: Map<String, DynamicValue> = emptyMap(),
    val onAction: (DynamicAction) -> Unit = {},
    val onValueChange: (String, DynamicValue) -> Unit = { _, _ -> },
) {
    fun resolve(value: DynamicValue): DynamicValue =
        DynamicReferenceResolver.resolve(value, bindings, contextValues, stateValues)

    fun string(properties: Map<String, DynamicValue>, key: String): String? =
        (resolve(properties[key] ?: return null) as? DynamicValue.StringValue)?.value

    fun number(properties: Map<String, DynamicValue>, key: String): Double? =
        (resolve(properties[key] ?: return null) as? DynamicValue.NumberValue)?.value

    fun boolean(properties: Map<String, DynamicValue>, key: String): Boolean? =
        (resolve(properties[key] ?: return null) as? DynamicValue.BooleanValue)?.value

    @Composable
    fun renderComponents(components: List<StackComponent>) =
        components.forEach { registry.component("stack_component")?.render(it, this) }

    @Composable
    fun renderSections(sections: List<StackSection>) =
        sections.forEach { registry.section("stack_section")?.render(it, this) }

    @Composable
    fun renderGroups(groups: List<StackGroup>) =
        groups.forEach { registry.group("stack_group")?.render(it, this) }

    @Composable
    fun renderElements(elements: List<DynamicElement>) {
        elements.forEach { element ->
            when (element) {
                is TextElement -> (registry.text("text")
                    ?: error("No Dynamic element renderer registered for type: text"))
                    .render(element, this)
                is ImageElement -> (registry.image("image")
                    ?: error("No Dynamic element renderer registered for type: image"))
                    .render(element, this)
                is InputElement -> (registry.input("input")
                    ?: error("No Dynamic element renderer registered for type: input"))
                    .render(element, this)
                is ButtonElement -> (registry.button("button")
                    ?: error("No Dynamic element renderer registered for type: button"))
                    .render(element, this)
                is IconElement -> (registry.icon("icon")
                    ?: error("No Dynamic element renderer registered for type: icon"))
                    .render(element, this)
                else -> error(
                    "No Dynamic element renderer registered for model class: ${element::class}",
                )
            }
        }
    }

    @Composable
    fun renderContent(contents: List<ElementContent>) =
        contents.forEach { registry.content(contentType(it))?.render(it, this) }

    private fun contentType(content: ElementContent): String = when (content) {
        is IconContent -> "icon"
        is DividerContent -> "divider"
    }
}
