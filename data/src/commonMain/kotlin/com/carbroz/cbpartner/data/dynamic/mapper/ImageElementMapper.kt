package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.ImageElementDto
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import kotlinx.serialization.json.Json

class ImageElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val support = ElementMapperSupport(json, values)

    fun map(dto: ImageElementDto) = ImageElement(
        id = dto.id,
        semanticRole = support.semanticRole(dto.properties),
        url = values.string(dto.properties, "url").orEmpty(),
        fallbackUrl = values.string(dto.properties, "fallbackUrl"),
        width = values.number(dto.properties, "width"),
        height = values.number(dto.properties, "height"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        maxHeight = values.number(dto.properties, "maxHeight"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        fillMaxHeight = values.boolean(dto.properties, "fillMaxHeight") ?: false,
        contentScale = values.string(dto.properties, "contentScale"),
        padding = support.padding(dto.properties),
        leadingContent = support.leading(dto.properties),
        trailingContent = support.trailing(dto.properties),
        actions = support.actions(dto.actions),
    )
}
