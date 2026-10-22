package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing

internal fun Modifier.applyLayout(template: FormTemplate): Modifier =
    applyDynamicPadding(template.padding).thenIf(template.fillMaxSize) { fillMaxSize() }

internal fun Modifier.applyLayout(component: StackComponent): Modifier =
    thenIf(component.fillMaxWidth) { fillMaxWidth() }
        .thenIf(component.fillMaxHeight) { fillMaxHeight() }
        .applyDynamicBackground(component.background, component.shape.toComposeShape())
        .applyDynamicPadding(component.padding)

internal fun Modifier.applyLayout(section: StackSection): Modifier =
    applyDynamicPadding(section.padding).thenIf(section.fillMaxWidth) { fillMaxWidth() }

internal fun Modifier.applyLayout(
    group: StackGroup,
    borderOverride: DynamicBorder? = group.border,
): Modifier =
    Modifier
        .thenIf(group.height != null) { height(group.height!!.dp) }
        .thenIf(group.minHeight != null) { heightIn(min = group.minHeight!!.dp) }
        .thenIf(group.fillMaxWidth) { fillMaxWidth() }
        // Clip child backgrounds to the authored Group shape; the validation message
        // is rendered by the outer Group Column and is not clipped.
        .thenIf(group.shape != null) { clip(group.shape.toComposeShape()) }
        .applyDynamicBackground(group.background, group.shape.toComposeShape())
        .applyBorder(borderOverride, group.shape)
        .applyDynamicPadding(group.padding)

internal fun Modifier.applyLayout(element: TextElement): Modifier =
    applyDynamicSpacing(element.spacing)
        .applyDynamicPadding(element.padding)
        .thenIf(element.width != null) { width(element.width!!.dp) }
        .thenIf(element.maxWidth != null) { widthIn(max = element.maxWidth!!.dp) }
        .thenIf(element.minHeight != null) { heightIn(min = element.minHeight!!.dp) }
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }

internal fun Modifier.applyLayout(element: ImageElement): Modifier =
    applyDynamicPadding(element.padding).applyImageSize(element)

internal fun Modifier.applyImageSize(element: ImageElement): Modifier =
    thenIf(element.width != null) { width(element.width!!.dp) }
        .thenIf(element.height != null) { height(element.height!!.dp) }
        .thenIf(element.maxWidth != null) { widthIn(max = element.maxWidth!!.dp) }
        .thenIf(element.maxHeight != null) { heightIn(max = element.maxHeight!!.dp) }
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }
        .thenIf(element.fillMaxHeight) { fillMaxHeight() }

internal fun Modifier.applyLayout(element: InputElement): Modifier =
    applyDynamicPadding(element.padding)
        .thenIf(element.width != null) { width(element.width!!.dp) }
        .thenIf(element.maxWidth != null) { widthIn(max = element.maxWidth!!.dp) }
        .thenIf(element.height != null) { height(element.height!!.dp) }
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }

internal fun Modifier.applyLayout(element: ButtonElement): Modifier =
    applyDynamicPadding(element.padding)
        .thenIf(element.width != null) { width(element.width!!.dp) }
        .thenIf(element.maxWidth != null) { widthIn(max = element.maxWidth!!.dp) }
        .thenIf(element.height != null) { height(element.height!!.dp) }
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }

internal fun Modifier.applyLayout(element: IconElement): Modifier =
    applyDynamicPadding(element.padding)
        .thenIf(element.size != null) { width(element.size!!.dp).height(element.size!!.dp) }
        .thenIf(element.minWidth != null) { widthIn(min = element.minWidth!!.dp) }
        .thenIf(element.minHeight != null) { heightIn(min = element.minHeight!!.dp) }

internal fun Modifier.applyDynamicPadding(padding: DynamicPadding?): Modifier =
    padding?.let {
        androidx.compose.foundation.layout.PaddingValues(
            it.start.dp,
            it.top.dp,
            it.end.dp,
            it.bottom.dp,
        )
    }?.let(::padding) ?: this

internal fun Modifier.applyDynamicSpacing(spacing: DynamicSpacing?): Modifier =
    spacing?.let {
        androidx.compose.foundation.layout.PaddingValues(
            it.start.dp,
            it.top.dp,
            it.end.dp,
            it.bottom.dp,
        )
    }?.let(::padding) ?: this

internal fun DynamicArrangement.toVerticalArrangement(): Arrangement.Vertical = when (this) {
    DynamicArrangement.Top -> Arrangement.Top
    DynamicArrangement.Bottom -> Arrangement.Bottom
    DynamicArrangement.Center -> Arrangement.Center
    DynamicArrangement.SpaceBetween -> Arrangement.SpaceBetween
    DynamicArrangement.End -> Arrangement.Bottom
    DynamicArrangement.Start -> Arrangement.Top
    is DynamicArrangement.SpacedBy -> Arrangement.spacedBy(spacing.dp)
}

internal fun DynamicArrangement.toHorizontalArrangement(): Arrangement.Horizontal = when (this) {
    DynamicArrangement.Start -> Arrangement.Start
    DynamicArrangement.End -> Arrangement.End
    DynamicArrangement.Center -> Arrangement.Center
    DynamicArrangement.SpaceBetween -> Arrangement.SpaceBetween
    DynamicArrangement.Top -> Arrangement.Start
    DynamicArrangement.Bottom -> Arrangement.End
    is DynamicArrangement.SpacedBy -> Arrangement.spacedBy(spacing.dp)
}

internal fun DynamicArrangement.toHorizontalAlignment(): Alignment.Horizontal = when (this) {
    DynamicArrangement.Start -> Alignment.Start
    DynamicArrangement.End -> Alignment.End
    else -> Alignment.CenterHorizontally
}

internal fun DynamicArrangement.toVerticalAlignment(): Alignment.Vertical = when (this) {
    DynamicArrangement.Top -> Alignment.Top
    DynamicArrangement.Bottom -> Alignment.Bottom
    else -> Alignment.CenterVertically
}

internal fun Modifier.applyLinearGradient(
    angle: Double,
    stops: List<Pair<Double?, Color>>,
): Modifier {
    if (stops.size < 2) return this
    val positions = stops.mapIndexed { index, (stop, color) ->
        val position = stop ?: index.toDouble() / (stops.size - 1)
        if (!position.isFinite() || position !in 0.0..1.0) return this
        position.toFloat() to color
    }
    if (positions.zipWithNext().any { (a, b) -> a.first > b.first }) return this
    if (!angle.isFinite()) return this
    return drawWithCache {
        val radians = angle * kotlin.math.PI / 180.0
        val direction = androidx.compose.ui.geometry.Offset(
            kotlin.math.cos(radians).toFloat(),
            kotlin.math.sin(radians).toFloat(),
        )
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val length = kotlin.math.sqrt(size.width * size.width + size.height * size.height) / 2f
        val brush = Brush.linearGradient(
            *positions.toTypedArray(),
            start = center - direction * length,
            end = center + direction * length,
        )
        onDrawBehind { drawRect(brush) }
    }
}

internal fun Modifier.applyDynamicBackground(
    background: DynamicBackground?,
    shape: Shape = RectangleShape,
): Modifier =
    when (background) {
        is DynamicBackground.LinearGradient -> {
            clip(shape).applyLinearGradient(
                background.angle,
                background.colors.mapNotNull { stop ->
                    stop.color.toColor()?.let { stop.stop to it }
                },
            )
        }
        is DynamicBackground.Solid ->
            background.color.toColor()?.let { background(it, shape) } ?: this
        null -> this
    }

internal fun DynamicBackground?.color(): Color? = when (this) {
    is DynamicBackground.Solid -> color.toColor()
    else -> null
}

internal fun Modifier.applyBorder(
    border: DynamicBorder?,
    shape: DynamicShape?,
): Modifier {
    if (border == null) return this
    return border(
        BorderStroke(
            border.width.dp,
            border.color.toColor() ?: error("Invalid dynamic border color"),
        ),
        shape.toComposeShape(),
    )
}

internal fun DynamicShape?.toComposeShape(): Shape = when (this) {
    is DynamicShape.RoundedCorner -> {
        val fallback = cornerRadius
        RoundedCornerShape(
            topStart = (topStart ?: fallback).dp,
            topEnd = (topEnd ?: fallback).dp,
            bottomEnd = (bottomEnd ?: fallback).dp,
            bottomStart = (bottomStart ?: fallback).dp,
        )
    }
    null -> RectangleShape
}

internal fun Double.toComposeWeight(): Float {
    require(isFinite() && this > 0.0) { "Dynamic weight must be finite and greater than zero" }
    return toFloat()
}

internal fun String.toColor(): Color? = runCatching {
    val hex = removePrefix("#")
    when (hex.length) {
        6 -> Color(
            hex.substring(0, 2).toInt(16) / 255f,
            hex.substring(2, 4).toInt(16) / 255f,
            hex.substring(4, 6).toInt(16) / 255f,
        )
        8 -> Color(
            hex.substring(2, 4).toInt(16) / 255f,
            hex.substring(4, 6).toInt(16) / 255f,
            hex.substring(6, 8).toInt(16) / 255f,
            hex.substring(0, 2).toInt(16) / 255f,
        )
        else -> null
    }
}.getOrNull()

private inline fun Modifier.thenIf(
    condition: Boolean,
    block: Modifier.() -> Modifier,
): Modifier = if (condition) block() else this
