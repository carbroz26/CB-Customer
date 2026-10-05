package com.carbroz.cbpartner.feature.dynamic.sdui.section

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.DynamicSection
import com.carbroz.cbpartner.feature.dynamic.registry.SectionRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.horizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.string
import com.carbroz.cbpartner.feature.dynamic.runtime.verticalArrangement

class StackSection : SectionRenderer {
    @Composable
    override fun render(section: DynamicSection, context: DynamicRenderContext) {
        Column(
            modifier = context.applyLayout(Modifier, section.properties),
            verticalArrangement = section.properties.verticalArrangement(),
            horizontalAlignment = section.properties.horizontalAlignment(),
        ) {
            context.renderElements(section.elements)
            context.renderGroups(section.groups)
        }
    }
}
