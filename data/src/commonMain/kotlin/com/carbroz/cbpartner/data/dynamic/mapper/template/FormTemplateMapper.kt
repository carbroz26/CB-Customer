package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicElementMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.template.FormTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate

class FormTemplateMapper(
    private val elements: DynamicElementMapper,
    private val values: DynamicValueMapper,
) {
    fun map(dto: FormTemplateDto): FormTemplate {
        require(dto.type == "form_template") { "Unsupported dynamic template type: " + dto.type }
        return FormTemplate(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxSize = values.boolean(dto.properties, "fillMaxSize") ?: false,
            semanticRole = values.string(dto.properties, "semanticRole"),
            components = dto.components.map { componentMapper.map(it) },
        )
    }

    private val componentMapper = com.carbroz.cbpartner.data.dynamic.mapper.component.StackComponentMapper(elements, values)
}
