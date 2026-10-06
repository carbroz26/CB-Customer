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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class TextElement : ElementRenderer {
    @Composable override fun render(element: TextElementModel, context: DynamicRenderContext) {
        val color = element.color?.toColor() ?: Color.Unspecified
        val annotated = if (element.spans.isEmpty()) AnnotatedString(element.text) else AnnotatedString.Builder().apply {
            element.spans.forEach { span ->
                val style = SpanStyle(color = span.color?.toColor() ?: color, fontWeight = span.fontWeight?.let(::FontWeight), textDecoration = if (span.underline) androidx.compose.ui.text.style.TextDecoration.Underline else null)
                if (span.action != null) {
                    val link = LinkAnnotation.Clickable(tag = span.text, styles = TextLinkStyles(style = style), linkInteractionListener = { context.onAction(span.action) })
                    val start = length; append(span.text); addLink(link, start, length)
                } else withStyle(style) { append(span.text) }
            }
        }.toAnnotatedString()
        Row(modifier = Modifier.applyLayout(element)) {
            context.renderElements(element.leading)
            Text(text = annotated, fontSize = element.fontSize?.sp ?: TextUnit.Unspecified, fontWeight = element.fontWeight?.let(::FontWeight), color = color,
                textAlign = when (element.textAlign) { "start" -> TextAlign.Start; "end" -> TextAlign.End; "left" -> TextAlign.Left; "right" -> TextAlign.Right; else -> TextAlign.Center },
                letterSpacing = element.letterSpacing?.sp ?: TextUnit.Unspecified, lineHeight = element.lineHeight?.sp ?: TextUnit.Unspecified)
            context.renderElements(element.trailing)
        }
    }
}