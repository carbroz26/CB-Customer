package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.element.ButtonElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.DividerElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.IconElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.ImageElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.InputElementDto
import com.carbroz.cbpartner.data.dynamic.model.element.TextElementDto
import com.carbroz.cbpartner.data.dynamic.model.validation.DynamicValidationDto
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.validation.DynamicValidatorSpec
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicButtonLoading
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicLoadingIndicator
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

class ElementMapper(
    private val json: Json,
    private val values: DynamicValueMapper,
) {
    private val actionMapper = DynamicActionMapper(values)
    private val contentMapper = ElementContentMapper(json, values, actionMapper)

    fun map(element: JsonObject): DynamicElement = when (values.string(element, "type")) {
        "text" -> mapText(json.decodeFromJsonElement(TextElementDto.serializer(), element))
        "image" -> mapImage(json.decodeFromJsonElement(ImageElementDto.serializer(), element))
        "input" -> mapInput(json.decodeFromJsonElement(InputElementDto.serializer(), element))
        "button" -> mapButton(json.decodeFromJsonElement(ButtonElementDto.serializer(), element))
        "icon" -> mapIcon(json.decodeFromJsonElement(IconElementDto.serializer(), element))
        "divider" -> mapDivider(json.decodeFromJsonElement(DividerElementDto.serializer(), element))
        else -> error("Unsupported dynamic element type: " + values.string(element, "type"))
    }

    private fun mapText(dto: TextElementDto): TextElement {
        val textValue = dto.properties["text"]
        val spans = values.spans(dto.properties).let { mapped ->
            if (textValue is JsonObject) {
                listOf(DynamicTextSpan(text = values.toDynamicValue(textValue))) + mapped
            } else mapped
        }
        return TextElement(
            id = dto.id,
            semanticRole = semanticRole(dto.properties),
            text = values.string(dto.properties, "text").orEmpty(),
            spans = spans,
            fontSize = values.number(dto.properties, "fontSize"),
            fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
            letterSpacing = values.number(dto.properties, "letterSpacing"),
            lineHeight = values.number(dto.properties, "lineHeight"),
            color = values.string(dto.properties, "color"),
            textAlign = values.string(dto.properties, "textAlign"),
            width = values.number(dto.properties, "width"),
            maxWidth = values.number(dto.properties, "maxWidth"),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            padding = values.padding(dto.properties),
            leadingContent = contentMapper.mapList(dto.properties, "leading"),
            trailingContent = contentMapper.mapList(dto.properties, "trailing"),
            actions = actionMapper.map(dto.actions),
            weight = values.number(dto.properties, "weight"),
            minHeight = values.number(dto.properties, "minHeight"),
            spacing = values.spacing(dto.properties),
            maxLines = values.number(dto.properties, "maxLines")?.toInt(),
            overflow = values.string(dto.properties, "overflow"),
        )
    }

    private fun mapImage(dto: ImageElementDto): ImageElement {
        val source = values.checkedAssetSource(dto.properties, requirePrimary = true)
        val url = values.string(dto.properties, "url").orEmpty()
        return ImageElement(
            id = dto.id,
            semanticRole = semanticRole(dto.properties),
            url = url,
            fallbackUrl = values.string(dto.properties, "fallbackUrl"),
            width = values.number(dto.properties, "width"),
            height = values.number(dto.properties, "height"),
            maxWidth = values.number(dto.properties, "maxWidth"),
            maxHeight = values.number(dto.properties, "maxHeight"),
            fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
            fillMaxHeight = values.boolean(dto.properties, "fillMaxHeight") ?: false,
            contentScale = values.assetContentScale(dto.properties),
            padding = values.padding(dto.properties),
            leadingContent = contentMapper.mapList(dto.properties, "leading"),
            trailingContent = contentMapper.mapList(dto.properties, "trailing"),
            actions = actionMapper.map(dto.actions),
            source = source,
        )
    }

    private fun mapInput(dto: InputElementDto) = InputElement(
        id = dto.id,
        semanticRole = semanticRole(dto.properties),
        placeholder = values.string(dto.properties, "placeholder"),
        keyboardType = values.string(dto.properties, "keyboardType") ?: "text",
        fontSize = values.number(dto.properties, "fontSize"),
        fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
        textColor = values.string(dto.properties, "textColor"),
        textAlign = values.string(dto.properties, "textAlign"),
        maxLength = values.number(dto.properties, "maxLength")?.toInt(),
        weight = values.number(dto.properties, "weight"),
        width = values.number(dto.properties, "width"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        padding = values.padding(dto.properties),
        validation = mapValidation(dto.validation),
        binding = dto.binding?.let { DynamicBinding(it.key) },
        presentation = values.inputPresentation(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = actionMapper.map(dto.actions),
        height = values.number(dto.properties, "height"),
        singleLine = values.boolean(dto.properties, "singleLine"),
        inputFilter = values.string(dto.properties, "inputFilter"),
        placeholderColor = values.string(dto.properties, "placeholderColor"),
        background = values.background(dto.properties),
        border = values.border(dto.properties),
        focusBorder = values.border(dto.properties, "focusBorder"),
        shape = values.shape(dto.properties),
        validationStyle = (dto.properties["validationStyle"] as? JsonObject)?.let {
            DynamicValidationStyle(background = values.background(it))
        },
    )

    private fun mapButton(dto: ButtonElementDto): ButtonElement {
        val actions = actionMapper.map(dto.actions)
        val loading = mapButtonLoading(dto.properties)
        if (loading != null) {
            require(actions["onClick"]?.type?.trim()?.lowercase() == "request" &&
                "onLongClick" !in actions) {
                "Dynamic Button loading requires onClick request without onLongClick"
            }
        }
        return ButtonElement(
        id = dto.id,
        semanticRole = semanticRole(dto.properties),
        text = values.string(dto.properties, "text").orEmpty(),
        width = values.number(dto.properties, "width"),
        maxWidth = values.number(dto.properties, "maxWidth"),
        height = values.number(dto.properties, "height"),
        fillMaxWidth = values.boolean(dto.properties, "fillMaxWidth") ?: false,
        fontSize = values.number(dto.properties, "fontSize"),
        fontWeight = values.number(dto.properties, "fontWeight")?.toInt(),
        textColor = values.string(dto.properties, "textColor"),
        background = values.background(dto.properties),
        shape = values.shape(dto.properties),
        padding = values.padding(dto.properties),
        leadingContent = contentMapper.mapList(dto.properties, "leading"),
        trailingContent = contentMapper.mapList(dto.properties, "trailing"),
        actions = actions,
        loading = loading,
        )
    }

    private fun mapIcon(dto: IconElementDto): IconElement {
        val visibleWhen = dto.properties["visibleWhen"] as? JsonObject
        if (visibleWhen != null) {
            require(visibleWhen.keys.all { it == "selected" }) {
                "Dynamic IconElement visibleWhen only supports selected"
            }
        }
        return IconElement(
            id = dto.id,
            semanticRole = semanticRole(dto.properties),
            url = values.string(dto.properties, "url").orEmpty(),
            fallbackUrl = values.string(dto.properties, "fallbackUrl"),
            size = values.number(dto.properties, "size"),
            color = values.string(dto.properties, "color"),
            padding = values.padding(dto.properties),
            leadingContent = contentMapper.mapList(dto.properties, "leading"),
            trailingContent = contentMapper.mapList(dto.properties, "trailing"),
            actions = actionMapper.map(dto.actions),
            source = values.checkedAssetSource(dto.properties),
            minWidth = values.number(dto.properties, "minWidth"),
            minHeight = values.number(dto.properties, "minHeight"),
            visibleWhenSelected = visibleWhen?.let {
                values.boolean(it, "selected") ?: error("Dynamic IconElement visibleWhen.selected must be boolean")
            },
            reserveSpaceWhenHidden = values.boolean(dto.properties, "reserveSpaceWhenHidden"),
        )
    }

    private fun mapDivider(dto: DividerElementDto): DividerElement {
        val properties = dto.properties
        fun requiredString(key: String): String {
            val raw = properties[key] as? JsonPrimitive
            require(raw != null && raw.isString) { "Dynamic divider $key must be a string" }
            return raw.content
        }
        fun requiredNumber(key: String): Double {
            val raw = properties[key] as? JsonPrimitive
            val number = raw?.takeUnless { it.isString }?.doubleOrNull
            require(number != null && number.isFinite() && number > 0.0) {
                "Dynamic divider $key must be a positive finite number"
            }
            return number
        }

        val orientation = requiredString("orientation")
        require(orientation == "horizontal" || orientation == "vertical") {
            "Unsupported dynamic divider orientation"
        }
        val spacing = properties["spacing"]?.let { value ->
            require(value is JsonObject) { "Dynamic divider spacing must be an object" }
            require(value.keys.all { it in setOf("start", "top", "end", "bottom") }) {
                "Unsupported dynamic divider spacing field"
            }
            value.forEach { (key, entry) ->
                val raw = entry as? JsonPrimitive
                val number = raw?.takeUnless { it.isString }?.doubleOrNull
                require(number != null && number.isFinite() && number >= 0.0) {
                    "Dynamic divider spacing $key must be a finite nonnegative number"
                }
            }
            values.spacing(properties)
        }
        if (orientation == "horizontal") {
            require("height" !in properties) { "Horizontal dynamic divider cannot declare height" }
        } else {
            require("width" !in properties) { "Vertical dynamic divider cannot declare width" }
        }
        return DividerElement(
            id = dto.id,
            orientation = orientation,
            width = if (orientation == "horizontal") requiredNumber("width") else null,
            height = if (orientation == "vertical") requiredNumber("height") else null,
            thickness = requiredNumber("thickness"),
            color = requiredString("color"),
            spacing = spacing,
            actions = actionMapper.map(dto.actions),
        )
    }

    private fun mapValidation(dto: DynamicValidationDto?): DynamicValidation? = dto?.let {
        val validator = it.validator?.let { spec ->
            require(spec.key.isNotBlank()) { "Dynamic validator key is required" }
            DynamicValidatorSpec(
                key = spec.key,
                arguments = values.toDynamicValueMap(spec.arguments),
            )
        }
        DynamicValidation(
            required = it.required,
            pattern = it.pattern,
            message = it.message,
            validator = validator,
        )
    }

    private fun mapButtonLoading(properties: JsonObject): DynamicButtonLoading? {
        if ("loading" !in properties) return null
        val raw = properties["loading"] as? JsonObject
            ?: throw IllegalArgumentException("Dynamic button loading must be an object")
        require(raw.keys == setOf("whileActionPending", "disableWhilePending", "replaceContent", "indicator")) {
            "Dynamic button loading keys are invalid"
        }
        for (key in listOf("whileActionPending", "disableWhilePending", "replaceContent")) {
            require(values.boolean(raw, key) == true) {
                "Dynamic button loading $key must be true"
            }
        }
        val indicator = raw["indicator"] as? JsonObject
            ?: throw IllegalArgumentException("Dynamic button loading indicator is required")
        require(indicator.keys == setOf("type", "size", "color")) {
            "Dynamic circular loading indicator keys are invalid"
        }
        require(values.string(indicator, "type") == "circular") {
            "Only circular dynamic loading indicator is supported"
        }
        val sizeValue = indicator["size"] as? JsonPrimitive
        val size = sizeValue?.takeUnless { it.isString }?.doubleOrNull
        require(size != null && size.isFinite() && size > 0.0) {
            "Dynamic circular loading indicator size must be positive and finite"
        }
        val colorValue = indicator["color"] as? JsonPrimitive
        val color = colorValue?.takeIf { it.isString }?.content
        require(color != null && color.matches(Regex("^#[A-Fa-f0-9]{6}([A-Fa-f0-9]{2})?$"))) {
            "Dynamic circular loading indicator color must be hexadecimal"
        }
        return DynamicButtonLoading(
            whileActionPending = true,
            disableWhilePending = true,
            replaceContent = true,
            indicator = DynamicLoadingIndicator.Circular(size, color),
        )
    }

    private fun semanticRole(properties: JsonObject): String? =
        values.string(properties, "semanticRole")
}
