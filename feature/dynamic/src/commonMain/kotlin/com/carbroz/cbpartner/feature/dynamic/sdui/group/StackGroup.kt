package com.carbroz.cbpartner.feature.dynamic.sdui.group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup as StackGroupModel
import com.carbroz.cbpartner.feature.dynamic.registry.GroupRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeWeight
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

/** Layout of direct Elements only. List-selected styling is an explicit P1 exception. */
class StackGroup : GroupRenderer<StackGroupModel> {
    @Composable
    override fun render(group: StackGroupModel, context: DynamicRenderContext) {
        val selectedBackground = group.selectedStyle?.background
        val displayed = if (context.rowSelected == true && selectedBackground != null) {
            group.copy(background = selectedBackground)
        } else group

        if (group.orientation == "horizontal") {
            Row(
                modifier = Modifier.applyLayout(displayed),
                horizontalArrangement = group.horizontalArrangement.toHorizontalArrangement(),
                verticalAlignment = group.verticalAlignment.toVerticalAlignment(),
            ) {
                group.elements.forEach { element ->
                    val weight = element.weight
                    if (weight == null) context.renderElement(element)
                    else Box(
                        modifier = Modifier.weight(weight.toComposeWeight()),
                        propagateMinConstraints = true,
                    ) { context.renderElement(element) }
                }
            }
        } else {
            Column(
                modifier = Modifier.applyLayout(displayed),
                verticalArrangement = group.verticalArrangement.toVerticalArrangement(),
                horizontalAlignment = group.horizontalAlignment.toHorizontalAlignment(),
            ) {
                group.elements.forEach { element ->
                    val weight = element.weight
                    if (weight == null) context.renderElement(element)
                    else Box(
                        modifier = Modifier.weight(weight.toComposeWeight()),
                        propagateMinConstraints = true,
                    ) { context.renderElement(element) }
                }
            }
        }
    }
}
