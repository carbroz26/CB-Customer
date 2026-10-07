package com.carbroz.cbpartner.feature.dynamic.renderer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DynamicRenderer(
    private val registry: DynamicRegistry,
    private val onAction: (DynamicAction) -> Unit = {},
    private val onValueChange: (String, DynamicValue) -> Unit = { _, _ -> },
    private val bindings: Map<String, DynamicValue> = emptyMap(),
    private val contextValues: Map<String, DynamicValue> = emptyMap(),
    private val stateValues: Map<String, DynamicValue> = emptyMap(),
) {
    @Composable
    fun Render(response: DynamicResponse, destination: DynamicDestination) {
        val screen = response.data ?: return
        if (
            screen.screenId != destination.screenId ||
            screen.template.id != destination.templateId ||
            screen.template.type != destination.templateType
        ) return
        val context = DynamicRenderContext(
            registry = registry,
            bindings = bindings,
            contextValues = contextValues,
            stateValues = stateValues,
            onAction = onAction,
            onValueChange = onValueChange,
        )
        Box(modifier = Modifier.fillMaxSize().screenTheme(screen.theme?.properties)) {
            registry.template(screen.template.type)?.render(screen.template, context)
        }
    }

    private fun Modifier.screenTheme(properties: Map<String, DynamicValue>?): Modifier {
        val gradient = properties?.get("gradient") as? DynamicValue.ObjectValue ?: return this
        val colors = (gradient.value["colors"] as? DynamicValue.ArrayValue)?.value?.mapNotNull { item ->
            val objectValue = item as? DynamicValue.ObjectValue ?: return@mapNotNull null
            (objectValue.value["color"] as? DynamicValue.StringValue)?.value?.toColor()
        }.orEmpty()
        if (colors.size < 2) return this
        val angle = (gradient.value["angle"] as? DynamicValue.NumberValue)?.value ?: 0.0
        return drawWithCache {
            val radians = angle * PI / 180.0
            val direction = Offset(cos(radians).toFloat(), sin(radians).toFloat())
            val center = Offset(size.width / 2f, size.height / 2f)
            val length = sqrt(size.width * size.width + size.height * size.height) / 2f
            val brush = Brush.linearGradient(colors = colors, start = center - direction * length, end = center + direction * length)
            onDrawBehind { drawRect(brush = brush) }
        }
    }
}
