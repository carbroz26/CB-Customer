package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.mapper.content.DividerContentMapper
import com.carbroz.cbpartner.data.dynamic.mapper.content.ElementContentMapper
import com.carbroz.cbpartner.data.dynamic.mapper.content.IconContentMapper
import com.carbroz.cbpartner.data.dynamic.mapper.template.DefaultTemplateMapper
import com.carbroz.cbpartner.data.dynamic.mapper.template.FormTemplateMapper
import com.carbroz.cbpartner.data.dynamic.mapper.template.StackTemplateMapper
import com.carbroz.cbpartner.data.dynamic.mapper.template.TemplateMapper
import kotlinx.serialization.json.Json

internal object DynamicMapperFactory {
    fun elements(json: Json, values: DynamicValueMapper): ElementMapper {
        val actions = DynamicActionMapper(values)
        val content = ElementContentMapper(
            json,
            actions,
            listOf(IconContentMapper(values), DividerContentMapper(values)),
        )
        return ElementMapper(values, actions, content)
    }

    fun templates(json: Json, values: DynamicValueMapper): TemplateMapper {
        val elements = elements(json, values)
        val groups = GroupMapper(elements, values)
        val sections = SectionMapper(elements, groups, values)
        val components = ComponentMapper(elements, sections, values)
        return TemplateMapper(
            listOf(
                FormTemplateMapper(components, values),
                StackTemplateMapper(components, values),
                DefaultTemplateMapper(components, values),
            ),
        )
    }
}
