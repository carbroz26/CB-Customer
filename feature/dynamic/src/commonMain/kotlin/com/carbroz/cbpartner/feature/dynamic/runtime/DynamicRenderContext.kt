package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
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
            val binding = (value.value["${'$'}binding"] as? DynamicValue.StringValue)?.value
            val context = (value.value["${'$'}context"] as? DynamicValue.StringValue)?.value
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

    @Composable
    fun renderComponents(components: List<DynamicComponent>) {
        components.forEach { component ->
            registry.component(component.type)?.render(component, this)
        }
    }

    @Composable
    fun renderSections(sections: List<DynamicSection>) {
        sections.forEach { section ->
            registry.section(section.type)?.render(section, this)
        }
    }

    @Composable
    fun renderGroups(groups: List<DynamicGroup>) {
        groups.forEach { group ->
            registry.group(group.type)?.render(group, this)
        }
    }

    @Composable
    fun renderElements(elements: List<DynamicElement>) {
        elements.forEach { element ->
            registry.element(element.type)?.render(element, this)
        }
    }
}
