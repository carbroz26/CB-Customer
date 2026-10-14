package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.mapper.ComponentMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.template.DynamicTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate

class FormTemplateMapper(
    private val components: ComponentMapper,
    private val values: DynamicValueMapper,
) : TemplateTypeMapper {
    override val type: String = "form_template"

    override fun map(dto: DynamicTemplateDto): FormTemplate {
        val layout = values.templateLayout(dto.properties)
        return FormTemplate(
            id = dto.id,
            type = dto.type,
            orientation = layout.orientation,
            verticalArrangement = layout.verticalArrangement,
            horizontalArrangement = layout.horizontalArrangement,
            horizontalAlignment = layout.horizontalAlignment,
            verticalAlignment = layout.verticalAlignment,
            padding = layout.padding,
            fillMaxSize = layout.fillMaxSize,
            semanticRole = values.string(dto.properties, "semanticRole"),
            components = dto.components.map(components::map),
        )
    }
}
