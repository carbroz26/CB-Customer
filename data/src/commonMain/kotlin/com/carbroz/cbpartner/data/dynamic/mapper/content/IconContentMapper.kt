package com.carbroz.cbpartner.data.dynamic.mapper.content

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import kotlinx.serialization.json.JsonObject

class IconContentMapper(private val values: DynamicValueMapper) : ContentTypeMapper {
    override val type = "icon"

    override fun map(id: String?, properties: JsonObject, actions: Map<String, DynamicAction>) = IconContent(
        id = id,
        semanticRole = values.string(properties, "semanticRole"),
        url = values.string(properties, "url").orEmpty(),
        fallbackUrl = values.string(properties, "fallbackUrl"),
        size = values.number(properties, "size"),
        color = values.string(properties, "color"),
        spacing = values.spacing(properties),
        actions = actions,
    )
}
