package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.color
import com.carbroz.cbpartner.feature.dynamic.runtime.number
import com.carbroz.cbpartner.feature.dynamic.runtime.string
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class TextElement : ElementRenderer {
    @Composable
    override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val properties = element.properties
        val text = properties.string("text") ?: ""
        val spans = properties["spans"] as? DynamicValue.ArrayValue
        val annotated = if (spans == null) AnnotatedString(text) else buildAnnotatedString(spans, properties.color("color"))
        val fontSize = properties.number("fontSize")?.sp
        val weight = properties.number("fontWeight")?.toInt()?.let(::FontWeight)
        val color = properties.color("color") ?: Color.Unspecified
        val textAlign = when (properties.string("textAlign")) {
            "start" -> TextAlign.Start
            "end" -> TextAlign.End
            "left" -> TextAlign.Left
            "right" -> TextAlign.Right
            else -> TextAlign.Center
        }

        Row(modifier = context.applyLayout(Modifier, properties)) {
            renderDecorations(properties["leading"], context)
            Text(
                text = annotated,
                fontSize = fontSize ?: TextUnit.Unspecified,
                fontWeight = weight,
                color = color,
                textAlign = textAlign,
                letterSpacing = properties.number("letterSpacing")?.sp ?: TextUnit.Unspecified,
                lineHeight = properties.number("lineHeight")?.sp ?: TextUnit.Unspecified,
            )
            renderDecorations(properties["trailing"], context)
        }
    }

    @Composable
    private fun renderDecorations(value: DynamicValue?, context: DynamicRenderContext) {
        val decorations = value as? DynamicValue.ArrayValue ?: return
        decorations.value.forEach { item ->
            val definition = item as? DynamicValue.ObjectValue ?: return@forEach
            val type = (definition.value["type"] as? DynamicValue.StringValue)?.value ?: return@forEach
            val properties = definition.value["properties"] as? DynamicValue.ObjectValue ?: return@forEach
            val element = DynamicElement(
                id = "decoration",
                type = type,
                properties = properties.value,
                validation = null,
                binding = null,
                actions = emptyMap(),
            )
            context.registry.element(type)?.render(element, context)
        }
    }

    private fun buildAnnotatedString(value: DynamicValue.ArrayValue, baseColor: Color?): AnnotatedString =
        AnnotatedString.Builder().apply {
            value.value.forEach { item ->
                val objectValue = item as? DynamicValue.ObjectValue ?: return@forEach
                val text = (objectValue.value["text"] as? DynamicValue.StringValue)?.value ?: return@forEach
                val style = SpanStyle(
                    color = (objectValue.value["color"] as? DynamicValue.StringValue)?.value?.toColor() ?: baseColor ?: Color.Unspecified,
                    fontWeight = (objectValue.value["fontWeight"] as? DynamicValue.NumberValue)?.value?.toInt()?.let(::FontWeight),
                    textDecoration = if ((objectValue.value["underline"] as? DynamicValue.BooleanValue)?.value == true) {
                        androidx.compose.ui.text.style.TextDecoration.Underline
                    } else null,
                )
                withStyle(style) { append(text) }
            }
        }.toAnnotatedString()
}
