package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.ButtonElementDto
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import kotlinx.serialization.json.Json

class ButtonElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val support = DynamicElementMapperSupport(json, values)

    fun map(dto: ButtonElementDto) = ButtonElement(
        id = dto.id,
        semanticRole = support.semanticRole(dto.properties),
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
        padding = support.padding(dto.properties),
        leadingContent = support.leading(dto.properties),
        trailingContent = support.trailing(dto.properties),
        actions = support.actions(dto.actions),
    )
}