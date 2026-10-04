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
    val template: DynamicTemplate,
    val theme: DynamicTheme?,
)

data class DynamicTemplate(
    val id: String,
    val type: String,
    val properties: Map<String, DynamicValue>,
    val components: List<DynamicComponent>,
)

data class DynamicComponent(
    val id: String,
    val type: String,
    val properties: Map<String, DynamicValue>,
    val elements: List<DynamicElement>,
    val sections: List<DynamicSection>,
)

data class DynamicSection(
    val id: String,
    val type: String,
    val properties: Map<String, DynamicValue>,
    val elements: List<DynamicElement>,
    val groups: List<DynamicGroup>,
)

data class DynamicGroup(
    val id: String,
    val type: String,
    val properties: Map<String, DynamicValue>,
    val elements: List<DynamicElement>,
)

data class DynamicElement(
    val id: String,
    val type: String,
    val properties: Map<String, DynamicValue>,
    val validation: DynamicValidation?,
    val binding: DynamicBinding?,
    val actions: Map<String, DynamicAction>,
)

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
