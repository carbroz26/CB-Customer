package com.carbroz.cbpartner.data.dynamic.mapper.content

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.content.ElementContent
import kotlinx.serialization.json.JsonObject

interface ContentTypeMapper {
    val type: String
    fun map(id: String?, properties: JsonObject, actions: Map<String, DynamicAction>): ElementContent
}
