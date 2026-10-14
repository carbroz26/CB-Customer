package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.model.template.DynamicTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.DynamicTemplate

interface TemplateTypeMapper {
    val type: String
    fun map(dto: DynamicTemplateDto): DynamicTemplate
}
