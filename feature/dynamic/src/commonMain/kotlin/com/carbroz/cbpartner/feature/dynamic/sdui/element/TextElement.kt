package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.unit.TextUnit
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
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
        val annotated = if (spans == null) AnnotatedString(text) else buildAnnotatedString(spans, properties.color("color"), context)
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
            val element = DynamicElement("decoration", type, properties.value, null, null, emptyMap())
            context.registry.element(type)?.render(element, context)
        }
    }

    private fun buildAnnotatedString(
        value: DynamicValue.ArrayValue,
        baseColor: Color?,
        context: DynamicRenderContext,
    ): AnnotatedString = AnnotatedString.Builder().apply {
        value.value.forEach { item ->
            val objectValue = item as? DynamicValue.ObjectValue ?: return@forEach
            val text = (objectValue.value["text"] as? DynamicValue.StringValue)?.value ?: return@forEach
            val color = (objectValue.value["color"] as? DynamicValue.StringValue)?.value?.toColor() ?: baseColor ?: Color.Unspecified
            val fontWeight = (objectValue.value["fontWeight"] as? DynamicValue.NumberValue)?.value?.toInt()?.let(::FontWeight)
            val underline = (objectValue.value["underline"] as? DynamicValue.BooleanValue)?.value == true
            val style = SpanStyle(color = color, fontWeight = fontWeight, textDecoration = if (underline) androidx.compose.ui.text.style.TextDecoration.Underline else null)
            val click = objectValue.value["onClick"] as? DynamicValue.ObjectValue
            val action = click?.toAction()
            if (action != null) {
                val link = LinkAnnotation.Clickable(
                    tag = text,
                    styles = TextLinkStyles(style = style),
                    linkInteractionListener = { context.onAction(action) },
                )
                withLink(link) { append(text) }
            } else {
                withStyle(style) { append(text) }
            }
        }
    }.toAnnotatedString()

    private fun DynamicValue.ObjectValue.toAction(): DynamicAction? {
        val type = (value["type"] as? DynamicValue.StringValue)?.value ?: return null
        val payload = (value["payload"] as? DynamicValue.ObjectValue)?.value ?: emptyMap()
        return DynamicAction(type = type, payload = payload)
    }
}
