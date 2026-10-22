package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
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
    val listCatalog: DynamicListCatalog? = null,
    val onListItemSelected: (String, String) -> Unit = { _, _ -> },
    val listFallbackSections: Map<String, StackSection> = emptyMap(),
    val rowItem: Map<String, DynamicValue>? = null,
    val rowSelected: Boolean? = null,
    val trustedConfig: Map<String, DynamicValue>? = null,
) {
    fun resolve(value: DynamicValue): DynamicValue =
        DynamicReferenceResolver.resolve(value, bindings, contextValues, stateValues, rowItem, trustedConfig)

    fun string(properties: Map<String, DynamicValue>, key: String): String? =
        (resolve(properties[key] ?: return null) as? DynamicValue.StringValue)?.value

    fun number(properties: Map<String, DynamicValue>, key: String): Double? =
        (resolve(properties[key] ?: return null) as? DynamicValue.NumberValue)?.value

    fun boolean(properties: Map<String, DynamicValue>, key: String): Boolean? =
        (resolve(properties[key] ?: return null) as? DynamicValue.BooleanValue)?.value

    /** Resolve through the same pure evaluator used by Store selection validation. */
    internal fun listRows(section: ListSection): List<DynamicListRow> =
        DynamicListItems(requireNotNull(listCatalog) { "Dynamic list catalog provider is missing" })
            .evaluate(section, bindings)

    /** Scope sibling fallbacks to the concrete Component currently rendering. */
    fun withListFallbacks(fallbacks: Map<String, StackSection>): DynamicRenderContext =
        child(fallbacks = fallbacks)

    /** A row-scoped immutable value snapshot; never stored in screen bindings. */
    fun withListRow(item: Map<String, DynamicValue>, selected: Boolean): DynamicRenderContext =
        child(item = item.toMap(), selected = selected)

    private fun child(
        fallbacks: Map<String, StackSection> = listFallbackSections,
        item: Map<String, DynamicValue>? = rowItem,
        selected: Boolean? = rowSelected,
    ) = DynamicRenderContext(
        registry = registry,
        trustedAssetOrigin = trustedAssetOrigin,
        bindings = bindings,
        contextValues = contextValues,
        stateValues = stateValues,
        validationErrors = validationErrors,
        focusedBinding = focusedBinding,
        onAction = onAction,
        onButtonAction = onButtonAction,
        pendingButtonId = pendingButtonId,
        onValueChange = onValueChange,
        onFocusChange = onFocusChange,
        listCatalog = listCatalog,
        onListItemSelected = onListItemSelected,
        listFallbackSections = fallbacks,
        rowItem = item,
        rowSelected = selected,
        trustedConfig = trustedConfig,
    )

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
