package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusGroup
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import com.carbroz.cbpartner.feature.dynamic.runtime.SegmentedInputSlots
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
            val filtered = if (element.presentation?.type == "segmented") {
                // Slots are held in the ONE existing String binding. An underscore denotes
                // a missing slot, preserving positional deletion without shifting later digits.
                require(newValue.all { it == '_' || it in '0'..'9' }) {
                    "Segmented input must contain ASCII digits or empty-slot sentinels"
                }
                newValue
            } else when (element.inputFilter) {
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
            val active = if (element.presentation?.type == "segmented") state.hasFocus else state.isFocused
            context.onFocusChange(binding, active)
            if (active && !hadFocus) {
                element.actions["onFocus"]?.let {
                    reportDynamicOrigin(element.id, "onFocus", it.type)
                    context.onAction(it)
                }
            }
            hadFocus = active
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
                    elementId = element.id,
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
                    border = if (invalid) {
                        element.validationStyle?.border ?: presentation.border
                    } else {
                        presentation.border
                    },
                    placeholder = presentation.placeholder,
                    placeholderColor = element.placeholderColor?.toColor(),
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
        elementId: String,
        count: Int,
        spacing: androidx.compose.ui.unit.Dp,
        segmentWidth: androidx.compose.ui.unit.Dp,
        segmentHeight: androidx.compose.ui.unit.Dp,
        background: DynamicBackground?,
        border: DynamicBorder?,
        placeholder: String?,
        placeholderColor: Color?,
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
        val focusRequesters = remember(elementId, count) { List(count) { FocusRequester() } }
        var focusedIndex by remember(elementId) { mutableStateOf(-1) }
        // The presentation holds only focus/selection; digits and holes remain in
        // the Screen's single retained binding, not in a second UI Store.
        Row(
            modifier = focusModifier.focusGroup().fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(count) { index ->
                val cell = SegmentedInputSlots.display(value, count, index)
                var field by remember(elementId, index) {
                    mutableStateOf(TextFieldValue(cell))
                }
                LaunchedEffect(cell, focusedIndex) {
                    if (field.text != cell || focusedIndex == index) {
                        field = TextFieldValue(
                            text = cell,
                            selection = TextRange(0, cell.length),
                        )
                    }
                }
                val cellBorder = when {
                    invalid -> border
                    focused && focusedIndex == index -> focusBorder ?: border
                    else -> border
                }
                BasicTextField(
                    modifier = Modifier
                        .width(segmentWidth)
                        .height(segmentHeight)
                        .focusRequester(focusRequesters[index])
                        .onFocusChanged { state ->
                            if (state.isFocused) focusedIndex = index
                        }
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown &&
                                event.key == Key.Backspace && cell.isEmpty()
                            ) {
                                val update = SegmentedInputSlots.backspaceOnEmpty(value, count, index)
                                focusRequesters[update.focusIndex].requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                        .applyDynamicBackground(background, shape.toComposeShape())
                        .applyBorder(cellBorder, shape),
                    value = field,
                    onValueChange = { next ->
                        field = next
                        if (next.text != cell) {
                            val edited = SegmentedInputSlots.edit(
                                value, count, index, next.text,
                            )
                            onValueChange(edited.value)
                            focusRequesters[edited.focusIndex].requestFocus()
                        }
                    },
                    textStyle = textStyle,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = imeAction,
                    ),
                    keyboardActions = keyboardActions,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth().height(segmentHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (cell.isEmpty() && focusedIndex != index) {
                                androidx.compose.material3.Text(
                                    text = placeholder.orEmpty(),
                                    style = if (placeholderColor != null) {
                                        textStyle.copy(color = placeholderColor)
                                    } else {
                                        textStyle
                                    },
                                    textAlign = TextAlign.Center,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        }
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
