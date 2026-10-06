package com.carbroz.cbpartner.data.dynamic.mapper.section

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicElementMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.section.StackSectionDto
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.data.dynamic.mapper.group.StackGroupMapper

class StackSectionMapper(
    private val elements: DynamicElementMapper,
    private val values: DynamicValueMapper,
) {
    private val groupMapper = StackGroupMapper(elements, values)

    fun map(dto: StackSectionDto): StackSection {
        require(dto.type == "stack_section") { "Unsupported dynamic section type: " + dto.type }
        return StackSection(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            elements = dto.elements.map(elements::map),
            groups = dto.groups.map(groupMapper::map),
        )
    }
}
