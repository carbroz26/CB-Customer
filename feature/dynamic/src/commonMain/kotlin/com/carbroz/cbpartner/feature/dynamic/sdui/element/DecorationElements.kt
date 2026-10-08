package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.IconElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class IconElement : IconElementRenderer {
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
            context.renderContent(element.leadingContent)
            Text(
                text = glyph,
                color = element.color?.toColor() ?: Color.Unspecified,
                fontSize = element.size?.sp ?: 18.sp,
            )
            context.renderContent(element.trailingContent)
        }
    }
}