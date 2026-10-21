package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.*
import kotlinx.serialization.json.*

class DynamicValueMapper {
    fun toDynamicValueMap(value: JsonObject): Map<String, DynamicValue> =
        value.entries.associate { (key, item) -> key to toDynamicValue(item) }

    fun toDynamicValue(value: JsonElement): DynamicValue = when (value) {
        JsonNull -> DynamicValue.NullValue
        is JsonObject -> DynamicValue.ObjectValue(value.entries.associate { (key, item) -> key to toDynamicValue(item) })
        is JsonArray -> DynamicValue.ArrayValue(value.map(::toDynamicValue))
        is JsonPrimitive -> when {
            value.isString -> DynamicValue.StringValue(value.content)
            value.booleanOrNull != null -> DynamicValue.BooleanValue(value.booleanOrNull!!)
            value.doubleOrNull != null -> DynamicValue.NumberValue(value.doubleOrNull!!)
            else -> DynamicValue.NullValue
        }
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

    fun spacing(properties: JsonObject): DynamicSpacing? {
        val value = properties["spacing"] as? JsonObject ?: return null
        return DynamicSpacing(
            start = number(value, "start") ?: 0.0,
            top = number(value, "top") ?: 0.0,
            end = number(value, "end") ?: 0.0,
            bottom = number(value, "bottom") ?: 0.0,
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
                    (item as? JsonObject)?.let { color ->
                        string(color, "color")?.let { DynamicGradientStop(it, number(color, "stop")) }
                    }
                },
            )
            else -> string(value, "color")?.let(DynamicBackground::Solid)
        }
    }

    fun border(properties: JsonObject, key: String = "border"): DynamicBorder? {
        val value = properties[key] as? JsonObject ?: return null
        return DynamicBorder(number(value, "width") ?: return null, string(value, "color") ?: return null)
    }

    fun shape(properties: JsonObject): DynamicShape? {
        val value = properties["shape"] as? JsonObject ?: return null
        if (string(value, "type") != "roundedCorner") return null
        return DynamicShape.RoundedCorner(
            cornerRadius = number(value, "cornerRadius") ?: 0.0,
            topStart = number(value, "topStart"),
            topEnd = number(value, "topEnd"),
            bottomStart = number(value, "bottomStart"),
            bottomEnd = number(value, "bottomEnd"),
        )
    }

    fun assetSource(properties: JsonObject, key: String = "source"): DynamicAssetSource? {
        val value = properties[key] ?: return null
        require(value is JsonObject && value.keys == setOf("type", "value")) {
            "Dynamic asset source requires type and value"
        }
        val raw = value.getValue("value")
        val reference = raw is JsonObject && raw.size == 1 &&
            raw.keys.single() in setOf("\$binding", "\$context", "\$state", "\$literal", "\$item") &&
            (raw.values.single() as? JsonPrimitive)?.let {
                it.isString && it.content.isNotBlank()
            } == true
        require((raw is JsonPrimitive && raw.isString && raw.content.isNotBlank()) || reference) {
            "Dynamic asset value must be a nonblank string or a reference"
        }
        return when (string(value, "type")) {
            "url" -> DynamicAssetSource.Url(toDynamicValue(raw))
            "resource" -> DynamicAssetSource.Resource(toDynamicValue(raw))
            else -> throw IllegalArgumentException("Unsupported dynamic asset source type")
        }
    }

    fun checkedAssetSource(properties: JsonObject, requireTyped: Boolean = false, requirePrimary: Boolean = false): DynamicAssetSource? {
        val source = assetSource(properties)
        require(!("source" in properties && "url" in properties)) { "Typed source and legacy url cannot coexist" }
        require(source !is DynamicAssetSource.Resource || "fallbackUrl" !in properties) {
            "Resource source cannot have fallbackUrl"
        }
        listOf("url", "fallbackUrl").forEach { key ->
            if (key in properties) {
                val raw = properties.getValue(key)
                require(raw is JsonPrimitive && raw.isString && raw.content.isNotBlank()) {
                    "Asset URL must be a nonblank string"
                }
            }
        }
        require(!requireTyped || source != null) { "Typed asset source is required" }
        require(!requirePrimary || source != null || "url" in properties) { "Asset source or legacy url is required" }
        return source
    }

    fun assetContentScale(properties: JsonObject): String? {
        val raw = properties["contentScale"] ?: return null
        require(raw is JsonPrimitive && raw.isString &&
            raw.content in setOf("fit", "crop", "fillBounds", "fillHeight", "fillWidth", "inside")) {
            "Unsupported dynamic image contentScale"
        }
        return raw.content
    }

    fun assetDimension(properties: JsonObject, key: String): Double? {
        if (key !in properties) return null
        val raw = properties.getValue(key) as? JsonPrimitive
        val dimension = raw?.takeUnless { it.isString }?.doubleOrNull
        require(dimension != null && dimension.isFinite() && dimension > 0.0) {
            "Dynamic image dimension must be positive and finite"
        }
        return dimension
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
        return DynamicInputPresentation(
            type = type,
            count = number(value, "count")?.toInt(),
            spacing = number(value, "spacing"),
            segmentWidth = number(value, "segmentWidth"),
            segmentHeight = number(value, "segmentHeight"),
            background = background(value),
            border = border(value),
            shape = shape(value),
            focusBorder = border(value, "focusBorder"),
        )
    }

    private fun toAction(value: JsonObject): DynamicAction? {
        val type = string(value, "type") ?: return null
        return DynamicAction(type, (value["payload"] as? JsonObject)?.let(::toDynamicValueMap).orEmpty())
    }
}
