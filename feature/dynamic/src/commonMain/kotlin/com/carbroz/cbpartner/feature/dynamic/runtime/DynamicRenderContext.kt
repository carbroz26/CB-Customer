package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.*
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry

class DynamicRenderContext(
    val registry: DynamicRegistry,
    val bindings: Map<String, String> = emptyMap(),
    val contextValues: Map<String, String> = emptyMap(),
    val onAction: (DynamicAction) -> Unit = {},
    val onValueChange: (String, String) -> Unit = { _, _ -> },
) {
    fun resolve(value: DynamicValue): DynamicValue = when (value) {
        is DynamicValue.ObjectValue -> {
            val binding = (value.value["\$binding"] as? DynamicValue.StringValue)?.value
            val context = (value.value["\$context"] as? DynamicValue.StringValue)?.value
            when {
                binding != null -> DynamicValue.StringValue(bindings[binding].orEmpty())
                context != null -> DynamicValue.StringValue(contextValues[context].orEmpty())
                else -> DynamicValue.ObjectValue(value.value.mapValues { (_, child) -> resolve(child) })
            }
        }
        is DynamicValue.ArrayValue -> DynamicValue.ArrayValue(value.value.map(::resolve))
        else -> value
    }

    fun string(properties: Map<String, DynamicValue>, key: String): String? =
        (resolve(properties[key] ?: return null) as? DynamicValue.StringValue)?.value

    fun number(properties: Map<String, DynamicValue>, key: String): Double? =
        (resolve(properties[key] ?: return null) as? DynamicValue.NumberValue)?.value

    fun boolean(properties: Map<String, DynamicValue>, key: String): Boolean? =
        (resolve(properties[key] ?: return null) as? DynamicValue.BooleanValue)?.value

    @Composable fun renderComponents(components: List<StackComponent>) =
        components.forEach { registry.component("stack_component")?.render(it, this) }

    @Composable fun renderSections(sections: List<StackSection>) =
        sections.forEach { registry.section("stack_section")?.render(it, this) }

    @Composable fun renderGroups(groups: List<StackGroup>) =
        groups.forEach { registry.group("stack_group")?.render(it, this) }

    @Composable fun renderElements(elements: List<DynamicElement>) =
        elements.forEach { registry.element(elementType(it))?.render(it, this) }

    @Composable fun renderContent(contents: List<ElementContent>) =
        contents.forEach { registry.content(contentType(it))?.render(it, this) }

    private fun contentType(content: ElementContent): String = when (content) {
        is com.carbroz.cbpartner.domain.model.dynamic.content.IconContent -> "icon"
        is com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent -> "divider"
    }

    private fun elementType(element: DynamicElement): String = when (element) {
        is TextElement -> "text"
        is ImageElement -> "image"
        is InputElement -> "input"
        is ButtonElement -> "button"
        is IconElement -> "icon"
        is DividerElement -> "divider"
    }
}
