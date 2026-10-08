package com.carbroz.cbpartner.feature.dynamic.sdui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent as StackComponentModel
import com.carbroz.cbpartner.feature.dynamic.registry.ComponentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class StackComponent : ComponentRenderer {
    @Composable
    override fun render(component: StackComponentModel, context: DynamicRenderContext) {
        if (component.orientation == "horizontal") {
            Row(
                modifier = Modifier.applyLayout(component),
                horizontalArrangement = component.horizontalArrangement.toHorizontalArrangement(),
                verticalAlignment = component.verticalAlignment.toVerticalAlignment(),
            ) {
                context.renderElements(component.elements)
                context.renderSections(component.sections)
            }
        } else {
            Column(
                modifier = Modifier.applyLayout(component),
                verticalArrangement = component.verticalArrangement.toVerticalArrangement(),
                horizontalAlignment = component.horizontalAlignment.toHorizontalAlignment(),
            ) {
                context.renderElements(component.elements)
                context.renderSections(component.sections)
            }
        }
    }
}