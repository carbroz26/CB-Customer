package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.DividerElement as DividerElementModel
import com.carbroz.cbpartner.domain.model.dynamic.IconElement as IconElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class DividerElement : ElementRenderer {
    @Composable
    override fun render(element: DividerElementModel, context: DynamicRenderContext) {
        Box(
            modifier = Modifier
                .applyLayout(element)
                .background(element.color?.toColor() ?: Color.Gray),
        )
    }
}

class IconElement : ElementRenderer {
    @Composable
    override fun render(element: IconElementModel, context: DynamicRenderContext) {
        val glyph = when (element.name) {
            "arrow_forward" -> "→"
            "edit" -> "✎"
            else -> "•"
        }
        Row(
            modifier = Modifier.applyLayout(element),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = glyph,
                color = element.color?.toColor() ?: Color.Unspecified,
                fontSize = element.size?.sp ?: 18.sp,
            )
        }
    }
}
