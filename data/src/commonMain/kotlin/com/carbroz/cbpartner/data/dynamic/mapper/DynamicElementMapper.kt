package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicBindingDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicElementDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicValidationDto
import com.carbroz.cbpartner.domain.model.dynamic.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.DividerElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.TextElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class DynamicElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    fun map(dto: DynamicElementDto): DynamicElement {
        val semanticRole = values.string(dto.properties, "semanticRole")
        val actions = dto.actions?.let(::mapActions).orEmpty()

        return when (dto.type) {
            "text" -> TextElement(
                id = dto.id,
                semanticRole = semanticRole,
                text = values.string(dto.properties, "text").orEmpty(),
                spans = values.spans(dto.properties),
                fontSize = values.number(dto.properties, "fontSize"),
                fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
                letterSpacing = values.number(dto.properties, "letterSpacing"),
                lineHeight = values.number(dto.properties, "lineHeight"),
                color = values.string(dto.properties, "color"),
                textAlign = values.string(dto.properties, "textAlign") ?: "center",
                fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
                leading = elements(dto.properties, "leading"),
                trailing = elements(dto.properties, "trailing"),
                actions = actions,
            )
            "image" -> ImageElement(
                id = dto.id,
                semanticRole = semanticRole,
                url = values.string(dto.properties, "url").orEmpty(),
                width = values.number(dto.properties, "width"),
                height = values.number(dto.properties, "height"),
                maxWidth = values.number(dto.properties, "maxWidth"),
                fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
                contentScale = values.string(dto.properties, "contentScale") ?: "fit",
                actions = actions,
            )
            "input" -> InputElement(
                id = dto.id,
                semanticRole = semanticRole,
                placeholder = values.string(dto.properties, "placeholder"),
                keyboardType = values.string(dto.properties, "keyboardType") ?: "text",
                maxLength = values.number(dto.properties, "maxLength")?.toInt(),
                weight = values.number(dto.properties, "weight"),
                validation = dto.validation?.toDomain(),
                binding = dto.binding?.toDomain(),
                presentation = values.inputPresentation(dto.properties),
                actions = actions,
            )
            "button" -> ButtonElement(
                id = dto.id,
                semanticRole = semanticRole,
                text = values.string(dto.properties, "text").orEmpty(),
                height = values.number(dto.properties, "height"),
                fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
                fontSize = values.number(dto.properties, "fontSize"),
                fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
                textColor = values.string(dto.properties, "textColor"),
                background = values.background(dto.properties),
                shape = values.shape(dto.properties),
                leading = elements(dto.properties, "leading"),
                trailing = elements(dto.properties, "trailing"),
                actions = actions,
            )
            "icon" -> IconElement(
                id = dto.id,
                semanticRole = semanticRole,
                name = values.string(dto.properties, "name").orEmpty(),
                size = values.number(dto.properties, "size"),
                color = values.string(dto.properties, "color"),
                actions = actions,
            )
            "divider" -> DividerElement(
                id = dto.id,
                semanticRole = semanticRole,
                orientation = values.string(dto.properties, "orientation") ?: "horizontal",
                width = values.number(dto.properties, "width"),
                height = values.number(dto.properties, "height"),
                thickness = values.number(dto.properties, "thickness") ?: 1.0,
                color = values.string(dto.properties, "color"),
                actions = actions,
            )
            else -> error("Unsupported dynamic element type: ${dto.type}")
        }
    }

    private fun elements(properties: JsonObject, key: String): List<DynamicElement> =
        (properties[key] as? JsonArray).orEmpty().mapNotNull { item ->
            val value = item as? JsonObject ?: return@mapNotNull null
            val type = values.string(value, "type") ?: return@mapNotNull null
            val id = values.string(value, "id") ?: "inline-$type"
            map(
                DynamicElementDto(
                    id = id,
                    type = type,
                    properties = value["properties"] as? JsonObject ?: JsonObject(emptyMap()),
                    validation = value["validation"]?.let {
                        json.decodeFromJsonElement(DynamicValidationDto.serializer(), it)
                    },
                    binding = value["binding"]?.let {
                        json.decodeFromJsonElement(DynamicBindingDto.serializer(), it)
                    },
                    actions = value["actions"]?.let {
                        json.decodeFromJsonElement(DynamicActionsDto.serializer(), it)
                    },
                ),
            )
        }

    private fun mapActions(dto: DynamicActionsDto): Map<String, DynamicAction> = buildMap {
        dto.onClick?.let { put("onClick", it.toDomain()) }
        dto.onLongClick?.let { put("onLongClick", it.toDomain()) }
        dto.onValueChange?.let { put("onValueChange", it.toDomain()) }
        dto.onFocus?.let { put("onFocus", it.toDomain()) }
        dto.onSubmit?.let { put("onSubmit", it.toDomain()) }
    }

    private fun DynamicActionDto.toDomain() = DynamicAction(
        type = type,
        payload = values.toDynamicValueMap(payload),
        destination = destination?.let {
            com.carbroz.cbpartner.domain.model.DynamicDestination(
                screenId = it.screenId,
                templateId = it.templateId,
                templateType = it.templateType,
                endpoint = it.endpoint,
            )
        },
    )

    private fun DynamicValidationDto.toDomain() = DynamicValidation(required, pattern, message)

    private fun DynamicBindingDto.toDomain() = DynamicBinding(key)

    private fun JsonArray.orEmpty() = this
}
