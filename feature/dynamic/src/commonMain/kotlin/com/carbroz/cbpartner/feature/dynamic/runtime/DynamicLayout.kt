package com.carbroz.cbpartner.feature.dynamic.runtime

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.StackSection

internal fun Modifier.applyLayout(template: FormTemplate): Modifier =
    padding(template.padding).thenIf(template.fillMaxSize) { fillMaxSize() }

internal fun Modifier.applyLayout(component: StackComponent): Modifier =
    padding(component.padding).thenIf(component.fillMaxWidth) { fillMaxWidth() }

internal fun Modifier.applyLayout(section: StackSection): Modifier =
    padding(section.padding).thenIf(section.fillMaxWidth) { fillMaxWidth() }

internal fun Modifier.applyLayout(group: StackGroup): Modifier =
    padding(group.padding)
        .thenIf(group.height != null) { height(group.height!!.dp) }
        .thenIf(group.fillMaxWidth) { fillMaxWidth() }
        .background(group.background)
        .border(group.border, group.shape)

internal fun Modifier.applyLayout(element: DynamicElement): Modifier = when (element) {
    is com.carbroz.cbpartner.domain.model.dynamic.TextElement -> padding(null)
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }
    is ImageElement -> padding(null)
        .thenIf(element.width != null) { width(element.width!!.dp) }
        .thenIf(element.height != null) { height(element.height!!.dp) }
        .thenIf(element.maxWidth != null) { widthIn(max = element.maxWidth!!.dp) }
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }
    is InputElement -> padding(null)
    is ButtonElement -> padding(null)
        .thenIf(element.height != null) { height(element.height!!.dp) }
        .thenIf(element.fillMaxWidth) { fillMaxWidth() }
        .background(element.background)
        .border(element.shape, element.shape)
    is com.carbroz.cbpartner.domain.model.dynamic.IconElement -> padding(null)
        .thenIf(element.size != null) { width(element.size!!.dp); height(element.size!!.dp) }
    is com.carbroz.cbpartner.domain.model.dynamic.DividerElement -> padding(null)
        .thenIf(element.width != null) { width(element.width!!.dp) }
        .thenIf(element.height != null) { height(element.height!!.dp) }
}

internal fun Modifier.padding(padding: DynamicPadding?): Modifier =
    padding?.let { androidx.compose.foundation.layout.PaddingValues(it.start.dp, it.top.dp, it.end.dp, it.bottom.dp) }
        ?.let(::padding) ?: this

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

internal fun DynamicBackground?.brush(): Brush? = when (this) {
    is DynamicBackground.LinearGradient -> Brush.linearGradient(colors.mapNotNull { it.color.toColor() })
    else -> null
}

internal fun DynamicBackground?.color(): Color? = when (this) {
    is DynamicBackground.Solid -> color.toColor()
    else -> null
}

private fun Modifier.background(background: DynamicBackground?): Modifier =
    when {
        background?.brush() != null -> background(background.brush()!!)
        background?.color() != null -> background(background.color()!!)
        else -> this
    }

private fun Modifier.border(border: DynamicBorder?, shape: DynamicShape?): Modifier {
    if (border == null) return this
    return border(
        BorderStroke(border.width.dp, border.color.toColor() ?: Color.Transparent),
        shape.toComposeShape(),
    )
}

internal fun DynamicShape?.toComposeShape(): Shape =
    when (this) {
        is DynamicShape.RoundedCorner -> RoundedCornerShape(cornerRadius.dp)
        null -> RoundedCornerShape(0.dp)
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

private inline fun Modifier.thenIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (condition) block() else this
