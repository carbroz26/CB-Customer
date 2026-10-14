package com.carbroz.cbpartner.data.dynamic.mapper.content

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicActionMapper
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.content.DynamicContentDto
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement

class ElementContentMapper(
    private val json: Json,
    private val actionMapper: DynamicActionMapper,
    mappers: List<ContentTypeMapper>,
) {
    private val byType = mappers.associateBy { it.type }.also {
        require(it.size == mappers.size) { "Duplicate dynamic content mapper type registration" }
        require(mappers.none { mapper -> mapper.type.isBlank() }) { "Blank dynamic content mapper type" }
    }

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
        val mapper = byType[type]
            ?: error("Unsupported dynamic element content type: " + type)
        val properties = dto.properties as? JsonObject ?: JsonObject(emptyMap())
        val actions = (dto.actions as? JsonObject)?.let {
            json.decodeFromJsonElement(DynamicActionsDto.serializer(), it)
        }
        return mapper.map(string(dto.id), properties, actionMapper.map(actions))
    }

    private fun string(value: JsonElement?): String? =
        (value as? JsonPrimitive)?.contentOrNull
}
