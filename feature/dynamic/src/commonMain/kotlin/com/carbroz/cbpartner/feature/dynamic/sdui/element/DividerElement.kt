package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement as DividerElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.sdui.dynamicGestures

/** Draws only standalone DividerElement; accessory DividerContent has its own renderer. */
class DividerElement : ElementRenderer<DividerElementModel> {
    @Composable
    override fun render(element: DividerElementModel, context: DynamicRenderContext) {
        val horizontal = element.orientation == "horizontal"
        val width = if (horizontal) requireNotNull(element.width) else element.thickness
        val height = if (horizontal) element.thickness else requireNotNull(element.height)

        Box(
            modifier = Modifier
                .applyDynamicSpacing(element.spacing)
                .width(width.dp)
                .height(height.dp)
                .background(requireNotNull(element.color.toColor()) {
                    "Invalid dynamic divider color"
                })
                .dynamicGestures(element.actions, context.onAction, element.id),
        )
    }
}
