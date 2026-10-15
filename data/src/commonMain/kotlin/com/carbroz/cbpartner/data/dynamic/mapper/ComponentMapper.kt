package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.component.StackComponentDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class ComponentMapper(
    private val json: Json,
    private val elements: ElementMapper,
    private val sections: SectionMapper,
    private val values: DynamicValueMapper,
) {
    fun map(node: JsonObject): DynamicComponent = when (values.string(node, "type")) {
        "stack_component" -> map(json.decodeFromJsonElement(StackComponentDto.serializer(), node))
        else -> error("Unsupported dynamic component type: " + values.string(node, "type"))
    }

    private fun map(dto: StackComponentDto): StackComponent {
        require(dto.type == "stack_component") { "Unsupported dynamic component type: " + dto.type }
        return StackComponent(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalArrangement = values.horizontalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            verticalAlignment = values.verticalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            elements = dto.elements.map(elements::map),
            sections = dto.sections.map(sections::map),
        )
    }
}
