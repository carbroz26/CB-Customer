package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.mapper.ComponentMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.template.DynamicTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.DefaultTemplate

class DefaultTemplateMapper(
    private val components: ComponentMapper,
    private val values: DynamicValueMapper,
) : TemplateTypeMapper {
    override val type: String = "default_template"

    override fun map(dto: DynamicTemplateDto): DefaultTemplate {
        val layout = values.templateLayout(dto.properties)
        return DefaultTemplate(
            id = dto.id,
            type = dto.type,
            orientation = layout.orientation,
            verticalArrangement = layout.verticalArrangement,
            horizontalArrangement = layout.horizontalArrangement,
            horizontalAlignment = layout.horizontalAlignment,
            verticalAlignment = layout.verticalAlignment,
            padding = layout.padding,
            fillMaxSize = layout.fillMaxSize,
            components = dto.components.map(components::map),
        )
    }
}
