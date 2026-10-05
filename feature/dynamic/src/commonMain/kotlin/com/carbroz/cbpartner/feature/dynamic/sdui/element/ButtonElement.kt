package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.boolean
import com.carbroz.cbpartner.feature.dynamic.runtime.color
import com.carbroz.cbpartner.feature.dynamic.runtime.number
import com.carbroz.cbpartner.feature.dynamic.runtime.shape
import com.carbroz.cbpartner.feature.dynamic.runtime.string
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class ButtonElement : ElementRenderer {
    @Composable
    override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val properties = element.properties
        val shape = properties.shape()
        val enabled = properties.boolean("enabled") ?: true
        val text = properties.string("text").orEmpty()
        val textColor = properties.color("textColor") ?: Color.White
        val background = properties["background"] as? DynamicValue.ObjectValue
        val brush = background?.let(::gradientBrush)
        val backgroundColor = (background?.value?.get("color") as? DynamicValue.StringValue)?.value?.toColor() ?: Color.Transparent

        Box(
            modifier = context.applyLayout(Modifier, properties)
                .clip(shape ?: androidx.compose.foundation.shape.RoundedCornerShape(0.dp))
                .background(brush ?: Brush.linearGradient(listOf(backgroundColor, backgroundColor)))
                .clickable(enabled = enabled) { element.actions["onClick"]?.let(context.onAction) },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = text, color = textColor, fontSize = properties.number("fontSize")?.sp ?: 18.sp, fontWeight = properties.number("fontWeight")?.toInt()?.let(::FontWeight))
                val trailing = properties["trailing"] as? DynamicValue.ArrayValue
                if (!trailing?.value.isNullOrEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text("→", color = textColor, fontSize = 22.sp)
                }
            }
        }
    }

    private fun gradientBrush(value: DynamicValue.ObjectValue): Brush? {
        if ((value.value["type"] as? DynamicValue.StringValue)?.value != "linearGradient") return null
        val colors = (value.value["colors"] as? DynamicValue.ArrayValue)?.value?.mapNotNull { item ->
            val color = item as? DynamicValue.ObjectValue ?: return@mapNotNull null
            (color.value["color"] as? DynamicValue.StringValue)?.value?.toColor()
        }.orEmpty()
        return colors.takeIf { it.size >= 2 }?.let(Brush::linearGradient)
    }
}
