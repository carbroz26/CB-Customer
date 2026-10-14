package com.carbroz.cbpartner.data.dynamic.mapper.content

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import kotlinx.serialization.json.JsonObject

class DividerContentMapper(private val values: DynamicValueMapper) : ContentTypeMapper {
    override val type = "divider"

    override fun map(id: String?, properties: JsonObject, actions: Map<String, DynamicAction>) = DividerContent(
        id = id,
        semanticRole = values.string(properties, "semanticRole"),
        orientation = values.string(properties, "orientation") ?: "horizontal",
        width = values.number(properties, "width"),
        height = values.number(properties, "height"),
        thickness = values.number(properties, "thickness") ?: 1.0,
        color = values.string(properties, "color"),
        spacing = values.spacing(properties),
        actions = actions,
    )
}
