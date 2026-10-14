package com.carbroz.cbpartner.data.dynamic.mapper.template

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import kotlinx.serialization.json.JsonObject

internal data class TemplateLayoutProperties(
    val orientation: String,
    val verticalArrangement: DynamicArrangement,
    val horizontalArrangement: DynamicArrangement,
    val horizontalAlignment: DynamicArrangement,
    val verticalAlignment: DynamicArrangement,
    val padding: DynamicPadding?,
    val fillMaxSize: Boolean,
)

internal fun DynamicValueMapper.templateLayout(properties: JsonObject) = TemplateLayoutProperties(
    orientation = string(properties, "orientation") ?: "vertical",
    verticalArrangement = verticalArrangement(properties),
    horizontalArrangement = horizontalArrangement(properties),
    horizontalAlignment = horizontalAlignment(properties),
    verticalAlignment = verticalAlignment(properties),
    padding = padding(properties),
    fillMaxSize = boolean(properties, "fillMaxSize") ?: false,
)
