package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.TextElementDto
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan
import kotlinx.serialization.json.Json

class TextElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val support = ElementMapperSupport(json, values)

    fun map(dto: TextElementDto): TextElement {
        val textValue = dto.properties["text"]
        val spans = values.spans(dto.properties).let { mapped ->
            if (textValue is kotlinx.serialization.json.JsonObject) {
                listOf(DynamicTextSpan(text = values.toDynamicValue(textValue))) + mapped
            } else mapped
        }
        return TextElement(
            id = dto.id,
            semanticRole = support.semanticRole(dto.properties),
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
            padding = support.padding(dto.properties),
            leadingContent = support.leading(dto.properties),
            trailingContent = support.trailing(dto.properties),
            actions = support.actions(dto.actions),
        )
    }
}