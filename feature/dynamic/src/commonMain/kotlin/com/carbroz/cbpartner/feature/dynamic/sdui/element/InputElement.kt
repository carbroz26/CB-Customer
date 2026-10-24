package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.alpha
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
            focusManager.clearFocus()
            keyboardController?.hide()
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
                    keyboardType = keyboardType,
                    inputFilter = element.inputFilter,
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
                    imeAction = ImeAction.Done,
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

    /**
     * One native IME connection with independently selectable painted cells.
     * Slot changes update selection, never native focus; dismissals do not refocus.
     */
    @Composable
    private fun SegmentedInput(
        value: String,
        count: Int,
        spacing: androidx.compose.ui.unit.Dp,
        segmentWidth: androidx.compose.ui.unit.Dp,
        segmentHeight: androidx.compose.ui.unit.Dp,
        placeholder: String?,
        keyboardType: androidx.compose.ui.text.input.KeyboardType,
        inputFilter: String?,
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
        val focusRequester = remember { FocusRequester() }
        var selectedSlot by remember(count) { mutableIntStateOf(0) }
        var hasFocus by remember { mutableStateOf(false) }
        val padded = value.padEnd(count, ' ').take(count)
        val firstEmpty = padded.indexOf(' ').let { if (it < 0) count else it }
        val selected = selectedSlot.coerceIn(0, count - 1)
        val fieldValue = TextFieldValue(padded, selection = TextRange(selected, selected + 1))

        BasicTextField(
            modifier = focusModifier.fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { hasFocus = it.isFocused },
            value = fieldValue,
            onValueChange = { proposed ->
                if (proposed.text != padded) {
                    val edited = editSegmentedInput(
                        current = value,
                        index = selected,
                        editedText = proposed.text,
                        count = count,
                        inputFilter = inputFilter,
                    )
                    selectedSlot = edited.nextFocus
                    if (edited.value != value) onValueChange(edited.value)
                } else if (proposed.selection.start != fieldValue.selection.start) {
                    val target = proposed.selection.start.coerceIn(0, count - 1)
                    if (target <= firstEmpty || padded[target] != ' ') selectedSlot = target
                }
            },
            textStyle = textStyle.copy(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(count) { index ->
                            val digit = padded[index].takeUnless { it == ' ' }?.toString().orEmpty()
                            val editable = index <= firstEmpty || digit.isNotEmpty()
                            val active = hasFocus && selected == index
                            val activeBorder = when {
                                invalid -> invalidBorder ?: border
                                active -> focusBorder ?: border
                                else -> border
                            }
                            Box(
                                modifier = Modifier.width(segmentWidth).height(segmentHeight)
                                    .applyDynamicBackground(background, shape.toComposeShape())
                                    .applyBorder(activeBorder, shape)
                                    .clickable(enabled = editable) {
                                        selectedSlot = index
                                        focusRequester.requestFocus()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                // The selected empty slot displays the caret instead of its placeholder.
                                val shown = if (active && digit.isEmpty()) "│"
                                    else digit.ifEmpty { placeholder.orEmpty() }
                                androidx.compose.material3.Text(
                                    text = shown,
                                    style = textStyle.copy(
                                        color = if (digit.isEmpty()) {
                                            (if (active) focusBorder else border)?.color?.toColor()
                                                ?: Color.Unspecified
                                        } else textStyle.color,
                                        textAlign = TextAlign.Center,
                                    ),
                                )
                            }
                        }
                    }
                    // The inner field is kept alive exactly once for IME/accessibility.
                    // Its text/cursor are painted instead in the authored cell surfaces.
                    Box(modifier = Modifier.width(1.dp).height(1.dp).alpha(0f)) {
                        innerTextField()
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

/** Slot edits retain interior positions; whitespace represents an unfilled slot. */
internal data class SegmentedEdit(val value: String, val nextFocus: Int)

internal fun editSegmentedValue(
    current: String,
    index: Int,
    incoming: String,
    count: Int,
    inputFilter: String? = null,
): SegmentedEdit {
    require(count > 0 && index in 0 until count)
    val cells = MutableList(count) { current.getOrNull(it)?.takeUnless { char -> char == ' ' } }
    if (incoming.isEmpty()) {
        cells[index] = null
        return SegmentedEdit(cells.joinToString("") { it?.toString() ?: " " }.trimEnd(), index)
    }
    // The focused native cell selects its character, so new typing replaces it.
    // Paste of any length overwrites slots sequentially, including two-character paste.
    val characters = when (inputFilter) {
        "digits" -> incoming.filter { it in '0'..'9' }
        null -> incoming.filter { it != ' ' }
        else -> error("Unsupported segmented input filter: " + inputFilter)
    }
    if (characters.isEmpty()) {
        return SegmentedEdit(current, index)
    }
    characters.forEachIndexed { offset, char ->
        if (index + offset < count) cells[index + offset] = char
    }
    val next = (index + characters.length).coerceAtMost(count - 1)
    return SegmentedEdit(cells.joinToString("") { it?.toString() ?: " " }.trimEnd(), next)
}

/** Decode the native replacement of one selected slot without moving native focus. */
internal fun editSegmentedInput(
    current: String,
    index: Int,
    editedText: String,
    count: Int,
    inputFilter: String? = null,
): SegmentedEdit {
    require(count > 0 && index in 0 until count)
    val padded = current.padEnd(count, ' ').take(count)
    val prefix = padded.substring(0, index)
    val suffix = padded.substring(index + 1)
    if (!editedText.startsWith(prefix) || !editedText.endsWith(suffix) ||
        editedText.length < prefix.length + suffix.length
    ) return SegmentedEdit(current, index)
    val incoming = editedText.substring(prefix.length, editedText.length - suffix.length)
    if (incoming.isEmpty() && padded[index] == ' ') {
        // Backspace over an empty slot moves selection to the previous cell.
        return SegmentedEdit(current, (index - 1).coerceAtLeast(0))
    }
    return editSegmentedValue(current, index, incoming, count, inputFilter)
}
