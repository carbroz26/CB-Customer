package com.carbroz.cbpartner.feature.dynamic.sdui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.DynamicComponent
import com.carbroz.cbpartner.feature.dynamic.registry.ComponentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.horizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.horizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.string
import com.carbroz.cbpartner.feature.dynamic.runtime.verticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.verticalArrangement

class StackComponent : ComponentRenderer {
    @Composable
    override fun render(component: DynamicComponent, context: DynamicRenderContext) {
        val horizontal = component.properties.string("orientation") == "horizontal"
        if (horizontal) {
            Row(
                modifier = context.applyLayout(Modifier, component.properties, row = true),
                horizontalArrangement = component.properties.horizontalArrangement(),
                verticalAlignment = component.properties.verticalAlignment(),
            ) {
                context.renderElements(component.elements)
                context.renderSections(component.sections)
            }
        } else {
            Column(
                modifier = context.applyLayout(Modifier, component.properties),
                verticalArrangement = component.properties.verticalArrangement(),
                horizontalAlignment = component.properties.horizontalAlignment(),
            ) {
                context.renderElements(component.elements)
                context.renderSections(component.sections)
            }
        }
    }
}
