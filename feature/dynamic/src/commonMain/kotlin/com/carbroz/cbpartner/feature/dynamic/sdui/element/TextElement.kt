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
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class TextElement : ElementRenderer {
    @Composable override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val model = element as? TextElementModel ?: return
        val color = model.color?.toColor() ?: Color.Unspecified
        val annotated = if (model.spans.isEmpty()) AnnotatedString(model.text) else AnnotatedString.Builder().apply {
            model.spans.forEach { span ->
                val style = SpanStyle(
                    color = span.color?.toColor() ?: color,
                    fontWeight = span.fontWeight?.let(::FontWeight),
                    textDecoration = if (span.underline) androidx.compose.ui.text.style.TextDecoration.Underline else null
                )
                val spanText = (context.resolve(span.text) as? DynamicValue.StringValue)?.value.orEmpty()
                val action = span.action
                if (action != null) {
                    val link = LinkAnnotation.Clickable(
                        tag = spanText,
                        styles = TextLinkStyles(style = style),
                        linkInteractionListener = { context.onAction(action) }
                    )
                    val start = length
                    append(spanText)
                    addLink(link, start, length)
                } else {
                    withStyle(style) { append(span.text) }
                }
            }
        }.toAnnotatedString()

        Row(modifier = Modifier.applyLayout(model)) {
            context.renderContent(model.leadingContent)
            Text(
                text = annotated,
                fontSize = model.fontSize?.sp ?: TextUnit.Unspecified,
                fontWeight = model.fontWeight?.let(::FontWeight),
                color = color,
                textAlign = when (model.textAlign) {
                    "start" -> TextAlign.Start
                    "end" -> TextAlign.End
                    "left" -> TextAlign.Left
                    "right" -> TextAlign.Right
                    else -> TextAlign.Center
                },
                letterSpacing = model.letterSpacing?.sp ?: TextUnit.Unspecified,
                lineHeight = model.lineHeight?.sp ?: TextUnit.Unspecified
            )
            context.renderContent(model.trailingContent)
        }
    }
}
