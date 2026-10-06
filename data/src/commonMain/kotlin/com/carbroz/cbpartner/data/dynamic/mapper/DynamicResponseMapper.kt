package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.DynamicActionDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicActionsDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicBindingDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicComponentDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicDestinationDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicElementDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicGroupDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicScreenDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicSectionDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicTemplateDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicThemeDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicValidationDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicGradientStop
import com.carbroz.cbpartner.domain.model.dynamic.DynamicInputPresentation
import com.carbroz.cbpartner.domain.model.dynamic.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.DynamicShape
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTheme
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTextSpan
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.DividerElement
import com.carbroz.cbpartner.domain.model.dynamic.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

class DynamicResponseMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun map(dto: DynamicResponseDto): DynamicResponse {
        val data = dto.data
        val isScreen = data is JsonObject && SCREEN_FIELDS.all { field -> field in data.keys }
        val screen = data
            ?.takeIf { isScreen }
            ?.let { json.decodeFromJsonElement(DynamicScreenDto.serializer(), it) }
            ?.toDomain()
        val destination = data
            ?.takeIf { it is JsonObject }
            ?.let { it as JsonObject }
            ?.get("destination")
            ?.takeIf { it is JsonObject }
            ?.let { json.decodeFromJsonElement(DynamicDestinationDto.serializer(), it) }
            ?.toDomain()
        val dataPayload = data?.takeUnless { isScreen }?.toDynamicValue()

        return DynamicResponse(
            status = dto.status,
            code = dto.code,
            message = dto.message,
            data = screen,
            traceId = dto.traceId,
            dataPayload = dataPayload,
            destination = destination,
        )
    }

    private fun DynamicDestinationDto.toDomain() = DynamicDestination(
        screenId = screenId,
        templateId = templateId,
        templateType = templateType,
        endpoint = endpoint,
    )

    private fun DynamicScreenDto.toDomain() = DynamicScreen(
        screenId = screenId,
        schemaVersion = schemaVersion,
        targetApp = targetApp,
        template = template.toDomain(),
        theme = theme?.toDomain(),
    )

    private fun DynamicTemplateDto.toDomain(): FormTemplate {
        require(type == "form_template") { "Unsupported dynamic template type: $type" }
        return FormTemplate(
            id = id,
            orientation = properties.string("orientation") ?: "vertical",
            verticalArrangement = properties.verticalArrangement(),
            horizontalAlignment = properties.horizontalAlignment(),
            padding = properties.padding(),
            fillMaxSize = properties.boolean("fillMaxSize") ?: false,
            semanticRole = properties.string("semanticRole"),
            components = components.map { it.toDomain() },
        )
    }

    private fun DynamicComponentDto.toDomain(): StackComponent {
        require(type == "stack_component") { "Unsupported dynamic component type: $type" }
        return StackComponent(
            id = id,
            orientation = properties.string("orientation") ?: "vertical",
            verticalArrangement = properties.verticalArrangement(),
            horizontalArrangement = properties.horizontalArrangement(),
            horizontalAlignment = properties.horizontalAlignment(),
            verticalAlignment = properties.verticalAlignment(),
            padding = properties.padding(),
            fillMaxWidth = properties.boolean("fillMaxWidth") ?: false,
            elements = elements.map { it.toDomain() },
            sections = sections.map { it.toDomain() },
        )
    }

    private fun DynamicSectionDto.toDomain(): StackSection {
        require(type == "stack_section") { "Unsupported dynamic section type: $type" }
        return StackSection(
            id = id,
            orientation = properties.string("orientation") ?: "vertical",
            verticalArrangement = properties.verticalArrangement(),
            horizontalAlignment = properties.horizontalAlignment(),
            padding = properties.padding(),
            fillMaxWidth = properties.boolean("fillMaxWidth") ?: false,
            elements = elements.map { it.toDomain() },
            groups = groups.map { it.toDomain() },
        )
    }

    private fun DynamicGroupDto.toDomain(): StackGroup {
        require(type == "stack_group") { "Unsupported dynamic group type: $type" }
        return StackGroup(
            id = id,
            orientation = properties.string("orientation") ?: "vertical",
            verticalArrangement = properties.verticalArrangement(),
            horizontalArrangement = properties.horizontalArrangement(),
            horizontalAlignment = properties.horizontalAlignment(),
            verticalAlignment = properties.verticalAlignment(),
            padding = properties.padding(),
            height = properties.number("height"),
            fillMaxWidth = properties.boolean("fillMaxWidth") ?: false,
            background = properties.background(),
            border = properties.border(),
            shape = properties.shape(),
            elements = elements.map { it.toDomain() },
        )
    }

    private fun DynamicElementDto.toDomain(): DynamicElement {
        val semanticRole = properties.string("semanticRole")
        val actions = actions?.toDomain() ?: emptyMap()
        return when (type) {
            "text" -> TextElement(
                id = id,
                semanticRole = semanticRole,
                text = properties.string("text").orEmpty(),
                spans = properties.spans(),
                fontSize = properties.number("fontSize"),
                fontWeight = properties.number("fontWeight")?.toInt(),
                letterSpacing = properties.number("letterSpacing"),
                lineHeight = properties.number("lineHeight"),
                color = properties.string("color"),
                textAlign = properties.string("textAlign") ?: "center",
                fillMaxWidth = properties.boolean("fillMaxWidth") ?: false,
                leading = properties.elements("leading"),
                trailing = properties.elements("trailing"),
                actions = actions,
            )
            "image" -> ImageElement(
                id = id,
                semanticRole = semanticRole,
                url = properties.string("url").orEmpty(),
                width = properties.number("width"),
                height = properties.number("height"),
                maxWidth = properties.number("maxWidth"),
                fillMaxWidth = properties.boolean("fillMaxWidth") ?: false,
                contentScale = properties.string("contentScale") ?: "fit",
                actions = actions,
            )
            "input" -> InputElement(
                id = id,
                semanticRole = semanticRole,
                placeholder = properties.string("placeholder"),
                keyboardType = properties.string("keyboardType") ?: "text",
                maxLength = properties.number("maxLength")?.toInt(),
                weight = properties.number("weight"),
                validation = validation?.toDomain(),
                binding = binding?.toDomain(),
                presentation = properties.inputPresentation(),
                actions = actions,
            )
            "button" -> ButtonElement(
                id = id,
                semanticRole = semanticRole,
                text = properties.string("text").orEmpty(),
                height = properties.number("height"),
                fillMaxWidth = properties.boolean("fillMaxWidth") ?: false,
                fontSize = properties.number("fontSize"),
                fontWeight = properties.number("fontWeight")?.toInt(),
                textColor = properties.string("textColor"),
                background = properties.background(),
                shape = properties.shape(),
                leading = properties.elements("leading"),
                trailing = properties.elements("trailing"),
                actions = actions,
            )
            "icon" -> IconElement(
                id = id,
                semanticRole = semanticRole,
                name = properties.string("name").orEmpty(),
                size = properties.number("size"),
                color = properties.string("color"),
                actions = actions,
            )
            "divider" -> DividerElement(
                id = id,
                semanticRole = semanticRole,
                orientation = properties.string("orientation") ?: "horizontal",
                width = properties.number("width"),
                height = properties.number("height"),
                thickness = properties.number("thickness") ?: 1.0,
                color = properties.string("color"),
                actions = actions,
            )
            else -> error("Unsupported dynamic element type: $type")
        }
    }

    private fun DynamicValidationDto.toDomain() = DynamicValidation(required, pattern, message)

    private fun DynamicBindingDto.toDomain() = DynamicBinding(key)

    private fun DynamicActionsDto.toDomain(): Map<String, DynamicAction> = buildMap {
        onClick?.let { put("onClick", it.toDomain()) }
        onLongClick?.let { put("onLongClick", it.toDomain()) }
        onValueChange?.let { put("onValueChange", it.toDomain()) }
        onFocus?.let { put("onFocus", it.toDomain()) }
        onSubmit?.let { put("onSubmit", it.toDomain()) }
    }

    private fun DynamicActionDto.toDomain() = DynamicAction(
        type = type,
        payload = payload.toDynamicValueMap(),
        destination = destination?.toDomain(),
    )

    private fun DynamicThemeDto.toDomain() = DynamicTheme(
        theme = theme,
        statusBar = statusBar,
        properties = properties.toDynamicValueMap(),
    )

    private fun JsonObject.toDynamicValueMap(): Map<String, DynamicValue> =
        entries.associate { (key, value) -> key to value.toDynamicValue() }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.number(key: String): Double? =
        (this[key] as? JsonPrimitive)?.doubleOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        (this[key] as? JsonPrimitive)?.booleanOrNull

    private fun JsonObject.padding(): DynamicPadding? {
        val value = this["padding"] as? JsonObject ?: return null
        return DynamicPadding(
            start = value.number("start") ?: value.number("horizontal") ?: value.number("all") ?: 0.0,
            top = value.number("top") ?: value.number("vertical") ?: value.number("all") ?: 0.0,
            end = value.number("end") ?: value.number("horizontal") ?: value.number("all") ?: 0.0,
            bottom = value.number("bottom") ?: value.number("vertical") ?: value.number("all") ?: 0.0,
        )
    }

    private fun JsonObject.verticalArrangement(): DynamicArrangement = arrangement("verticalArrangement", true)
    private fun JsonObject.horizontalArrangement(): DynamicArrangement = arrangement("horizontalArrangement", false)

    private fun JsonObject.arrangement(key: String, vertical: Boolean): DynamicArrangement {
        val value = this[key] as? JsonObject
        return when (value?.string("type")) {
            "spacedBy" -> DynamicArrangement.SpacedBy(value.number("spacing") ?: 0.0)
            "center" -> DynamicArrangement.Center
            "end" -> DynamicArrangement.End
            "bottom" -> DynamicArrangement.Bottom
            "spaceBetween" -> DynamicArrangement.SpaceBetween
            else -> if (vertical) DynamicArrangement.Top else DynamicArrangement.Start
        }
    }

    private fun JsonObject.horizontalAlignment(): DynamicArrangement = when (string("horizontalAlignment")) {
        "start" -> DynamicArrangement.Start
        "end" -> DynamicArrangement.End
        else -> DynamicArrangement.Center
    }

    private fun JsonObject.verticalAlignment(): DynamicArrangement = when (string("verticalAlignment")) {
        "top" -> DynamicArrangement.Top
        "bottom" -> DynamicArrangement.Bottom
        else -> DynamicArrangement.Center
    }

    private fun JsonObject.background(): DynamicBackground? {
        val value = this["background"] as? JsonObject ?: return null
        return when (value.string("type")) {
            "linearGradient" -> DynamicBackground.LinearGradient(
                angle = value.number("angle") ?: 0.0,
                colors = (value["colors"] as? JsonArray).orEmpty().mapNotNull { item ->
                    (item as? JsonObject)?.string("color")?.let(::DynamicGradientStop)
                },
            )
            else -> value.string("color")?.let(DynamicBackground::Solid)
        }
    }

    private fun JsonObject.border(): DynamicBorder? {
        val value = this["border"] as? JsonObject ?: return null
        val width = value.number("width") ?: return null
        val color = value.string("color") ?: return null
        return DynamicBorder(width, color)
    }

    private fun JsonObject.shape(): DynamicShape? {
        val value = this["shape"] as? JsonObject ?: return null
        if (value.string("type") != "roundedCorner") return null
        return DynamicShape.RoundedCorner(value.number("cornerRadius") ?: 0.0)
    }

    private fun JsonObject.spans(): List<DynamicTextSpan> =
        (this["spans"] as? JsonArray).orEmpty().mapNotNull { item ->
            val value = item as? JsonObject ?: return@mapNotNull null
            DynamicTextSpan(
                text = value.string("text").orEmpty(),
                color = value.string("color"),
                fontWeight = value.number("fontWeight")?.toInt(),
                underline = value.boolean("underline") ?: false,
                action = (value["action"] as? JsonObject ?: value["onClick"] as? JsonObject)?.toAction(),
            )
        }

    private fun JsonObject.elements(key: String): List<DynamicElement> =
        (this[key] as? JsonArray).orEmpty().mapNotNull { item ->
            val value = item as? JsonObject ?: return@mapNotNull null
            val type = value.string("type") ?: return@mapNotNull null
            val id = value.string("id") ?: "inline-$type"
            DynamicElementDto(
                id = id,
                type = type,
                properties = value["properties"] as? JsonObject ?: JsonObject(emptyMap()),
                validation = value["validation"]?.let { json.decodeFromJsonElement(com.carbroz.cbpartner.data.dynamic.model.DynamicValidationDto.serializer(), it) },
                binding = value["binding"]?.let { json.decodeFromJsonElement(com.carbroz.cbpartner.data.dynamic.model.DynamicBindingDto.serializer(), it) },
                actions = value["actions"]?.let { json.decodeFromJsonElement(com.carbroz.cbpartner.data.dynamic.model.DynamicActionsDto.serializer(), it) },
            ).toDomain()
        }

    private fun JsonObject.toAction(): DynamicAction? {
        val type = string("type") ?: return null
        return DynamicAction(type = type, payload = (this["payload"] as? JsonObject)?.toDynamicValueMap() ?: emptyMap())
    }

    private fun JsonObject.inputPresentation(): DynamicInputPresentation? {
        val value = this["presentation"] as? JsonObject ?: return null
        return DynamicInputPresentation(
            type = value.string("type") ?: return null,
            count = value.number("count")?.toInt(),
            spacing = value.number("spacing"),
            segmentWidth = value.number("segmentWidth"),
            segmentHeight = value.number("segmentHeight"),
        )
    }

    private fun JsonElement.toDynamicValue(): DynamicValue = when (this) {
        JsonNull -> DynamicValue.NullValue
        is JsonObject -> DynamicValue.ObjectValue(entries.associate { (key, value) -> key to value.toDynamicValue() })
        is JsonArray -> DynamicValue.ArrayValue(map { it.toDynamicValue() })
        else -> {
            val primitive = this as? JsonPrimitive
            when {
                primitive?.booleanOrNull != null -> DynamicValue.BooleanValue(primitive.booleanOrNull!!)
                primitive?.doubleOrNull != null -> DynamicValue.NumberValue(primitive.doubleOrNull!!)
                primitive?.contentOrNull != null -> DynamicValue.StringValue(primitive.content)
                else -> DynamicValue.NullValue
            }
        }
    }

    private fun JsonArray.orEmpty(): JsonArray = this

    private companion object {
        val SCREEN_FIELDS = setOf("screenId", "schemaVersion", "targetApp", "template")
    }
}
