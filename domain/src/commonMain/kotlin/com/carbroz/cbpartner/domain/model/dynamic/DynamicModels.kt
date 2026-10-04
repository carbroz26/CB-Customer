package com.carbroz.cbpartner.domain.model.dynamic

data class DynamicDestination(
    val screenId: String,
    val templateId: String?,
    val templateType: String?,
    val endpoint: String,
    val method: String = "GET",
    val authentication: String? = null,
)

data class DynamicResponse(
    val status: Int,
    val code: String,
    val message: String,
    val data: DynamicScreen?,
    val traceId: String?,
    val nextScreen: DynamicNextScreen? = null,
    val dataPayload: DynamicValue? = null,
)

data class DynamicNextScreen(
    val screenId: String,
    val templateId: String,
    val templateType: String,
    val endpoint: String,
    val method: String = "GET",
    val authentication: String? = null,
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
    val payload: Map<String, DynamicValue>,
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
