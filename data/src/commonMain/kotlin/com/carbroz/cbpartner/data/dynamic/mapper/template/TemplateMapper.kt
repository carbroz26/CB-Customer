package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.model.template.DynamicTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.template.DynamicTemplate

class TemplateMapper(mappers: List<TemplateTypeMapper>) {
    private val byType: Map<String, TemplateTypeMapper> = mappers.associateBy { it.type }.also {
        require(it.size == mappers.size) { "Duplicate dynamic template mapper type registration" }
        require(mappers.none { mapper -> mapper.type.isBlank() }) { "Blank dynamic template mapper type" }
    }

    fun map(dto: DynamicTemplateDto): DynamicTemplate =
        (byType[dto.type] ?: error("Unsupported dynamic template type: " + dto.type)).map(dto)
}
