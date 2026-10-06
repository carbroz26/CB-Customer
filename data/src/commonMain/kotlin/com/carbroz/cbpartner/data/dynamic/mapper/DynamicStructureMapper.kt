package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.component.StackComponentDto
import com.carbroz.cbpartner.data.dynamic.model.group.StackGroupDto
import com.carbroz.cbpartner.data.dynamic.model.section.StackSectionDto
import com.carbroz.cbpartner.data.dynamic.model.template.FormTemplateDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate

class DynamicStructureMapper(
    private val elements: DynamicElementMapper,
    private val values: DynamicValueMapper,
) {
    fun mapTemplate(dto: FormTemplateDto): FormTemplate {
        require(dto.type == "form_template") { "Unsupported dynamic template type: " + dto.type }
        return FormTemplate(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxSize = values.boolean(dto.properties, "fillMaxSize") ?: false,
            semanticRole = values.string(dto.properties, "semanticRole"),
            components = dto.components.map(::mapComponent),
        )
    }

    fun mapComponent(dto: StackComponentDto): StackComponent {
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
            sections = dto.sections.map(::mapSection),
        )
    }

    fun mapSection(dto: StackSectionDto): StackSection {
        require(dto.type == "stack_section") { "Unsupported dynamic section type: " + dto.type }
        return StackSection(
            id = dto.id,
            orientation = values.string(dto.properties, "orientation") ?: "vertical",
            verticalArrangement = values.verticalArrangement(dto.properties),
            horizontalAlignment = values.horizontalAlignment(dto.properties),
            padding = values.padding(dto.properties),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            elements = dto.elements.map(elements::map),
            groups = dto.groups.map(::mapGroup),
        )
    }

    fun mapGroup(dto: StackGroupDto): StackGroup {
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