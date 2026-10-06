package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.carbroz.cbpartner.domain.model.dynamic.InputElement as InputElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout

class InputElement : ElementRenderer {
    @Composable
    override fun render(element: InputElementModel, context: DynamicRenderContext) {
        val binding = element.binding?.key ?: return
        val value = context.bindings[binding].orEmpty()
        val keyboardType = when (element.keyboardType) {
            "phone" -> KeyboardType.Phone
            "number" -> KeyboardType.Number
            else -> KeyboardType.Text
        }

        fun changed(newValue: String) {
            val limited = newValue.take(element.maxLength ?: Int.MAX_VALUE)
            context.onValueChange(binding, limited)
            element.actions["onValueChange"]?.let(context.onAction)
        }

        if (element.presentation?.type == "segmented") {
            SegmentedInput(
                value = value,
                count = element.presentation.count ?: element.maxLength ?: 1,
                spacing = element.presentation.spacing?.dp ?: 8.dp,
                segmentWidth = element.presentation.segmentWidth?.dp ?: 44.dp,
                segmentHeight = element.presentation.segmentHeight?.dp ?: 52.dp,
                onValueChange = ::changed,
            )
        } else {
            OutlinedTextField(
                modifier = Modifier.applyLayout(element),
                value = value,
                onValueChange = ::changed,
                placeholder = element.placeholder?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
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
        onValueChange: (String) -> Unit,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent),
            decorationBox = {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    repeat(count) { index ->
                        if (index > 0) Spacer(Modifier.padding(horizontal = spacing / 2))
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .height(segmentHeight)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Color(0xFFCCE0E3)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(value.getOrNull(index)?.toString().orEmpty(), textAlign = TextAlign.Center)
                        }
                    }
                }
            },
        )
    }
}
