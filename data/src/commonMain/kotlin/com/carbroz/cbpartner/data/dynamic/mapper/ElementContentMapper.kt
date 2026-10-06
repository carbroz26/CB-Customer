package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.content.ElementContentDto
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
        val dto = json.decodeFromJsonElement(ElementContentDto.serializer(), element)
        return when (dto.type) {
            "icon" -> IconContent(
                id = dto.id,
                semanticRole = values.string(dto.properties, "semanticRole"),
                name = values.string(dto.properties, "name").orEmpty(),
                size = values.number(dto.properties, "size"),
                color = values.string(dto.properties, "color"),
                actions = mapActions(dto.actions),
            )
            "divider" -> DividerContent(
                id = dto.id,
                semanticRole = values.string(dto.properties, "semanticRole"),
                orientation = values.string(dto.properties, "orientation") ?: "horizontal",
                width = values.number(dto.properties, "width"),
                height = values.number(dto.properties, "height"),
                thickness = values.number(dto.properties, "thickness") ?: 1.0,
                color = values.string(dto.properties, "color"),
                actions = mapActions(dto.actions),
            )
            else -> error("Unsupported dynamic element content type: " + dto.type)
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
