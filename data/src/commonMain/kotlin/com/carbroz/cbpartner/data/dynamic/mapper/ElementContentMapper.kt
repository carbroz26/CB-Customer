package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class ElementContentMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
    private val actionMapper: DynamicActionMapper = DynamicActionMapper(values),
) {
    fun mapList(properties: JsonObject, key: String): List<ElementContent> {
        val items = properties[key] ?: return emptyList()
        require(items is JsonArray) { "Dynamic element content '$key' must be an array" }
        return items.map { item ->
            require(item is JsonObject) { "Dynamic element content '$key' entries must be objects" }
            map(item)
        }
    }

    fun map(element: JsonObject): ElementContent {
        val type = values.string(element, "type")
            ?: error("Dynamic element content type is required")
        val properties = element["properties"] as? JsonObject ?: JsonObject(emptyMap())
        val actions = (element["actions"] as? JsonObject)?.let {
            json.decodeFromJsonElement(DynamicActionsDto.serializer(), it)
        }
        return when (type) {
            "icon" -> IconContent(
                id = values.string(element, "id"),
                semanticRole = values.string(properties, "semanticRole"),
                url = values.string(properties, "url").orEmpty(),
                fallbackUrl = values.string(properties, "fallbackUrl"),
                size = values.number(properties, "size"),
                color = values.string(properties, "color"),
                spacing = values.spacing(properties),
                actions = actionMapper.map(actions),
            )
            "divider" -> DividerContent(
                id = values.string(element, "id"),
                semanticRole = values.string(properties, "semanticRole"),
                orientation = values.string(properties, "orientation") ?: "horizontal",
                width = values.number(properties, "width"),
                height = values.number(properties, "height"),
                thickness = values.number(properties, "thickness") ?: 1.0,
                color = values.string(properties, "color"),
                spacing = values.spacing(properties),
                actions = actionMapper.map(actions),
            )
            else -> error("Unsupported dynamic element content type: " + type)
        }
    }
}
