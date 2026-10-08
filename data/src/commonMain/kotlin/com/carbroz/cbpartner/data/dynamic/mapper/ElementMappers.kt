package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.ButtonElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.IconElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.ImageElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.InputElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.TextElementDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class ElementMappers(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val text = TextElementMapper(json, values)
    private val image = ImageElementMapper(json, values)
    private val input = InputElementMapper(json, values)
    private val button = ButtonElementMapper(json, values)
    private val icon = IconElementMapper(json, values)

    fun map(element: JsonObject): Any = when (values.string(element, "type")) {
        "text" -> text.map(json.decodeFromJsonElement(TextElementDto.serializer(), element))
        "image" -> image.map(json.decodeFromJsonElement(ImageElementDto.serializer(), element))
        "input" -> input.map(json.decodeFromJsonElement(InputElementDto.serializer(), element))
        "button" -> button.map(json.decodeFromJsonElement(ButtonElementDto.serializer(), element))
        "icon" -> icon.map(json.decodeFromJsonElement(IconElementDto.serializer(), element))
        else -> error("Unsupported dynamic element type: " + values.string(element, "type"))
    }
}