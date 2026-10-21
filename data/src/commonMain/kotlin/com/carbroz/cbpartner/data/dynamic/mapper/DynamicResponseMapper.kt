package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicDestinationDto
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.model.screen.DynamicScreenDto
import com.carbroz.cbpartner.data.dynamic.model.theme.DynamicThemeDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.theme.DynamicTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class DynamicResponseMapper(private val json: Json) {
    private val values = DynamicValueMapper()
    private val elementMapper = ElementMapper(json, values)
    private val groups = GroupMapper(json, elementMapper, values)
    private val sections = SectionMapper(json, elementMapper, groups, values)
    private val components = ComponentMapper(json, elementMapper, sections, values)
    private val templateMapper = TemplateMapper(json, components, values)
    private val presentationMapper = PresentationMapper(components, values)

    fun map(dto: DynamicResponseDto): DynamicResponse {
        val data = dto.data
        val isScreen = data is JsonObject && SCREEN_FIELDS.all { field -> field in data.keys }

        val screen = data?.takeIf { isScreen }
            ?.let { json.decodeFromJsonElement(DynamicScreenDto.serializer(), it) }
            ?.let(::mapScreen)

        val destination = data?.let { it as? JsonObject }
            ?.get("destination")?.let { it as? JsonObject }
            ?.let { json.decodeFromJsonElement(DynamicDestinationDto.serializer(), it) }
            ?.let(::mapDestination)

        val dataPayload = data?.takeUnless { isScreen }?.let(values::toDynamicValue)

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

    private fun mapScreen(dto: DynamicScreenDto): DynamicScreen {
        val presentations = dto.presentations.map(presentationMapper::map)
        require(presentations.map { it.id }.distinct().size == presentations.size) {
            "Dynamic presentation ids must be unique within a screen"
        }
        return DynamicScreen(
            screenId = dto.screenId,
            schemaVersion = dto.schemaVersion,
            targetApp = dto.targetApp,
            template = templateMapper.map(dto.template),
            theme = dto.theme?.let(::mapTheme),
            initialBindings = values.toDynamicValueMap(dto.initialBindings),
            presentations = presentations,
        )
    }

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
