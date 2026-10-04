package com.carbroz.cbpartner.feature.dynamic.renderer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.DynamicSection
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.sdui.component.StackComponent
import com.carbroz.cbpartner.feature.dynamic.sdui.element.ImageElement
import com.carbroz.cbpartner.feature.dynamic.sdui.element.TextElement
import com.carbroz.cbpartner.feature.dynamic.sdui.group.StackGroup
import com.carbroz.cbpartner.feature.dynamic.sdui.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.sdui.template.StackTemplate

class DynamicRenderer(
    private val registry: DynamicRegistry,
    private val onAction: (DynamicAction) -> Unit = {},
    private val onValueChange: (String, String) -> Unit = { _, _ -> },
) {
    @Composable
    fun Render(
        response: DynamicResponse,
        destination: DynamicDestination,
    ) {
        val screen = response.data
        if (screen == null) {
            Text(text = response.message)
            return
        }
        renderScreen(screen, destination)
    }

    @Composable
    private fun renderScreen(
        screen: DynamicScreen,
        destination: DynamicDestination,
    ) {
        val responseTemplate = screen.template

        if (screen.screenId != destination.screenId) {
            Text(text = "Dynamic screen identity mismatch: ${destination.screenId} != ${screen.screenId}")
            return
        }

        if (destination.templateId != null && destination.templateId != responseTemplate.id) {
            Text(text = "Dynamic template identity mismatch: ${destination.templateId} != ${responseTemplate.id}")
            return
        }

        val selectedTemplateType = destination.templateType ?: responseTemplate.type
        if (selectedTemplateType != responseTemplate.type) {
            Text(text = "Dynamic template type mismatch: $selectedTemplateType != ${responseTemplate.type}")
            return
        }

        when (val registered = registry.getTemplate(selectedTemplateType)) {
            is StackTemplate -> registered.Render(responseTemplate, this)
            else -> Text(text = "Unsupported template: $selectedTemplateType")
        }
    }

    @Composable
    fun renderComponents(components: List<DynamicComponent>) {
        components.forEach { component ->
            when (val registered = registry.getComponent(component.type)) {
                is StackComponent -> registered.Render(component, this)
                else -> Text(text = "Unsupported component: ${component.type}")
            }
        }
    }

    @Composable
    fun renderSections(sections: List<DynamicSection>) {
        sections.forEach { section ->
            when (val registered = registry.getSection(section.type)) {
                is StackSection -> registered.Render(section, this)
                else -> Text(text = "Unsupported section: ${section.type}")
            }
        }
    }

    @Composable
    fun renderGroups(groups: List<DynamicGroup>) {
        groups.forEach { group ->
            when (val registered = registry.getGroup(group.type)) {
                is StackGroup -> registered.Render(group, this)
                else -> Text(text = "Unsupported group: ${group.type}")
            }
        }
    }

    @Composable
    fun renderElements(elements: List<DynamicElement>) {
        elements.forEach { element ->
            when (val registered = registry.getElement(element.type)) {
                is TextElement -> registered.Render(element)
                is ImageElement -> registered.Render(element)
                else -> Text(text = "Unsupported element: ${element.type}")
            }
        }
    }
}
