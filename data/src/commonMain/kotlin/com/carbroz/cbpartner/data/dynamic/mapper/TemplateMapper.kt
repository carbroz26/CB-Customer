package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.template.FormTemplateDto
import com.carbroz.cbpartner.data.dynamic.model.template.TemplateSlotDto
import com.carbroz.cbpartner.domain.model.dynamic.template.DynamicTemplate
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.template.TemplateSlot
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.template.TemplateBackgroundImage

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
            header = dto.header?.let(::mapSlot),
            footer = dto.footer?.let(::mapSlot),
            scrollable = values.boolean(dto.properties, "scrollable") ?: false,
            backgroundImage = dto.properties["backgroundImage"]?.let(::mapBackgroundImage),
        )
    }

    private fun mapBackgroundImage(value: kotlinx.serialization.json.JsonElement): TemplateBackgroundImage {
        require(value is JsonObject) { "Template backgroundImage must be an object" }
        require(value.keys.all { it in setOf("source", "contentScale", "alignment", "fallbackUrl") }) {
            "Unsupported Template backgroundImage property"
        }
        val source = values.checkedAssetSource(value, requireTyped = true)
        require(source is DynamicAssetSource.Url || source is DynamicAssetSource.Resource) {
            "Template backgroundImage must use a typed URL or packaged resource source"
        }
        val scale = values.assetContentScale(value)
        require(scale != null) { "Template backgroundImage contentScale is required" }
        val alignment = value["alignment"] as? JsonPrimitive
        require(alignment != null && alignment.isString) { "Template backgroundImage alignment is required" }
        require(alignment.content in setOf(
            "topStart", "topCenter", "topEnd",
            "centerStart", "center", "centerEnd",
            "bottomStart", "bottomCenter", "bottomEnd",
        )) { "Unsupported Template backgroundImage alignment" }
        return TemplateBackgroundImage(
            source = source,
            contentScale = scale,
            alignment = alignment.content,
            fallbackUrl = values.string(value, "fallbackUrl"),
        )
    }

    private fun mapSlot(slot: TemplateSlotDto): TemplateSlot = TemplateSlot(
        padding = values.padding(slot.properties),
        components = slot.components.map(components::map),
    )
}
