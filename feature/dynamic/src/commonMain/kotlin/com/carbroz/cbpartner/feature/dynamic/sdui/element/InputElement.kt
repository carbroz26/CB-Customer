package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.InputElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.brush
import com.carbroz.cbpartner.feature.dynamic.runtime.color
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class InputElement : InputElementRenderer {
    @Composable
    override fun render(element: InputElementModel, context: DynamicRenderContext) {
        val binding = element.binding?.key ?: return
        val value = (context.bindings[binding] as? DynamicValue.StringValue)?.value.orEmpty()
        val keyboardType = when (element.keyboardType) {
            "phone" -> androidx.compose.ui.text.input.KeyboardType.Phone
            "number" -> androidx.compose.ui.text.input.KeyboardType.Number
            else -> androidx.compose.ui.text.input.KeyboardType.Text
        }

        fun changed(newValue: String) {
            val limited = newValue.take(element.maxLength ?: Int.MAX_VALUE)
            context.onValueChange(binding, DynamicValue.StringValue(limited))
            element.actions["onValueChange"]?.let(context.onAction)
        }

        Row(
            modifier = Modifier.applyLayout(element),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            context.renderContent(element.leadingContent)
            val presentation = element.presentation
            if (presentation?.type == "segmented") {
                SegmentedInput(
                    value = value,
                    count = requireNotNull(presentation.count) {
                        "Segmented input count is required for element " + element.id
                    },
                    spacing = requireNotNull(presentation.spacing) {
                        "Segmented input spacing is required for element " + element.id
                    }.dp,
                    segmentWidth = requireNotNull(presentation.segmentWidth) {
                        "Segmented input width is required for element " + element.id
                    }.dp,
                    segmentHeight = requireNotNull(presentation.segmentHeight) {
                        "Segmented input height is required for element " + element.id
                    }.dp,
                    background = presentation.background,
                    border = presentation.border,
                    shape = presentation.shape,
                    focusBorder = presentation.focusBorder,
                    textStyle = inputTextStyle(element),
                    onValueChange = ::changed,
                )
            } else {
                BasicTextField(
                    modifier = Modifier
                        .weight((element.weight ?: 1.0).toFloat())
                        .then(if (element.width != null) Modifier.width(element.width.dp) else Modifier)
                        .then(if (element.maxWidth != null) Modifier.widthIn(max = element.maxWidth.dp) else Modifier),
                    value = value,
                    onValueChange = ::changed,
                    textStyle = inputTextStyle(element),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty() && element.placeholder != null) {
                                androidx.compose.material3.Text(
                                    text = element.placeholder,
                                    style = inputTextStyle(element),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
            context.renderContent(element.trailingContent)
        }
    }

    private fun inputTextStyle(element: InputElementModel): TextStyle = TextStyle(
        color = element.textColor?.toColor() ?: Color.Unspecified,
        fontSize = element.fontSize?.sp ?: TextUnit.Unspecified,
        fontWeight = element.fontWeight?.let(::FontWeight),
        textAlign = element.textAlign.toTextAlign(),
    )

    @Composable
    private fun SegmentedInput(
        value: String,
        count: Int,
        spacing: androidx.compose.ui.unit.Dp,
        segmentWidth: androidx.compose.ui.unit.Dp,
        segmentHeight: androidx.compose.ui.unit.Dp,
        background: DynamicBackground?,
        border: DynamicBorder?,
        shape: DynamicShape?,
        focusBorder: DynamicBorder?,
        textStyle: TextStyle,
        onValueChange: (String) -> Unit,
    ) {
        var focused by remember { mutableStateOf(false) }

        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            singleLine = true,
            textStyle = textStyle,
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(count) { index ->
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .height(segmentHeight)
                                .applyBackground(background, shape)
                                .applyInputBorder(if (focused) focusBorder ?: border else border, shape),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Text(
                                text = value.getOrNull(index)?.toString().orEmpty(),
                                style = textStyle,
                                modifier = Modifier.fillMaxWidth(),
                                style = textStyle,
                                textAlign = textStyle.textAlign,
                            )
                        }
                    }
                }
            },
        )
    }
}

private fun Modifier.applyBackground(
    background: DynamicBackground?,
    shape: DynamicShape?,
): Modifier = when {
    background?.brush() != null -> background(background.brush()!!, shape.toComposeShape())
    background?.color() != null -> background(background.color()!!, shape.toComposeShape())
    else -> this
}

private fun Modifier.applyInputBorder(
    border: DynamicBorder?,
    shape: DynamicShape?,
): Modifier = if (border == null) {
    this
} else {
    border(
        width = border.width.dp,
        color = border.color.toColor() ?: Color.Transparent,
        shape = shape.toComposeShape(),
    )
}

private fun DynamicShape?.toComposeShape() = when (this) {
    is DynamicShape.RoundedCorner -> RoundedCornerShape(cornerRadius.dp)
    null -> androidx.compose.ui.graphics.RectangleShape
}

private fun String?.toTextAlign(): TextAlign = when (this) {
    "start" -> TextAlign.Start
    "end" -> TextAlign.End
    "left" -> TextAlign.Left
    "right" -> TextAlign.Right
    "center" -> TextAlign.Center
    else -> TextAlign.Unspecified
}
