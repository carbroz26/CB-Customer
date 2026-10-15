package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.section.StackSectionDto
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicSection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class SectionMapper(
    private val json: Json,
    private val elements: ElementMapper,
    private val groups: GroupMapper,
    private val values: DynamicValueMapper,
) {
    fun map(node: JsonObject): DynamicSection = when (values.string(node, "type")) {
        "stack_section" -> map(json.decodeFromJsonElement(StackSectionDto.serializer(), node))
        else -> error("Unsupported dynamic section type: " + values.string(node, "type"))
    }

    private fun map(dto: StackSectionDto): StackSection {
        require(dto.type == "stack_section") { "Unsupported dynamic section type: " + dto.type }
        return StackSection(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalArrangement = values.horizontalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            verticalAlignment = values.verticalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            elements = dto.elements.map(elements::map),
            groups = dto.groups.map(groups::map),
        )
    }
}
