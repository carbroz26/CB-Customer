package com.carbroz.cbpartner.feature.dynamic.sdui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent as DividerContentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent as IconContentModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementContentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

private fun Modifier.contentClick(
    content: ElementContent,
    context: DynamicRenderContext,
): Modifier = if (content.actions["onClick"] != null) {
    clickable { content.actions["onClick"]?.let(context.onAction) }
} else {
    this
}

class IconContent : ElementContentRenderer {
    @Composable
    override fun render(content: ElementContent, context: DynamicRenderContext) {
        val model = content as? IconContentModel ?: return
        val glyph = when (model.name) {
            "arrow_forward" -> "→"
            "edit" -> "✎"
            else -> "•"
        }
        Row(
            modifier = Modifier.contentClick(content, context),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = glyph,
                color = model.color?.toColor() ?: Color.Unspecified,
                fontSize = model.size?.sp ?: 18.sp,
            )
        }
    }
}

class DividerContent : ElementContentRenderer {
    @Composable
    override fun render(content: ElementContent, context: DynamicRenderContext) {
        val model = content as? DividerContentModel ?: return
        val modifier = when (model.orientation) {
            "vertical" -> Modifier
                .width(model.width?.dp ?: 1.dp)
                .height(model.height?.dp ?: 36.dp)
            else -> Modifier
                .width(model.width?.dp ?: 36.dp)
                .height(model.thickness.dp)
        }
        Box(
            modifier = modifier
                .background(model.color?.toColor() ?: Color.Gray)
                .contentClick(content, context),
        )
    }
}
