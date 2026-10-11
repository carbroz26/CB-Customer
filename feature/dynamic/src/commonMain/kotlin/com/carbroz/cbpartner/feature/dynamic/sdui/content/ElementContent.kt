package com.carbroz.cbpartner.feature.dynamic.sdui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent as DividerContentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent as IconContentModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementContentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.generated.resources.Res
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dynamic_icon_fallback
import org.jetbrains.compose.resources.painterResource

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
        Row(
            modifier = Modifier.contentClick(content, context).applyDynamicSpacing(model.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DynamicAssetImage(
                url = model.url,
                fallbackUrl = model.fallbackUrl,
                fallbackPainter = painterResource(Res.drawable.dynamic_icon_fallback),
                contentDescription = model.semanticRole,
                size = model.size,
                color = model.color,
            )
        }
    }
}

class DividerContent : ElementContentRenderer {
    @Composable
    override fun render(content: ElementContent, context: DynamicRenderContext) {
        val model = content as? DividerContentModel ?: return
        val color = model.color?.toColor() ?: return
        val modifier = when (model.orientation) {
            "vertical" -> Modifier
                .width(model.width?.dp ?: model.thickness.dp)
                .height(model.height?.dp ?: return)
            else -> Modifier
                .width(model.width?.dp ?: return)
                .height(model.thickness.dp)
        }
        Box(
            modifier = Modifier
                .applyDynamicSpacing(model.spacing)
                .then(modifier)
                .background(color)
                .contentClick(content, context),
        )
    }
}
