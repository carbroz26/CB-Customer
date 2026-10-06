package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.*
import kotlinx.serialization.json.*

class DynamicValueMapper {
    fun toDynamicValueMap(value: JsonObject): Map<String, DynamicValue> = value.entries.associate { (key, item) -> key to toDynamicValue(item) }

    fun toDynamicValue(value: JsonElement): DynamicValue = when (value) {
        JsonNull -> DynamicValue.NullValue
        is JsonObject -> DynamicValue.ObjectValue(value.entries.associate { (key, item) -> key to toDynamicValue(item) })
        is JsonArray -> DynamicValue.ArrayValue(value.map(::toDynamicValue))
        is JsonPrimitive -> when {
            value.booleanOrNull != null -> DynamicValue.BooleanValue(value.booleanOrNull!!)
            value.doubleOrNull != null -> DynamicValue.NumberValue(value.doubleOrNull!!)
            value.contentOrNull != null -> DynamicValue.StringValue(value.content)
            else -> DynamicValue.NullValue
        }
        else -> DynamicValue.NullValue
    }

    fun string(value: JsonObject, key: String): String? = (value[key] as? JsonPrimitive)?.contentOrNull
    fun number(value: JsonObject, key: String): Double? = (value[key] as? JsonPrimitive)?.doubleOrNull
    fun boolean(value: JsonObject, key: String): Boolean? = (value[key] as? JsonPrimitive)?.booleanOrNull

    fun padding(properties: JsonObject): DynamicPadding? {
        val value = properties["padding"] as? JsonObject ?: return null
        return DynamicPadding(
            start = number(value, "start") ?: number(value, "horizontal") ?: number(value, "all") ?: 0.0,
            top = number(value, "top") ?: number(value, "vertical") ?: number(value, "all") ?: 0.0,
            end = number(value, "end") ?: number(value, "horizontal") ?: number(value, "all") ?: 0.0,
            bottom = number(value, "bottom") ?: number(value, "vertical") ?: number(value, "all") ?: 0.0,
        )
    }

    fun verticalArrangement(properties: JsonObject): DynamicArrangement = arrangement(properties, "verticalArrangement", true)
    fun horizontalArrangement(properties: JsonObject): DynamicArrangement = arrangement(properties, "horizontalArrangement", false)

    private fun arrangement(properties: JsonObject, key: String, vertical: Boolean): DynamicArrangement {
        val value = properties[key] as? JsonObject ?: return if (vertical) DynamicArrangement.Top else DynamicArrangement.Start
        return when (string(value, "type")) {
            "spacedBy" -> DynamicArrangement.SpacedBy(number(value, "spacing") ?: 0.0)
            "center" -> DynamicArrangement.Center
            "end" -> DynamicArrangement.End
            "bottom" -> DynamicArrangement.Bottom
            "spaceBetween" -> DynamicArrangement.SpaceBetween
            else -> if (vertical) DynamicArrangement.Top else DynamicArrangement.Start
        }
    }

    fun horizontalAlignment(properties: JsonObject): DynamicArrangement = when (string(properties, "horizontalAlignment")) {
        "start" -> DynamicArrangement.Start
        "end" -> DynamicArrangement.End
        else -> DynamicArrangement.Center
    }

    fun verticalAlignment(properties: JsonObject): DynamicArrangement = when (string(properties, "verticalAlignment")) {
        "top" -> DynamicArrangement.Top
        "bottom" -> DynamicArrangement.Bottom
        else -> DynamicArrangement.Center
    }

    fun background(properties: JsonObject): DynamicBackground? {
        val value = properties["background"] as? JsonObject ?: return null
        return when (string(value, "type")) {
            "linearGradient" -> DynamicBackground.LinearGradient(
                angle = number(value, "angle") ?: 0.0,
                colors = (value["colors"] as? JsonArray).orEmpty().mapNotNull { item ->
                    (item as? JsonObject)?.let { string(it, "color")?.let(::DynamicGradientStop) }
                },
            )
            else -> string(value, "color")?.let(DynamicBackground::Solid)
        }
    }

    fun border(properties: JsonObject): DynamicBorder? {
        val value = properties["border"] as? JsonObject ?: return null
        return DynamicBorder(number(value, "width") ?: return null, string(value, "color") ?: return null)
    }

    fun shape(properties: JsonObject): DynamicShape? {
        val value = properties["shape"] as? JsonObject ?: return null
        if (string(value, "type") != "roundedCorner") return null
        return DynamicShape.RoundedCorner(number(value, "cornerRadius") ?: 0.0)
    }

    fun spans(properties: JsonObject): List<DynamicTextSpan> = (properties["spans"] as? JsonArray).orEmpty().mapNotNull { item ->
        val value = item as? JsonObject ?: return@mapNotNull null
        DynamicTextSpan(
            text = toDynamicValue(value["text"] ?: JsonPrimitive("")),
            color = string(value, "color"),
            fontWeight = number(value, "fontWeight")?.toInt(),
            underline = boolean(value, "underline") ?: false,
            action = ((value["action"] as? JsonObject) ?: (value["onClick"] as? JsonObject))?.let(::toAction),
        )
    }

    fun inputPresentation(properties: JsonObject): DynamicInputPresentation? {
        val value = properties["presentation"] as? JsonObject ?: return null
        val type = string(value, "type") ?: return null
        return DynamicInputPresentation(type, number(value, "count")?.toInt(), number(value, "spacing"), number(value, "segmentWidth"), number(value, "segmentHeight"))
    }

    private fun toAction(value: JsonObject): DynamicAction? {
        val type = string(value, "type") ?: return null
        return DynamicAction(type, (value["payload"] as? JsonObject)?.let(::toDynamicValueMap).orEmpty())
    }
}