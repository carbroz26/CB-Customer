package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue

internal fun DynamicRenderContext.applyLayout(
    modifier: Modifier,
    properties: Map<String, DynamicValue>,
    row: Boolean = false,
): Modifier {
    var result = modifier

    properties.padding()?.let { result = result.padding(it) }
    properties.number("width")?.let { result = result.width(it.dp) }
    properties.number("height")?.let { result = result.height(it.dp) }
    if (properties.boolean("fillMaxWidth") == true) result = result.fillMaxWidth()
    if (properties.boolean("fillMaxHeight") == true) result = result.fillMaxHeight()
    if (properties.boolean("fillMaxSize") == true) result = result.fillMaxSize()
    properties.number("weight")?.takeIf { row }?.let { result = result.then(Modifier.weight(it.toFloat())) }
    properties.shape()?.let { result = result.clip(it) }
    properties.backgroundColor()?.let { result = result.background(it, properties.shape() ?: RoundedCornerShape(0.dp)) }
    properties.border()?.let { result = result.border(it, properties.shape() ?: RoundedCornerShape(0.dp)) }
    return result
}

internal fun Map<String, DynamicValue>.padding(): PaddingValues? {
    val value = this["padding"] as? DynamicValue.ObjectValue ?: return null
    fun number(key: String) = (value.value[key] as? DynamicValue.NumberValue)?.value?.dp ?: 0.dp
    return PaddingValues(
        start = number("start"),
        top = number("top"),
        end = number("end"),
        bottom = number("bottom"),
    )
}

internal fun Map<String, DynamicValue>.number(key: String): Double? =
    (this[key] as? DynamicValue.NumberValue)?.value

internal fun Map<String, DynamicValue>.boolean(key: String): Boolean? =
    (this[key] as? DynamicValue.BooleanValue)?.value

internal fun Map<String, DynamicValue>.string(key: String): String? =
    (this[key] as? DynamicValue.StringValue)?.value

internal fun Map<String, DynamicValue>.color(key: String): Color? = string(key)?.toColor()

internal fun Map<String, DynamicValue>.backgroundColor(): Color? {
    val background = this["background"] as? DynamicValue.ObjectValue
    return (background?.value?.get("color") as? DynamicValue.StringValue)?.value?.toColor()
}

internal fun Map<String, DynamicValue>.border(): BorderStroke? {
    val border = this["border"] as? DynamicValue.ObjectValue ?: return null
    val width = (border.value["width"] as? DynamicValue.NumberValue)?.value?.dp ?: return null
    val color = (border.value["color"] as? DynamicValue.StringValue)?.value?.toColor() ?: return null
    return BorderStroke(width, color)
}

internal fun Map<String, DynamicValue>.shape(): Shape? {
    val shape = this["shape"] as? DynamicValue.ObjectValue ?: return null
    val type = (shape.value["type"] as? DynamicValue.StringValue)?.value ?: return null
    if (type != "roundedCorner") return null
    val radius = (shape.value["cornerRadius"] as? DynamicValue.NumberValue)?.value ?: 0.0
    return RoundedCornerShape(radius.dp)
}

internal fun String.toColor(): Color? = runCatching {
    Color(android.graphics.Color.parseColor(this))
}.getOrNull()

internal fun Map<String, DynamicValue>.horizontalAlignment(): Alignment.Horizontal = when (string("horizontalAlignment")) {
    "start" -> Alignment.Start
    "end" -> Alignment.End
    else -> Alignment.CenterHorizontally
}

internal fun Map<String, DynamicValue>.verticalAlignment(): Alignment.Vertical = when (string("verticalAlignment")) {
    "top" -> Alignment.Top
    "bottom" -> Alignment.Bottom
    else -> Alignment.CenterVertically
}

internal fun Map<String, DynamicValue>.horizontalArrangement(): Arrangement.Horizontal {
    val arrangement = this["horizontalArrangement"] as? DynamicValue.ObjectValue
    return if ((arrangement?.value?.get("type") as? DynamicValue.StringValue)?.value == "spacedBy") {
        Arrangement.spacedBy(((arrangement.value["spacing"] as? DynamicValue.NumberValue)?.value ?: 0.0).dp)
    } else {
        Arrangement.Start
    }
}

internal fun Map<String, DynamicValue>.verticalArrangement(): Arrangement.Vertical {
    val arrangement = this["verticalArrangement"] as? DynamicValue.ObjectValue
    return if ((arrangement?.value?.get("type") as? DynamicValue.StringValue)?.value == "spacedBy") {
        Arrangement.spacedBy(((arrangement.value["spacing"] as? DynamicValue.NumberValue)?.value ?: 0.0).dp)
    } else {
        Arrangement.Top
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun RowScope.dynamicWeight(properties: Map<String, DynamicValue>): Modifier =
    properties.number("weight")?.let { Modifier.weight(it.toFloat()) } ?: Modifier
