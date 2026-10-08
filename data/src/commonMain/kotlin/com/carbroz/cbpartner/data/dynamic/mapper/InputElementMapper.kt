package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.InputElementDto
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import kotlinx.serialization.json.Json

class InputElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val support = ElementMapperSupport(json, values)

    fun map(dto: InputElementDto) = InputElement(
        id = dto.id,
        semanticRole = support.semanticRole(dto.properties),
        placeholder = values.string(dto.properties, "placeholder"),
        keyboardType = values.string(dto.properties, "keyboardType") ?: "text",
        maxLength = values.number(dto.properties, "maxLength")?.toInt(),
        weight = values.number(dto.properties, "weight"),
        width = values.number(dto.properties, "width"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        padding = support.padding(dto.properties),
        validation = dto.validation?.let { DynamicValidation(it.required, it.pattern, it.message) },
        binding = support.binding(dto.binding),
        presentation = values.inputPresentation(dto.properties),
        leadingContent = support.leading(dto.properties),
        trailingContent = support.trailing(dto.properties),
        actions = support.actions(dto.actions),
    )
}