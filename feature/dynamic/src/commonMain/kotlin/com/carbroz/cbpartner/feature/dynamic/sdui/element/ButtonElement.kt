package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement as ButtonElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.brush
import com.carbroz.cbpartner.feature.dynamic.runtime.color
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeShape

class ButtonElement : ElementRenderer<ButtonElementModel> {
    @Composable
    override fun render(element: ButtonElementModel, context: DynamicRenderContext) {
        val backgroundBrush = element.background.brush()
        val backgroundColor = element.background.color()
        var modifier = Modifier.applyLayout(element)
            .then(if (element.shape != null) Modifier.clip(element.shape.toComposeShape()) else Modifier)

        modifier = when {
            backgroundBrush != null -> modifier.background(backgroundBrush)
            backgroundColor != null -> modifier.background(backgroundColor)
            else -> modifier
        }

        Box(
            modifier = modifier.clickable {
                element.actions["onClick"]?.let(context.onAction)
            },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                context.renderContent(element.leadingContent)
                androidx.compose.material3.Text(
                    text = element.text,
                    color = element.textColor?.toColor() ?: Color.Unspecified,
                    fontSize = element.fontSize?.sp ?: androidx.compose.ui.unit.TextUnit.Unspecified,
                    fontWeight = element.fontWeight?.let(::FontWeight),
                )
                context.renderContent(element.trailingContent)
            }
        }
    }
}