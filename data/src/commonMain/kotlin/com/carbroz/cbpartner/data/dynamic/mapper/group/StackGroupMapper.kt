package com.carbroz.cbpartner.data.dynamic.mapper.group

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicElementMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.group.StackGroupDto
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup

class StackGroupMapper(
    private val elements: DynamicElementMapper,
    private val values: DynamicValueMapper,
) {
    fun map(dto: StackGroupDto): StackGroup {
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
        )
    }
}
