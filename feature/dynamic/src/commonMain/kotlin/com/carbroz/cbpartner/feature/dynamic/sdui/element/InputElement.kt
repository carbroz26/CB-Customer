package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout

class InputElement : ElementRenderer {
    @Composable override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val model = element as? InputElementModel ?: return
        val binding = model.binding?.key ?: return
        val value = context.bindings[binding].orEmpty()
        val keyboardType = when (model.keyboardType) {
            "phone" -> KeyboardType.Phone
            "number" -> KeyboardType.Number
            else -> KeyboardType.Text
        }

        fun changed(newValue: String) {
            val limited = newValue.take(model.maxLength ?: Int.MAX_VALUE)
            context.onValueChange(binding, limited)
            model.actions["onValueChange"]?.let(context.onAction)
        }

        val presentation = model.presentation
        if (presentation?.type == "segmented") {
            SegmentedInput(
                value,
                presentation.count ?: model.maxLength ?: 1,
                presentation.spacing?.dp ?: 8.dp,
                presentation.segmentWidth?.dp ?: 44.dp,
                presentation.segmentHeight?.dp ?: 52.dp,
                ::changed
            )
        } else {
            OutlinedTextField(
                modifier = Modifier.applyLayout(model),
                value = value,
                onValueChange = ::changed,
                placeholder = model.placeholder?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true
            )
        }
    }

    @Composable
    private fun SegmentedInput(
        value: String,
        count: Int,
        spacing: androidx.compose.ui.unit.Dp,
        segmentWidth: androidx.compose.ui.unit.Dp,
        segmentHeight: androidx.compose.ui.unit.Dp,
        onValueChange: (String) -> Unit
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(count) { index ->
                        if (index > 0) Spacer(Modifier.padding(horizontal = spacing / 2))
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .height(segmentHeight)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFCCE0E3),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(value.getOrNull(index)?.toString().orEmpty(), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        )
    }
}
