package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicPadding
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

        fun changed(newValue: String, segmented: Boolean = false) {
            // Segmented controls encode temporarily empty interior cells as spaces in
            // their ONE binding. Such values fail authored full-match validation.
            val filtered = when (element.inputFilter) {
                "digits" -> newValue.filter { it in '0'..'9' || (segmented && it == ' ') }
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
            context.onFocusChange(binding, state.hasFocus)
            if (state.hasFocus && !hadFocus) {
                element.actions["onFocus"]?.let {
                    reportDynamicOrigin(element.id, "onFocus", it.type)
                    context.onAction(it)
                }
            }
            hadFocus = state.hasFocus
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

        val presentation = element.presentation
        val surfaceModifier = if (presentation?.type == "segmented") {
            // Segmented fields own their per-cell surfaces; keep their existing layout.
            Modifier.applyLayout(element).applyDynamicPadding(element.padding)
        } else {
            val activeBackground =
                if (invalid) element.validationStyle?.background ?: element.background
                else element.background
            val activeBorder = when {
                invalid -> element.border
                focused -> element.focusBorder ?: element.border
                else -> element.border
            }
            // The authored border/background surround accessories and editable text.
            // Padding is internal so tinted number fields have no white outer gutter.
            Modifier.applyLayout(element)
                .applyDynamicBackground(activeBackground, element.shape.toComposeShape())
                .applyBorder(activeBorder, element.shape)
                .applyDynamicPadding(element.padding)
        }

        Row(
            modifier = surfaceModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            context.renderContent(element.leadingContent)
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
                    placeholder = presentation.placeholder,
                    background = if (invalid) {
                        element.validationStyle?.background ?: presentation.background
                    } else {
                        presentation.background
                    },
                    border = presentation.border,
                    invalidBorder = element.validationStyle?.border,
                    shape = presentation.shape,
                    focusBorder = presentation.focusBorder,
                    invalid = invalid,
                    textStyle = inputTextStyle(element),
                    onValueChange = { changed(it, segmented = true) },
                    focusModifier = focusModifier,
                    keyboardActions = keyboardActions,
                    imeAction = submitIme,
                )
            } else {
                var fieldModifier = focusModifier.weight(1f)
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
        lineHeight = element.lineHeight?.sp ?: TextUnit.Unspecified,
        fontWeight = element.fontWeight?.let(::FontWeight),
        textAlign = element.textAlign.toTextAlign(),
    )

    /** Multiple native editing surfaces, but one authored InputElement and one binding. */
    @Composable
    private fun SegmentedInput(
        value: String,
        count: Int,
        spacing: androidx.compose.ui.unit.Dp,
        segmentWidth: androidx.compose.ui.unit.Dp,
        segmentHeight: androidx.compose.ui.unit.Dp,
        placeholder: String?,
        background: DynamicBackground?,
        border: DynamicBorder?,
        invalidBorder: DynamicBorder?,
        shape: DynamicShape?,
        focusBorder: DynamicBorder?,
        invalid: Boolean,
        textStyle: TextStyle,
        onValueChange: (String) -> Unit,
        focusModifier: Modifier,
        keyboardActions: KeyboardActions,
        imeAction: ImeAction,
    ) {
        require(count > 0) { "Segmented input needs at least one slot" }
        val requesters = remember(count) { List(count) { FocusRequester() } }
        var focusedSlot by remember(count) { mutableIntStateOf(-1) }
        var targetFocus by remember(count) { mutableIntStateOf(-1) }
        val slots = List(count) { index ->
            value.getOrNull(index)?.takeUnless { it == ' ' }?.toString().orEmpty()
        }
        val nextEmpty = slots.indexOfFirst(String::isEmpty).let { if (it < 0) count else it }
        LaunchedEffect(value, targetFocus) {
            if (targetFocus in 0 until count &&
                (targetFocus <= nextEmpty || slots[targetFocus].isNotEmpty())
            ) {
                requesters[targetFocus].requestFocus()
                targetFocus = -1
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().then(focusModifier),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(count) { index ->
                val cell = slots[index]
                val enabled = index <= nextEmpty || cell.isNotEmpty()
                val activeBorder = when {
                    invalid -> invalidBorder ?: border
                    focusedSlot == index -> focusBorder ?: border
                    else -> border
                }
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .height(segmentHeight)
                        .applyDynamicBackground(background, shape.toComposeShape())
                        .applyBorder(activeBorder, shape),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(requesters[index])
                            .onFocusChanged { state ->
                                if (state.isFocused) focusedSlot = index
                                else if (focusedSlot == index) focusedSlot = -1
                            }
                            .onPreviewKeyEvent { event ->
                                if (event.key == Key.Backspace && event.type == KeyEventType.KeyDown &&
                                    cell.isEmpty() && index > 0
                                ) {
                                    requesters[index - 1].requestFocus()
                                    true
                                } else false
                            },
                        value = TextFieldValue(cell, selection = TextRange(0, cell.length)),
                        onValueChange = { input ->
                            val update = editSegmentedValue(value, index, input.text, count)
                            onValueChange(update.value)
                            targetFocus = update.nextFocus
                        },
                        enabled = enabled,
                        textStyle = textStyle.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                            imeAction = imeAction,
                        ),
                        keyboardActions = keyboardActions,
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (cell.isEmpty() && placeholder != null) {
                                    androidx.compose.material3.Text(
                                        text = placeholder,
                                        style = textStyle.copy(
                                            color = border?.color?.toColor() ?: Color.Unspecified,
                                            textAlign = TextAlign.Center,
                                        ),
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

}

private fun String?.toTextAlign(): TextAlign = when (this) {
    "start" -> TextAlign.Start
    "end" -> TextAlign.End
    "left" -> TextAlign.Left
    "right" -> TextAlign.Right
    "center" -> TextAlign.Center
    else -> TextAlign.Unspecified
}

/** Slot edits retain interior positions; whitespace represents an unfilled slot. */
internal data class SegmentedEdit(val value: String, val nextFocus: Int)

internal fun editSegmentedValue(
    current: String,
    index: Int,
    incoming: String,
    count: Int,
): SegmentedEdit {
    require(count > 0 && index in 0 until count)
    val cells = MutableList(count) { current.getOrNull(it)?.takeUnless { char -> char == ' ' } }
    if (incoming.isEmpty()) {
        cells[index] = null
        return SegmentedEdit(cells.joinToString("") { it?.toString() ?: " " }.trimEnd(), index)
    }
    // A single key appended to a selected occupied cell replaces that cell;
    // paste distributes multiple digits starting at the selected position.
    val replacement = if (incoming.length == 2 && incoming[0] == cells[index]) {
        incoming.drop(1)
    } else incoming
    val characters = replacement.filter { it in '0'..'9' }
    if (characters.isEmpty()) {
        return SegmentedEdit(current, index)
    }
    characters.forEachIndexed { offset, char ->
        if (index + offset < count) cells[index + offset] = char
    }
    val next = (index + characters.length).coerceAtMost(count - 1)
    return SegmentedEdit(cells.joinToString("") { it?.toString() ?: " " }.trimEnd(), next)
}
