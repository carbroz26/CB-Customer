package com.carbroz.cbpartner.feature.dynamic.sdui.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.DynamicGroup
import com.carbroz.cbpartner.feature.dynamic.registry.GroupRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.horizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.horizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.string
import com.carbroz.cbpartner.feature.dynamic.runtime.verticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.verticalArrangement

class StackGroup : GroupRenderer {
    @Composable
    override fun render(group: DynamicGroup, context: DynamicRenderContext) {
        if (group.properties.string("orientation") == "horizontal") {
            Row(
                modifier = context.applyLayout(Modifier, group.properties, row = true),
                horizontalArrangement = group.properties.horizontalArrangement(),
                verticalAlignment = group.properties.verticalAlignment(),
            ) { context.renderElements(group.elements) }
        } else {
            Column(
                modifier = context.applyLayout(Modifier, group.properties),
                verticalArrangement = group.properties.verticalArrangement(),
                horizontalAlignment = group.properties.horizontalAlignment(),
            ) { context.renderElements(group.elements) }
        }
    }
}
