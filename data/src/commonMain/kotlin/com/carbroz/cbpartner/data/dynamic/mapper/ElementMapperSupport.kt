package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicBindingDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

internal class ElementMapperSupport(
    json: Json,
    private val values: DynamicValueMapper,
) {
    private val contentMapper = ElementContentMapper(json, values)

    fun semanticRole(properties: JsonObject): String? = values.string(properties, "semanticRole")
    fun padding(properties: JsonObject) = values.padding(properties)
    fun leading(properties: JsonObject) = contentMapper.mapList(properties, "leading")
    fun trailing(properties: JsonObject) = contentMapper.mapList(properties, "trailing")

    fun actions(dto: DynamicActionsDto?): Map<String, DynamicAction> = buildMap {
        dto?.onClick?.let { put("onClick", it.toDomain()) }
        dto?.onLongClick?.let { put("onLongClick", it.toDomain()) }
        dto?.onValueChange?.let { put("onValueChange", it.toDomain()) }
        dto?.onFocus?.let { put("onFocus", it.toDomain()) }
        dto?.onSubmit?.let { put("onSubmit", it.toDomain()) }
    }

    fun binding(dto: DynamicBindingDto?) = dto?.let { DynamicBinding(it.key) }

    private fun DynamicActionDto.toDomain() = DynamicAction(
        type = type,
        payload = values.toDynamicValueMap(payload),
        destination = destination?.let {
            DynamicDestination(it.screenId, it.templateId, it.templateType, it.endpoint)
        },
    )
}