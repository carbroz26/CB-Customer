package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.action.DynamicActionsDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction

class DynamicActionMapper(
    private val values: DynamicValueMapper,
) {
    fun map(dto: DynamicActionsDto?): Map<String, DynamicAction> = buildMap {
        dto?.onClick?.let { put("onClick", it.toDomain()) }
        dto?.onLongClick?.let { put("onLongClick", it.toDomain()) }
        dto?.onValueChange?.let { put("onValueChange", it.toDomain()) }
        dto?.onFocus?.let { put("onFocus", it.toDomain()) }
        dto?.onSubmit?.let { put("onSubmit", it.toDomain()) }
    }

    private fun DynamicActionDto.toDomain() = DynamicAction(
        type = type,
        payload = values.toDynamicValueMap(payload),
        destination = destination?.let {
            DynamicDestination(it.screenId, it.templateId, it.templateType, it.endpoint)
        },
    )
}
