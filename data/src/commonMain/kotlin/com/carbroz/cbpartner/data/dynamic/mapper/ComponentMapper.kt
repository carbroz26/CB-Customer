package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.component.StackComponentDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent

class ComponentMapper(
    private val elements: ElementMapper,
    private val sections: SectionMapper,
    private val values: DynamicValueMapper,
) {
    fun map(dto: StackComponentDto): StackComponent {
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
