package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.IconElementDto
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import kotlinx.serialization.json.Json

class IconElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val support = ElementMapperSupport(json, values)

    fun map(dto: IconElementDto) = IconElement(
        id = dto.id,
        semanticRole = support.semanticRole(dto.properties),
        url = values.string(dto.properties, "url").orEmpty(),
        fallbackUrl = values.string(dto.properties, "fallbackUrl"),
        size = values.number(dto.properties, "size"),
        color = values.string(dto.properties, "color"),
        padding = support.padding(dto.properties),
        leadingContent = support.leading(dto.properties),
        trailingContent = support.trailing(dto.properties),
        actions = support.actions(dto.actions),
    )
}
