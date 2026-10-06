package com.carbroz.cbpartner.domain.model.dynamic

import com.carbroz.cbpartner.domain.model.DynamicDestination

data class DynamicResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: DynamicScreen?,
    val traceId: String?,
    val dataPayload: DynamicValue? = null,
    val destination: DynamicDestination? = null,
)

data class DynamicScreen(
    val screenId: String,
    val schemaVersion: String,
    val targetApp: String,
    val template: FormTemplate,
    val theme: DynamicTheme?,
)

data class FormTemplate(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxSize: Boolean = false,
    val semanticRole: String? = null,
    val components: List<StackComponent> = emptyList(),
)

data class StackComponent(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalArrangement: DynamicArrangement = DynamicArrangement.Start,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val verticalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxWidth: Boolean = false,
    val elements: List<DynamicElement> = emptyList(),
    val sections: List<StackSection> = emptyList(),
)

data class StackSection(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val fillMaxWidth: Boolean = false,
    val elements: List<DynamicElement> = emptyList(),
    val groups: List<StackGroup> = emptyList(),
)

data class StackGroup(
    val id: String,
    val orientation: String = "vertical",
    val verticalArrangement: DynamicArrangement = DynamicArrangement.Top,
    val horizontalArrangement: DynamicArrangement = DynamicArrangement.Start,
    val horizontalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val verticalAlignment: DynamicArrangement = DynamicArrangement.Center,
    val padding: DynamicPadding? = null,
    val height: Double? = null,
    val fillMaxWidth: Boolean = false,
    val background: DynamicBackground? = null,
    val border: DynamicBorder? = null,
    val shape: DynamicShape? = null,
    val elements: List<DynamicElement> = emptyList(),
)

sealed interface DynamicElement {
    val id: String
    val semanticRole: String?
    val actions: Map<String, DynamicAction>
}

data class TextElement(
    override val id: String,
    override val semanticRole: String? = null,
    val text: String = "",
    val spans: List<DynamicTextSpan> = emptyList(),
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val letterSpacing: Double? = null,
    val lineHeight: Double? = null,
    val color: String? = null,
    val textAlign: String = "center",
    val fillMaxWidth: Boolean = false,
    val leading: List<DynamicElement> = emptyList(),
    val trailing: List<DynamicElement> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement

data class ImageElement(
    override val id: String,
    override val semanticRole: String? = null,
    val url: String = "",
    val width: Double? = null,
    val height: Double? = null,
    val maxWidth: Double? = null,
    val fillMaxWidth: Boolean = false,
    val contentScale: String = "fit",
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement

data class InputElement(
    override val id: String,
    override val semanticRole: String? = null,
    val placeholder: String? = null,
    val keyboardType: String = "text",
    val maxLength: Int? = null,
    val weight: Double? = null,
    val validation: DynamicValidation? = null,
    val binding: DynamicBinding? = null,
    val presentation: DynamicInputPresentation? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement

data class ButtonElement(
    override val id: String,
    override val semanticRole: String? = null,
    val text: String = "",
    val height: Double? = null,
    val fillMaxWidth: Boolean = false,
    val fontSize: Double? = null,
    val fontWeight: Int? = null,
    val textColor: String? = null,
    val background: DynamicBackground? = null,
    val shape: DynamicShape? = null,
    val leading: List<DynamicElement> = emptyList(),
    val trailing: List<DynamicElement> = emptyList(),
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement

data class IconElement(
    override val id: String,
    override val semanticRole: String? = null,
    val name: String,
    val size: Double? = null,
    val color: String? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement

data class DividerElement(
    override val id: String,
    override val semanticRole: String? = null,
    val orientation: String = "horizontal",
    val width: Double? = null,
    val height: Double? = null,
    val thickness: Double = 1.0,
    val color: String? = null,
    override val actions: Map<String, DynamicAction> = emptyMap(),
) : DynamicElement

data class DynamicValidation(
    val required: Boolean?,
    val pattern: String?,
    val message: String?,
)

data class DynamicBinding(
    val key: String,
)

data class DynamicAction(
    val type: String,
    val payload: Map<String, DynamicValue> = emptyMap(),
    val destination: DynamicDestination? = null,
)

data class DynamicTheme(
    val theme: String?,
    val statusBar: String?,
    val properties: Map<String, DynamicValue>,
)

sealed interface DynamicValue {
    data class StringValue(val value: String) : DynamicValue
    data class NumberValue(val value: Double) : DynamicValue
    data class BooleanValue(val value: Boolean) : DynamicValue
    data class ObjectValue(val value: Map<String, DynamicValue>) : DynamicValue
    data class ArrayValue(val value: List<DynamicValue>) : DynamicValue
    data object NullValue : DynamicValue
}
