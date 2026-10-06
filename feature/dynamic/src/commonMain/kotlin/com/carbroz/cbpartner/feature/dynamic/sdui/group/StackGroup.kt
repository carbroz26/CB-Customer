package com.carbroz.cbpartner.feature.dynamic.sdui.group

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup as StackGroupModel
import com.carbroz.cbpartner.feature.dynamic.registry.GroupRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class StackGroup : GroupRenderer {
    @Composable override fun render(group: StackGroupModel, context: DynamicRenderContext) {
        if (group.orientation == "horizontal") Row(
            modifier = Modifier.applyLayout(group),
            horizontalArrangement = group.horizontalArrangement.toHorizontalArrangement(),
            verticalAlignment = group.verticalAlignment.toVerticalAlignment(),
        ) { context.renderElements(group.elements) }
        else Column(
            modifier = Modifier.applyLayout(group),
            verticalArrangement = group.verticalArrangement.toVerticalArrangement(),
            horizontalAlignment = group.horizontalAlignment.toHorizontalAlignment(),
        ) { context.renderElements(group.elements) }
    }
}