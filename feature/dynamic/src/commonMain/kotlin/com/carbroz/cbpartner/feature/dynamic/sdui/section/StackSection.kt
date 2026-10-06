package com.carbroz.cbpartner.feature.dynamic.sdui.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection as StackSectionModel
import com.carbroz.cbpartner.feature.dynamic.registry.SectionRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class StackSection : SectionRenderer {
    @Composable override fun render(section: StackSectionModel, context: DynamicRenderContext) {
        if (section.orientation == "horizontal") Row(
            modifier = Modifier.applyLayout(section),
            horizontalArrangement = section.verticalArrangement.toHorizontalArrangement(),
            verticalAlignment = section.horizontalAlignment.toVerticalAlignment(),
        ) { context.renderElements(section.elements); context.renderGroups(section.groups) }
        else Column(
            modifier = Modifier.applyLayout(section),
            verticalArrangement = section.verticalArrangement.toVerticalArrangement(),
            horizontalAlignment = section.horizontalAlignment.toHorizontalAlignment(),
        ) { context.renderElements(section.elements); context.renderGroups(section.groups) }
    }
}