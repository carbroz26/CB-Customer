package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.mapper.content.ElementContentMapper
import com.carbroz.cbpartner.data.dynamic.model.element.DynamicElementDto
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan
import kotlinx.serialization.json.JsonObject

class ElementMapper(
    private val values: DynamicValueMapper,
    private val actionMapper: DynamicActionMapper,
    private val contentMapper: ElementContentMapper,
) {

    fun map(element: DynamicElementDto): DynamicElement = when (element.type) {
        "text" -> mapText(element)
        "image" -> mapImage(element)
        "input" -> mapInput(element)
        "button" -> mapButton(element)
        "icon" -> mapIcon(element)
        else -> error("Unsupported dynamic element type: " + element.type)
    }

    private fun mapText(dto: DynamicElementDto): TextElement {
        val textValue = dto.properties["text"]
        val spans = values.spans(dto.properties).let { mapped ->
            if (textValue is JsonObject) {
                listOf(DynamicTextSpan(text = values.toDynamicValue(textValue))) + mapped
            } else mapped
        }
        return TextElement(
            id = dto.id,
            semanticRole = semanticRole(dto.properties),
            text = values.string(dto.properties, "text").orEmpty(),
            spans = spans,
            fontSize = values.number(dto.properties, "fontSize"),
            fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
            letterSpacing = values.number(dto.properties, "letterSpacing"),
            lineHeight = values.number(dto.properties, "lineHeight"),
            color = values.string(dto.properties, "color"),
            textAlign = values.string(dto.properties, "textAlign"),
            width = values.number(dto.properties, "width"),
            maxWidth = values.number(dto.properties, "maxWidth"),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            padding = values.padding(dto.properties),
            leadingContent = contentMapper.mapList(dto.properties, "leading"),
            trailingContent = contentMapper.mapList(dto.properties, "trailing"),
            actions = actionMapper.map(dto.actions),
        )
    }

    private fun mapImage(dto: DynamicElementDto) = ImageElement(
        id = dto.id,
        semanticRole = semanticRole(dto.properties),
        url = values.string(dto.properties, "url").orEmpty(),
        fallbackUrl = values.string(dto.properties, "fallbackUrl"),
        width = values.number(dto.properties, "width"),
        height = values.number(dto.properties, "height"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        maxHeight = values.number(dto.properties, "maxHeight"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        fillMaxHeight = values.boolean(dto.properties, "fillMaxHeight") ?: false,
        contentScale = values.string(dto.properties, "contentScale"),
        padding = values.padding(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = actionMapper.map(dto.actions),
    )

    private fun mapInput(dto: DynamicElementDto) = InputElement(
        id = dto.id,
        semanticRole = semanticRole(dto.properties),
        placeholder = values.string(dto.properties, "placeholder"),
        keyboardType = values.string(dto.properties, "keyboardType") ?: "text",
        fontSize = values.number(dto.properties, "fontSize"),
        fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
        textColor = values.string(dto.properties, "textColor"),
        textAlign = values.string(dto.properties, "textAlign"),
        maxLength = values.number(dto.properties, "maxLength")?.toInt(),
        weight = values.number(dto.properties, "weight"),
        width = values.number(dto.properties, "width"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        padding = values.padding(dto.properties),
        validation = dto.validation?.let { DynamicValidation(it.required, it.pattern, it.message) },
        binding = dto.binding?.let { DynamicBinding(it.key) },
        presentation = values.inputPresentation(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = actionMapper.map(dto.actions),
    )

    private fun mapButton(dto: DynamicElementDto) = ButtonElement(
        id = dto.id,
        semanticRole = semanticRole(dto.properties),
        text = values.string(dto.properties, "text").orEmpty(),
        width = values.number(dto.properties, "width"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        height = values.number(dto.properties, "height"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        fontSize = values.number(dto.properties, "fontSize"),
        fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
        textColor = values.string(dto.properties, "textColor"),
        background = values.background(dto.properties),
        shape = values.shape(dto.properties),
        padding = values.padding(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = actionMapper.map(dto.actions),
    )

    private fun mapIcon(dto: DynamicElementDto) = IconElement(
        id = dto.id,
        semanticRole = semanticRole(dto.properties),
        url = values.string(dto.properties, "url").orEmpty(),
        fallbackUrl = values.string(dto.properties, "fallbackUrl"),
        size = values.number(dto.properties, "size"),
        color = values.string(dto.properties, "color"),
        padding = values.padding(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = actionMapper.map(dto.actions),
    )

    private fun semanticRole(properties: JsonObject): String? =
        values.string(properties, "semanticRole")
}
