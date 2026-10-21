package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement as ButtonElementModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicLoadingIndicator
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicBackground
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeShape
import com.carbroz.cbpartner.feature.dynamic.sdui.dynamicGestures

/** Button loading is keyed by its originating Button ID in the active render scope. */
class ButtonElement : ElementRenderer<ButtonElementModel> {
    @Composable
    override fun render(element: ButtonElementModel, context: DynamicRenderContext) {
        val pending = element.loading != null && context.pendingButtonId == element.id
        val modifier = Modifier.applyLayout(element)
            .applyDynamicBackground(element.background, element.shape.toComposeShape())
        Box(
            modifier = modifier
                .then(if (pending) Modifier.semantics { disabled() } else Modifier)
                .then(if (pending) Modifier else Modifier.dynamicGestures(
                    actions = element.actions,
                    dispatch = { action ->
                        if (element.loading != null) {
                            context.onButtonAction(action, element.id)
                        } else {
                            context.onAction(action)
                        }
                    },
                    elementId = element.id,
                )),
            contentAlignment = Alignment.Center,
        ) {
            if (pending) {
                when (val indicator = element.loading!!.indicator) {
                    is DynamicLoadingIndicator.Circular -> CircularProgressIndicator(
                        modifier = Modifier.size(indicator.size.dp),
                        color = requireNotNull(indicator.color.toColor()) {
                            "Invalid dynamic loading indicator color"
                        },
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    context.renderContent(element.leadingContent)
                    Text(
                        text = element.text,
                        color = element.textColor?.toColor() ?: Color.Unspecified,
                        fontSize = element.fontSize?.sp ?: TextUnit.Unspecified,
                        fontWeight = element.fontWeight?.let(::FontWeight),
                    )
                    context.renderContent(element.trailingContent)
                }
            }
        }
    }
}
