package com.carbroz.cbpartner.feature.dynamic.sdui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent as StackComponentModel
import com.carbroz.cbpartner.domain.model.dynamic.section.ListSection
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.feature.dynamic.registry.ComponentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeWeight
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class StackComponent : ComponentRenderer<StackComponentModel> {
    @Composable
    override fun render(component: StackComponentModel, context: DynamicRenderContext) {
        val fallbackIds = component.sections.filterIsInstance<ListSection>()
            .mapNotNull { it.emptySectionId }.toSet()
        val fallbacks = component.sections.filterIsInstance<StackSection>()
            .filter { it.id in fallbackIds }.associateBy { it.id }
        val scoped = context.withListFallbacks(fallbacks)
        if (component.orientation == "horizontal") {
            Row(
                modifier = Modifier.applyLayout(component),
                horizontalArrangement = component.horizontalArrangement.toHorizontalArrangement(),
                verticalAlignment = component.verticalAlignment.toVerticalAlignment(),
            ) {
                component.elements.forEach { element ->
                    val weight = element.weight
                    if (weight == null) {
                        context.renderElement(element)
                    } else {
                        Box(
                            modifier = Modifier.weight(weight.toComposeWeight()),
                            propagateMinConstraints = true,
                        ) {
                            context.renderElement(element)
                        }
                    }
                }
                component.sections.forEach { section ->
                    if (section is StackSection && section.id in fallbackIds) return@forEach
                    val weight = section.weight
                    if (weight == null) {
                        scoped.renderSection(section)
                    } else {
                        Box(
                            modifier = Modifier.weight(weight.toComposeWeight()),
                            propagateMinConstraints = true,
                        ) {
                            context.renderSection(section)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.applyLayout(component),
                verticalArrangement = component.verticalArrangement.toVerticalArrangement(),
                horizontalAlignment = component.horizontalAlignment.toHorizontalAlignment(),
            ) {
                component.elements.forEach { element ->
                    val weight = element.weight
                    if (weight == null) {
                        context.renderElement(element)
                    } else {
                        Box(
                            modifier = Modifier.weight(weight.toComposeWeight()),
                            propagateMinConstraints = true,
                        ) {
                            context.renderElement(element)
                        }
                    }
                }
                component.sections.forEach { section ->
                    val weight = section.weight
                    if (weight == null) {
                        context.renderSection(section)
                    } else {
                        Box(
                            modifier = Modifier.weight(weight.toComposeWeight()),
                            propagateMinConstraints = true,
                        ) {
                            context.renderSection(section)
                        }
                    }
                }
            }
        }
    }
}
