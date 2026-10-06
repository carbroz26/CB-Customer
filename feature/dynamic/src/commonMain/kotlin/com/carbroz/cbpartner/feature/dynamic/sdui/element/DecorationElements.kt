package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class IconElement : ElementRenderer {
    @Composable override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val model = element as? IconElementModel ?: return
        val glyph = when (model.name) {
            "arrow_forward" -> "→"
            "edit" -> "✎"
            else -> "•"
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            context.renderContent(model.leadingContent)
            Text(
                text = glyph,
                color = model.color?.toColor() ?: Color.Unspecified,
                fontSize = model.size?.sp ?: 18.sp,
            )
            context.renderContent(model.trailingContent)
        }
    }
}
