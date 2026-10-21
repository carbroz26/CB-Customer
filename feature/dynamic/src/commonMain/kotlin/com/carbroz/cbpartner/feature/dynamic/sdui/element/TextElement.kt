package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement as TextElementModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class TextElement : ElementRenderer<TextElementModel> {
    @Composable
    override fun render(element: TextElementModel, context: DynamicRenderContext) {
        val color = element.color?.toColor() ?: Color.Unspecified
        val annotated = if (element.spans.isEmpty()) {
            AnnotatedString(element.text)
        } else {
            AnnotatedString.Builder().apply {
                element.spans.forEach { span ->
                    val style = SpanStyle(
                        color = span.color?.toColor() ?: color,
                        fontWeight = span.fontWeight?.let(::FontWeight),
                        textDecoration = if (span.underline) {
                            androidx.compose.ui.text.style.TextDecoration.Underline
                        } else {
                            null
                        },
                    )
                    val spanText =
                        (context.resolve(span.text) as? DynamicValue.StringValue)?.value.orEmpty()
                    val action = span.action
                    if (action != null) {
                        val link = LinkAnnotation.Clickable(
                            tag = spanText,
                            styles = TextLinkStyles(style = style),
                            linkInteractionListener = {
                                reportDynamicOrigin(element.id, "onSpanClick", action.type)
                                context.onAction(action)
                            },
                        )
                        val start = length
                        append(spanText)
                        addLink(link, start, length)
                    } else {
                        withStyle(style) { append(spanText) }
                    }
                }
            }.toAnnotatedString()
        }

        val overflow = when (element.overflow) {
            null, "clip" -> TextOverflow.Clip
            "ellipsis" -> TextOverflow.Ellipsis
            "visible" -> TextOverflow.Visible
            else -> error("Unsupported Dynamic text overflow: " + element.overflow)
        }

        Row(
            modifier = Modifier.applyLayout(element).let { base ->
                if (element.spans.any { it.action != null }) {
                    base
                } else {
                    base.dynamicGestures(element.actions, context.onAction, element.id)
                }
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            context.renderContent(element.leadingContent)
            Text(
                modifier = if (element.fillMaxWidth) Modifier.weight(1f) else Modifier,
                text = annotated,
                fontSize = element.fontSize?.sp ?: TextUnit.Unspecified,
                fontWeight = element.fontWeight?.let(::FontWeight),
                color = color,
                textAlign = when (element.textAlign) {
                    "start" -> TextAlign.Start
                    "end" -> TextAlign.End
                    "left" -> TextAlign.Left
                    "right" -> TextAlign.Right
                    "center" -> TextAlign.Center
                    else -> TextAlign.Unspecified
                },
                letterSpacing = element.letterSpacing?.sp ?: TextUnit.Unspecified,
                lineHeight = element.lineHeight?.sp ?: TextUnit.Unspecified,
                maxLines = element.maxLines ?: Int.MAX_VALUE,
                overflow = overflow,
            )
            context.renderContent(element.trailingContent)
        }
    }
}
