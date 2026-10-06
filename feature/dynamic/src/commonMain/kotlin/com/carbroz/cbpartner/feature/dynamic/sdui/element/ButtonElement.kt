package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.ButtonElement as ButtonElementModel
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBackground
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.brush
import com.carbroz.cbpartner.feature.dynamic.runtime.color
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeShape

class ButtonElement : ElementRenderer {
    @Composable
    override fun render(element: ButtonElementModel, context: DynamicRenderContext) {
        val shape = element.shape.toComposeShape()
        val textColor = element.textColor?.toColor() ?: Color.White

        Box(
            modifier = Modifier
                .applyLayout(element)
                .clip(shape)
                .background(element.background?.brush() ?: Brush.linearGradient(
                    listOf(element.background?.color() ?: Color.Transparent, element.background?.color() ?: Color.Transparent),
                ))
                .clickable { element.actions["onClick"]?.let(context.onAction) },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                context.renderElements(element.leading)
                Text(
                    text = element.text,
                    color = textColor,
                    fontSize = element.fontSize?.sp ?: 18.sp,
                    fontWeight = element.fontWeight?.let(::FontWeight),
                )
                context.renderElements(element.trailing)
            }
        }
    }
}
