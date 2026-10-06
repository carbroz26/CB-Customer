package com.carbroz.cbpartner.data.dynamic.mapper.component

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicElementMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.component.StackComponentDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.data.dynamic.mapper.section.StackSectionMapper

class StackComponentMapper(
    private val elements: DynamicElementMapper,
    private val values: DynamicValueMapper,
) {
    private val sectionMapper = StackSectionMapper(elements, values)

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
            sections = dto.sections.map(sectionMapper::map),
        )
    }
}
