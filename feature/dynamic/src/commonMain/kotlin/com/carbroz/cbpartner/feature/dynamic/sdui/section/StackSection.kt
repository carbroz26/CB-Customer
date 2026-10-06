package com.carbroz.cbpartner.feature.dynamic.sdui.section

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.StackSection as StackSectionModel
import com.carbroz.cbpartner.feature.dynamic.registry.SectionRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class StackSection : SectionRenderer {
    @Composable
    override fun render(section: StackSectionModel, context: DynamicRenderContext) {
        Column(
            modifier = Modifier.applyLayout(section),
            verticalArrangement = section.verticalArrangement.toVerticalArrangement(),
            horizontalAlignment = section.horizontalAlignment.toHorizontalAlignment(),
        ) {
            context.renderElements(section.elements)
            context.renderGroups(section.groups)
        }
    }
}
