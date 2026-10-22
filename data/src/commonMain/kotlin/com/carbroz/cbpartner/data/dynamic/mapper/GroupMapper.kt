package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.group.StackGroupDto
import com.carbroz.cbpartner.domain.model.dynamic.group.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicFocusPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSelectedStyle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationMessageStyle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationPresentation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class GroupMapper(
    private val json: Json,
    private val elements: ElementMapper,
    private val values: DynamicValueMapper,
) {
    fun map(node: JsonObject): DynamicGroup = when (values.string(node, "type")) {
        "stack_group" -> map(json.decodeFromJsonElement(StackGroupDto.serializer(), node))
        else -> throw IllegalArgumentException("Unsupported dynamic group type: " + values.string(node, "type"))
    }

    private fun map(dto: StackGroupDto): StackGroup {
        require(dto.type == "stack_group") { "Unsupported dynamic group type: " + dto.type }
        return StackGroup(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalArrangement = values.horizontalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            verticalAlignment = values.verticalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            height = values.number(dto.properties, "height"),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            background = values.background(dto.properties),
            border = values.border(dto.properties),
            shape = values.shape(dto.properties),
            elements = dto.elements.map(elements::map),
            minHeight = values.number(dto.properties, "minHeight"),
            selectedStyle = mapSelectedStyle(dto.properties),
            focusPresentation = mapFocusPresentation(dto.properties),
            validationPresentation = mapValidationPresentation(dto.properties),
        )
    }

    private fun mapSelectedStyle(properties: JsonObject): DynamicSelectedStyle? {
        val raw = properties["selectedStyle"] as? JsonObject ?: return null
        return DynamicSelectedStyle(background = values.background(raw))
    }

    private fun mapFocusPresentation(properties: JsonObject): DynamicFocusPresentation? {
        val raw = properties["focusPresentation"] as? JsonObject ?: return null
        return DynamicFocusPresentation(
            binding = values.string(raw, "binding")?.takeIf(String::isNotBlank)
                ?: error("Dynamic focus presentation binding is required"),
            border = values.border(raw)
                ?: error("Dynamic focus presentation border is required"),
        )
    }

    private fun mapValidationPresentation(properties: JsonObject): DynamicValidationPresentation? {
        val raw = properties["validationPresentation"] as? JsonObject ?: return null
        val messageStyle = (raw["messageStyle"] as? JsonObject)?.let { style ->
            DynamicValidationMessageStyle(
                fontSize = values.number(style, "fontSize"),
                fontWeight = values.number(style, "fontWeight")?.toInt(),
                color = values.string(style, "color"),
                spacing = values.spacing(style),
                reserveSpaceWhenHidden = values.boolean(style, "reserveSpaceWhenHidden") ?: false,
                minHeight = values.number(style, "minHeight"),
            )
        }
        require(messageStyle?.minHeight == null ||
            (messageStyle.minHeight.isFinite() && messageStyle.minHeight >= 0.0)) {
            "Validation message minHeight must be finite and nonnegative"
        }
        return DynamicValidationPresentation(
            binding = values.string(raw, "binding")?.takeIf(String::isNotBlank)
                ?: error("Dynamic validation presentation binding is required"),
            errorBorder = values.border(raw, "errorBorder"),
            messageStyle = messageStyle,
        )
    }
}
