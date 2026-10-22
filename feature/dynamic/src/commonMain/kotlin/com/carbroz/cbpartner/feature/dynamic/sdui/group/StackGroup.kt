package com.carbroz.cbpartner.feature.dynamic.sdui.group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup as StackGroupModel
import com.carbroz.cbpartner.feature.dynamic.registry.GroupRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeWeight
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class StackGroup : GroupRenderer<StackGroupModel> {
    @Composable
    override fun render(group: StackGroupModel, context: DynamicRenderContext) {
        val selectedBackground = group.selectedStyle?.background
        val displayedGroup = if (context.rowSelected == true && selectedBackground != null) {
            group.copy(background = selectedBackground)
        } else {
            group
        }
        val validation = group.validationPresentation
        val validationMessage = validation?.binding?.let(context.validationErrors::get)
        val focused = group.focusPresentation?.binding == context.focusedBinding
        val activeBorder = when {
            validationMessage != null -> validation.errorBorder
            focused -> group.focusPresentation?.border
            else -> group.border
        }

        Column(
            modifier = if (group.fillMaxWidth) Modifier.fillMaxWidth() else Modifier,
        ) {
            if (group.orientation == "horizontal") {
                Row(
                    modifier = Modifier.applyLayout(displayedGroup, activeBorder),
                    horizontalArrangement = group.horizontalArrangement.toHorizontalArrangement(),
                    verticalAlignment = group.verticalAlignment.toVerticalAlignment(),
                ) {
                    group.elements.forEach { element ->
                        val weight = element.weight
                        if (weight == null) {
                            context.renderElement(element)
                        } else {
                            Box(
                                modifier = Modifier.weight(weight.toComposeWeight()),
                                propagateMinConstraints = true,
                            ) {
                                context.renderElement(element)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.applyLayout(group, activeBorder),
                    verticalArrangement = group.verticalArrangement.toVerticalArrangement(),
                    horizontalAlignment = group.horizontalAlignment.toHorizontalAlignment(),
                ) {
                    group.elements.forEach { element ->
                        val weight = element.weight
                        if (weight == null) {
                            context.renderElement(element)
                        } else {
                            Box(
                                modifier = Modifier.weight(weight.toComposeWeight()),
                                propagateMinConstraints = true,
                            ) {
                                context.renderElement(element)
                            }
                        }
                    }
                }
            }

            if (validationMessage != null) {
                val style = validation.messageStyle
                Text(
                    text = validationMessage,
                    modifier = Modifier.applyDynamicSpacing(style?.spacing),
                    color = style?.color?.toColor() ?: Color.Unspecified,
                    fontSize = style?.fontSize?.sp ?: TextUnit.Unspecified,
                    fontWeight = style?.fontWeight?.let(::FontWeight),
                )
            }
        }
    }
}
