package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.DynamicDestinationDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicScreenDto
import com.carbroz.cbpartner.data.dynamic.model.DynamicThemeDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTheme
import kotlinx.serialization.json.Json

class DynamicResponseMapper {
    private val json = Json { ignoreUnknownKeys = true }
    private val values = DynamicValueMapper()
    private val elements = DynamicElementMapper(json, values)
    private val structure = DynamicStructureMapper(elements, values)

    fun map(dto: DynamicResponseDto): DynamicResponse {
        val data = dto.data
        val isScreen = data is kotlinx.serialization.json.JsonObject &&
            SCREEN_FIELDS.all { field -> field in data.keys }

        val screen = data
            ?.takeIf { isScreen }
            ?.let { json.decodeFromJsonElement(DynamicScreenDto.serializer(), it) }
            ?.let(::mapScreen)

        val destination = data
            ?.let { it as? kotlinx.serialization.json.JsonObject }
            ?.get("destination")
            ?.let { it as? kotlinx.serialization.json.JsonObject }
            ?.let { json.decodeFromJsonElement(DynamicDestinationDto.serializer(), it) }
            ?.let(::mapDestination)

        val dataPayload = data
            ?.takeUnless { isScreen }
            ?.let(values::toDynamicValue)

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

    private fun mapScreen(dto: DynamicScreenDto) = DynamicScreen(
        screenId = dto.screenId,
        schemaVersion = dto.schemaVersion,
        targetApp = dto.targetApp,
        template = structure.mapTemplate(dto.template),
        theme = dto.theme?.let(::mapTheme),
    )

    private fun mapDestination(dto: DynamicDestinationDto) = DynamicDestination(
        screenId = dto.screenId,
        templateId = dto.templateId,
        templateType = dto.templateType,
        endpoint = dto.endpoint,
    )

    private fun mapTheme(dto: DynamicThemeDto) = DynamicTheme(
        theme = dto.theme,
        statusBar = dto.statusBar,
        properties = values.toDynamicValueMap(dto.properties),
    )

    private companion object {
        val SCREEN_FIELDS = setOf("screenId", "schemaVersion", "targetApp", "template")
    }
}
