
package com.carbroz.cbpartner.feature.dynamic.sdui.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent as DividerContentModel
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent as IconContentModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementContentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.sdui.element.dynamicGestures
import com.carbroz.cbpartner.feature.dynamic.generated.resources.Res
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dynamic_icon_fallback
import org.jetbrains.compose.resources.painterResource

private fun Modifier.contentClick(content: ElementContent, context: DynamicRenderContext): Modifier =
    dynamicGestures(content.actions, context.onAction)

class IconContent : ElementContentRenderer<IconContentModel> {
    @Composable
    override fun render(
        content: IconContentModel,
        context: DynamicRenderContext,
    ) {
        Row(
            modifier = Modifier
                .contentClick(content, context)
                .applyDynamicSpacing(content.spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DynamicAssetImage(
                url = content.url,
                fallbackUrl = content.fallbackUrl,
                fallbackPainter = painterResource(Res.drawable.dynamic_icon_fallback),
                contentDescription = content.semanticRole,
                size = content.size,
                color = content.color,
            )
        }
    }
}

class DividerContent : ElementContentRenderer<DividerContentModel> {
    @Composable
    override fun render(
        content: DividerContentModel,
        context: DynamicRenderContext,
    ) {
        val color = content.color?.toColor() ?: return

        val modifier = when (content.orientation) {
            "vertical" -> Modifier
                .width(content.width?.dp ?: content.thickness.dp)
                .height(content.height?.dp ?: return)

            else -> Modifier
                .width(content.width?.dp ?: return)
                .height(content.thickness.dp)
        }

        Box(
            modifier = Modifier
                .applyDynamicSpacing(content.spacing)
                .then(modifier)
                .background(color)
                .contentClick(content, context),
        )
    }
}
