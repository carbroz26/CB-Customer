package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.content.DynamicContentDto
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement

class ElementContentMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
    private val actionMapper: DynamicActionMapper = DynamicActionMapper(values),
) {
    fun mapList(properties: JsonObject, key: String): List<ElementContent> =
        (properties[key] as? JsonArray).orEmpty()
            .mapNotNull { it as? JsonObject }
            .map { element ->
                map(
                    DynamicContentDto(
                        type = element["type"],
                        id = element["id"],
                        properties = element["properties"],
                        actions = element["actions"],
                    ),
                )
            }

    fun map(dto: DynamicContentDto): ElementContent {
        val type = string(dto.type)
            ?: error("Dynamic element content type is required")
        val properties = dto.properties as? JsonObject ?: JsonObject(emptyMap())
        val actions = (dto.actions as? JsonObject)?.let {
            json.decodeFromJsonElement(DynamicActionsDto.serializer(), it)
        }
        return when (type) {
            "icon" -> IconContent(
                id = string(dto.id),
                semanticRole = values.string(properties, "semanticRole"),
                url = values.string(properties, "url").orEmpty(),
                fallbackUrl = values.string(properties, "fallbackUrl"),
                size = values.number(properties, "size"),
                color = values.string(properties, "color"),
                spacing = values.spacing(properties),
                actions = actionMapper.map(actions),
            )
            "divider" -> DividerContent(
                id = string(dto.id),
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

    private fun string(value: JsonElement?): String? =
        (value as? JsonPrimitive)?.contentOrNull
}
