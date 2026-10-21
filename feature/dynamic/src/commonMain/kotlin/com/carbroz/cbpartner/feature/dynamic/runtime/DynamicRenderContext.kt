package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry

class DynamicRenderContext(
    val registry: DynamicRegistry,
    val trustedAssetOrigin: String? = null,
    val bindings: Map<String, DynamicValue> = emptyMap(),
    val contextValues: Map<String, DynamicValue> = emptyMap(),
    val stateValues: Map<String, DynamicValue> = emptyMap(),
    val validationErrors: Map<String, String> = emptyMap(),
    val focusedBinding: String? = null,
    val onAction: (DynamicAction) -> Unit = {},
    val onButtonAction: (DynamicAction, String) -> Unit = { action, _ -> onAction(action) },
    val pendingButtonId: String? = null,
    val onValueChange: (String, DynamicValue) -> Unit = { _, _ -> },
    val onFocusChange: (String, Boolean) -> Unit = { _, _ -> },
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
    fun renderComponent(component: DynamicComponent) {
        val renderer = registry.component(component)
            ?: error("No Dynamic component renderer registered for model class: " + component::class)
        renderer.render(component, this)
    }

    @Composable
    fun renderSection(section: DynamicSection) {
        val renderer = registry.section(section)
            ?: error("No Dynamic section renderer registered for model class: " + section::class)
        renderer.render(section, this)
    }

    @Composable
    fun renderGroup(group: DynamicGroup) {
        val renderer = registry.group(group)
            ?: error("No Dynamic group renderer registered for model class: " + group::class)
        renderer.render(group, this)
    }

    @Composable
    fun renderElement(element: DynamicElement) {
        val renderer = registry.element(element)
            ?: error("No Dynamic element renderer registered for model class: " + element::class)
        renderer.render(element, this)
    }

    @Composable
    fun renderContentItem(content: ElementContent) {
        val renderer = registry.content(content)
            ?: error("No Dynamic content renderer registered for model class: " + content::class)
        renderer.render(content, this)
    }

    @Composable
    fun <T : DynamicComponent> renderComponents(components: List<T>) {
        components.forEach { component ->
            renderComponent(component)
        }
    }

    @Composable
    fun <T : DynamicSection> renderSections(sections: List<T>) {
        sections.forEach { section ->
            renderSection(section)
        }
    }

    @Composable
    fun <T : DynamicGroup> renderGroups(groups: List<T>) {
        groups.forEach { group ->
            renderGroup(group)
        }
    }

    @Composable
    fun renderElements(elements: List<DynamicElement>) {
        elements.forEach { element ->
            renderElement(element)
        }
    }

    @Composable
    fun renderContent(contents: List<ElementContent>) {
        contents.forEach { content ->
            renderContentItem(content)
        }
    }
}
