package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement as InputElementModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyBorder
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicBackground
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeShape
import com.carbroz.cbpartner.feature.dynamic.sdui.reportDynamicOrigin

class InputElement : ElementRenderer<InputElementModel> {
    @Composable
    override fun render(element: InputElementModel, context: DynamicRenderContext) {
        val binding = element.binding?.key ?: return
        require(element.inputFilter == null || element.inputFilter == "digits") {
            "Unsupported Dynamic input filter: " + element.inputFilter
        }

        val value = (context.bindings[binding] as? DynamicValue.StringValue)?.value.orEmpty()
        val invalid = binding in context.validationErrors
        val focused = context.focusedBinding == binding
        val keyboardType = when (element.keyboardType) {
            "phone" -> androidx.compose.ui.text.input.KeyboardType.Phone
            "number" -> androidx.compose.ui.text.input.KeyboardType.Number
            else -> androidx.compose.ui.text.input.KeyboardType.Text
        }

        fun changed(newValue: String) {
            val filtered = when (element.inputFilter) {
                "digits" -> newValue.filter { it in '0'..'9' }
                null -> newValue
                else -> error("Unsupported Dynamic input filter: " + element.inputFilter)
            }
            val limited = filtered.take(element.maxLength ?: Int.MAX_VALUE)
            context.onValueChange(binding, DynamicValue.StringValue(limited))
            element.actions["onValueChange"]?.let {
                reportDynamicOrigin(element.id, "onValueChange", it.type)
                context.onAction(it)
            }
        }

        var hadFocus by remember(element.id) { mutableStateOf(false) }
        val focusModifier = Modifier.onFocusChanged { state ->
            context.onFocusChange(binding, state.isFocused)
            if (state.isFocused && !hadFocus) {
                element.actions["onFocus"]?.let {
                    reportDynamicOrigin(element.id, "onFocus", it.type)
                    context.onAction(it)
                }
            }
            hadFocus = state.isFocused
        }

        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val submit: () -> Unit = {
            keyboardController?.hide()
            focusManager.clearFocus()
            element.actions["onSubmit"]?.let {
                reportDynamicOrigin(element.id, "onSubmit", it.type)
                context.onAction(it)
            }
        }
        val keyboardActions = KeyboardActions(
            onDone = { submit() },
            onGo = { submit() },
            onSend = { submit() },
        )
        val submitIme =
            if (element.actions.containsKey("onSubmit")) ImeAction.Done else ImeAction.Default

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
                    background = if (invalid) {
                        element.validationStyle?.background ?: presentation.background
                    } else {
                        presentation.background
                    },
                    border = presentation.border,
                    shape = presentation.shape,
                    focusBorder = presentation.focusBorder,
                    focused = focused,
                    invalid = invalid,
                    textStyle = inputTextStyle(element),
                    onValueChange = ::changed,
                    focusModifier = focusModifier,
                    keyboardActions = keyboardActions,
                    imeAction = submitIme,
                )
            } else {
                val activeBackground =
                    if (invalid) element.validationStyle?.background ?: element.background
                    else element.background
                val activeBorder = when {
                    invalid -> element.border
                    focused -> element.focusBorder ?: element.border
                    else -> element.border
                }
                var fieldModifier = focusModifier
                    .weight(1f)
                    .applyDynamicBackground(activeBackground, element.shape.toComposeShape())
                    .applyBorder(activeBorder, element.shape)
                if (element.height != null) {
                    fieldModifier = fieldModifier.fillMaxHeight()
                }

                BasicTextField(
                    modifier = fieldModifier,
                    value = value,
                    onValueChange = ::changed,
                    textStyle = inputTextStyle(element),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = submitIme,
                    ),
                    keyboardActions = keyboardActions,
                    singleLine = element.singleLine ?: true,
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty()) {
                                element.placeholder?.let { placeholder ->
                                    val baseStyle = inputTextStyle(element)
                                    androidx.compose.material3.Text(
                                        text = placeholder,
                                        style = element.placeholderColor?.toColor()?.let { color ->
                                            baseStyle.copy(color = color)
                                        } ?: baseStyle,
                                    )
                                }
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
        focused: Boolean,
        invalid: Boolean,
        textStyle: TextStyle,
        onValueChange: (String) -> Unit,
        focusModifier: Modifier,
        keyboardActions: KeyboardActions,
        imeAction: ImeAction,
    ) {
        val activeBorder = if (invalid) border else if (focused) focusBorder ?: border else border

        BasicTextField(
            modifier = focusModifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                imeAction = imeAction,
            ),
            keyboardActions = keyboardActions,
            singleLine = true,
            textStyle = textStyle,
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        spacing,
                        Alignment.CenterHorizontally,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(count) { index ->
                        Box(
                            modifier = Modifier
                                .width(segmentWidth)
                                .height(segmentHeight)
                                .applyDynamicBackground(
                                    background,
                                    shape.toComposeShape(),
                                )
                                .applyBorder(activeBorder, shape),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Text(
                                text = value.getOrNull(index)?.toString().orEmpty(),
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

private fun String?.toTextAlign(): TextAlign = when (this) {
    "start" -> TextAlign.Start
    "end" -> TextAlign.End
    "left" -> TextAlign.Left
    "right" -> TextAlign.Right
    "center" -> TextAlign.Center
    else -> TextAlign.Unspecified
}
