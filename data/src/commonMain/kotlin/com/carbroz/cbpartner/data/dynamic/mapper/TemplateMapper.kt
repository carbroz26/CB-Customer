package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.template.FormTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.template.DynamicTemplate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class TemplateMapper(
    private val json: Json,
    private val components: ComponentMapper,
    private val values: DynamicValueMapper,
) {
    fun map(node: JsonObject): DynamicTemplate = when (values.string(node, "type")) {
        "form_template" -> map(json.decodeFromJsonElement(FormTemplateDto.serializer(), node))
        else -> throw IllegalArgumentException("Unsupported dynamic template type: " + values.string(node, "type"))
    }

    private fun map(dto: FormTemplateDto): FormTemplate {
        require(dto.type == "form_template") { "Unsupported dynamic template type: " + dto.type }
        return FormTemplate(
            id = dto.id,
            type = dto.type,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalArrangement = values.horizontalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            verticalAlignment = values.verticalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxSize = values.boolean(dto.properties, "fillMaxSize") ?: false,
            semanticRole = values.string(dto.properties, "semanticRole"),
            components = dto.components.map(components::map),
        )
    }
}