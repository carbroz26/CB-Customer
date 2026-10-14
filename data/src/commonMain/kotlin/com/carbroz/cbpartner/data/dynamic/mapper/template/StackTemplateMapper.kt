package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.mapper.ComponentMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.template.DynamicTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.StackTemplate

class StackTemplateMapper(
    private val components: ComponentMapper,
    private val values: DynamicValueMapper,
) : TemplateTypeMapper {
    override val type: String = "stack_template"

    override fun map(dto: DynamicTemplateDto) = StackTemplate(
        id = dto.id,
        type = dto.type,
        orientation = values.string(dto.properties, "orientation") ?: "vertical",
        verticalArrangement = values.verticalArrangement(dto.properties),
        horizontalArrangement = values.horizontalArrangement(dto.properties),
        horizontalAlignment = values.horizontalAlignment(dto.properties),
        verticalAlignment = values.verticalAlignment(dto.properties),
        padding = values.padding(dto.properties),
        fillMaxSize = values.boolean(dto.properties, "fillMaxSize") ?: false,
        components = dto.components.map(components::map),
    )
}
