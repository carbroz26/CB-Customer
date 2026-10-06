package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicBindingDto
import com.carbroz.cbpartner.data.dynamic.model.element.*
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.element.*
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class DynamicElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val contentMapper = ElementContentMapper(json, values)

    fun map(element: JsonObject): DynamicElement = when (values.string(element, "type")) {
        "text" -> json.decodeFromJsonElement(TextElementDto.serializer(), element).let(::mapText)
        "image" -> json.decodeFromJsonElement(ImageElementDto.serializer(), element).let(::mapImage)
        "input" -> json.decodeFromJsonElement(InputElementDto.serializer(), element).let(::mapInput)
        "button" -> json.decodeFromJsonElement(ButtonElementDto.serializer(), element).let(::mapButton)
        "icon" -> json.decodeFromJsonElement(IconElementDto.serializer(), element).let(::mapIcon)
        "divider" -> json.decodeFromJsonElement(DividerElementDto.serializer(), element).let(::mapDivider)
        else -> error("Unsupported dynamic element type: " + values.string(element, "type"))
    }

    private fun mapText(dto: TextElementDto): TextElement {
        val textValue = dto.properties["text"]
        val spans = values.spans(dto.properties).let { mappedSpans ->
            if (textValue is kotlinx.serialization.json.JsonObject) {
                listOf(com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan(text = values.toDynamicValue(textValue))) + mappedSpans
            } else {
                mappedSpans
            }
        }
        return TextElement(
        id = dto.id,
        semanticRole = values.string(dto.properties, "semanticRole"),
        text = values.string(dto.properties, "text").orEmpty(),
        spans = spans,
        fontSize = values.number(dto.properties, "fontSize"),
        fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
        letterSpacing = values.number(dto.properties, "letterSpacing"),
        lineHeight = values.number(dto.properties, "lineHeight"),
        color = values.string(dto.properties, "color"),
        textAlign = values.string(dto.properties, "textAlign") ?: "center",
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = mapActions(dto.actions),
        )
    }

    private fun mapImage(dto: ImageElementDto) = ImageElement(
        id = dto.id,
        semanticRole = values.string(dto.properties, "semanticRole"),
        url = values.string(dto.properties, "url").orEmpty(),
        width = values.number(dto.properties, "width"),
        height = values.number(dto.properties, "height"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        contentScale = values.string(dto.properties, "contentScale") ?: "fit",
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = mapActions(dto.actions),
    )

    private fun mapInput(dto: InputElementDto) = InputElement(
        id = dto.id,
        semanticRole = values.string(dto.properties, "semanticRole"),
        placeholder = values.string(dto.properties, "placeholder"),
        keyboardType = values.string(dto.properties, "keyboardType") ?: "text",
        maxLength = values.number(dto.properties, "maxLength")?.toInt(),
        weight = values.number(dto.properties, "weight"),
        validation = dto.validation?.let { DynamicValidation(it.required, it.pattern, it.message) },
        binding = dto.binding?.let(::mapBinding),
        presentation = values.inputPresentation(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = mapActions(dto.actions),
    )

    private fun mapButton(dto: ButtonElementDto) = ButtonElement(
        id = dto.id,
        semanticRole = values.string(dto.properties, "semanticRole"),
        text = values.string(dto.properties, "text").orEmpty(),
        height = values.number(dto.properties, "height"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        fontSize = values.number(dto.properties, "fontSize"),
        fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
        textColor = values.string(dto.properties, "textColor"),
        background = values.background(dto.properties),
        shape = values.shape(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = mapActions(dto.actions),
    )

    private fun mapIcon(dto: IconElementDto) = IconElement(
        id = dto.id,
        semanticRole = values.string(dto.properties, "semanticRole"),
        name = values.string(dto.properties, "name").orEmpty(),
        size = values.number(dto.properties, "size"),
        color = values.string(dto.properties, "color"),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = mapActions(dto.actions),
    )

    private fun mapDivider(dto: DividerElementDto) = DividerElement(
        id = dto.id,
        semanticRole = values.string(dto.properties, "semanticRole"),
        orientation = values.string(dto.properties, "orientation") ?: "horizontal",
        width = values.number(dto.properties, "width"),
        height = values.number(dto.properties, "height"),
        thickness = values.number(dto.properties, "thickness") ?: 1.0,
        color = values.string(dto.properties, "color"),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = mapActions(dto.actions),
    )

    private fun mapActions(dto: DynamicActionsDto?): Map<String, DynamicAction> = buildMap {
        dto?.onClick?.let { put("onClick", it.toDomain()) }
        dto?.onLongClick?.let { put("onLongClick", it.toDomain()) }
        dto?.onValueChange?.let { put("onValueChange", it.toDomain()) }
        dto?.onFocus?.let { put("onFocus", it.toDomain()) }
        dto?.onSubmit?.let { put("onSubmit", it.toDomain()) }
    }

    private fun mapBinding(dto: DynamicBindingDto) = DynamicBinding(dto.key)

    private fun DynamicActionDto.toDomain() = DynamicAction(
        type = type,
        payload = values.toDynamicValueMap(payload),
        destination = destination?.let { DynamicDestination(it.screenId, it.templateId, it.templateType, it.endpoint) },
    )
}
