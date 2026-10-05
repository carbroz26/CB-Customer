package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.color
import com.carbroz.cbpartner.feature.dynamic.runtime.number
import com.carbroz.cbpartner.feature.dynamic.runtime.string

class DividerElement : ElementRenderer {
    @Composable
    override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val properties = element.properties
        val vertical = properties.string("orientation") == "vertical"
        Box(
            modifier = context.applyLayout(Modifier, properties)
                .width(if (vertical) properties.number("thickness")?.dp ?: 1.dp else properties.number("width")?.dp ?: 1.dp)
                .height(if (vertical) properties.number("height")?.dp ?: 1.dp else properties.number("thickness")?.dp ?: 1.dp)
                .background(properties.color("color") ?: Color.Gray),
        )
    }
}

class IconElement : ElementRenderer {
    @Composable
    override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val properties = element.properties
        val glyph = when (properties.string("name")) {
            "arrow_forward" -> "→"
            "edit" -> "✎"
            else -> "•"
        }
        Row(modifier = context.applyLayout(Modifier, properties), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = glyph,
                color = properties.color("color") ?: Color.Unspecified,
                fontSize = properties.number("size")?.dp?.value?.sp ?: 18.dp.value.sp,
            )
        }
    }
}
