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
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement as DividerElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class DividerElement : ElementRenderer {
    @Composable override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val model = element as? DividerElementModel ?: return
        Box(modifier = Modifier.applyLayout(model).background(model.color?.toColor() ?: Color.Gray))
    }
}

class IconElement : ElementRenderer {
    @Composable override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val model = element as? IconElementModel ?: return
        val glyph = when (model.name) {
            "arrow_forward" -> "→"
            "edit" -> "✎"
            else -> "•"
        }
        Row(modifier = Modifier.applyLayout(model), verticalAlignment = Alignment.CenterVertically) {
            Text(text = glyph, color = model.color?.toColor() ?: Color.Unspecified, fontSize = model.size?.sp ?: 18.sp)
        }
    }
}
