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
import com.carbroz.cbpartner.domain.model.dynamic.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.DynamicGroup
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.DynamicSection
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTemplate
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTheme
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValidation
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
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
        val dataPayload = data
            ?.takeUnless { isScreen }
            ?.toDynamicValue()

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

    private fun DynamicTemplateDto.toDomain() = DynamicTemplate(
        id = id,
        type = type,
        properties = properties.toDynamicValueMap(),
        components = components.map { it.toDomain() },
    )

    private fun DynamicComponentDto.toDomain() = DynamicComponent(
        id = id,
        type = type,
        properties = properties.toDynamicValueMap(),
        elements = elements.map { it.toDomain() },
        sections = sections.map { it.toDomain() },
    )

    private fun DynamicSectionDto.toDomain() = DynamicSection(
        id = id,
        type = type,
        properties = properties.toDynamicValueMap(),
        elements = elements.map { it.toDomain() },
        groups = groups.map { it.toDomain() },
    )

    private fun DynamicGroupDto.toDomain() = DynamicGroup(
        id = id,
        type = type,
        properties = properties.toDynamicValueMap(),
        elements = elements.map { it.toDomain() },
    )

    private fun DynamicElementDto.toDomain() = DynamicElement(
        id = id,
        type = type,
        properties = properties.toDynamicValueMap(),
        validation = validation?.toDomain(),
        binding = binding?.toDomain(),
        actions = actions?.toDomain() ?: emptyMap(),
    )

    private fun DynamicValidationDto.toDomain() = DynamicValidation(
        required = required,
        pattern = pattern,
        message = message,
    )

    private fun DynamicBindingDto.toDomain() = DynamicBinding(key)

    private fun DynamicActionsDto.toDomain(): Map<String, DynamicAction> {
        val result = mutableMapOf<String, DynamicAction>()
        onClick?.let { result["onClick"] = it.toDomain() }
        onLongClick?.let { result["onLongClick"] = it.toDomain() }
        onValueChange?.let { result["onValueChange"] = it.toDomain() }
        onFocus?.let { result["onFocus"] = it.toDomain() }
        onSubmit?.let { result["onSubmit"] = it.toDomain() }
        return result
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

    private companion object {
        // Bootstrap/dynamic responses share the same unified destination model.
        val SCREEN_FIELDS = setOf("screenId", "schemaVersion", "targetApp", "template")
    }
}
