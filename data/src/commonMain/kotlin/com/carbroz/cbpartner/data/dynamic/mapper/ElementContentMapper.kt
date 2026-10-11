package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class ElementContentMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    fun mapList(properties: JsonObject, key: String): List<ElementContent> =
        (properties[key] as? JsonArray).orEmpty()
            .mapNotNull { it as? JsonObject }
            .map { map(it) }

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
                padding = values.padding(properties),
                actions = mapActions(actions),
            )
            "divider" -> DividerContent(
                id = values.string(element, "id"),
                semanticRole = values.string(properties, "semanticRole"),
                orientation = values.string(properties, "orientation") ?: "horizontal",
                width = values.number(properties, "width"),
                height = values.number(properties, "height"),
                thickness = values.number(properties, "thickness") ?: 1.0,
                color = values.string(properties, "color"),
                actions = mapActions(actions),
            )
            else -> error("Unsupported dynamic element content type: " + type)
        }
    }

    private fun mapActions(dto: DynamicActionsDto?): Map<String, DynamicAction> = buildMap {
        dto?.onClick?.let { put("onClick", it.toDomain()) }
        dto?.onLongClick?.let { put("onLongClick", it.toDomain()) }
        dto?.onValueChange?.let { put("onValueChange", it.toDomain()) }
        dto?.onFocus?.let { put("onFocus", it.toDomain()) }
        dto?.onSubmit?.let { put("onSubmit", it.toDomain()) }
    }

    private fun DynamicActionDto.toDomain() = DynamicAction(
        type = type,
        payload = values.toDynamicValueMap(payload),
        destination = destination?.let {
            DynamicDestination(it.screenId, it.templateId, it.templateType, it.endpoint)
        },
    )
}
