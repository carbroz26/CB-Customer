package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicDestinationDto
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.data.dynamic.model.screen.DynamicScreenDto
import com.carbroz.cbpartner.data.dynamic.model.theme.DynamicThemeDto
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.component.DynamicComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
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
        val template = templateMapper.map(dto.template)
        validateButtonIds(
            template.header?.components.orEmpty() + template.components + template.footer?.components.orEmpty(),
        )
        presentations.forEach { presentation -> validateButtonIds(presentation.components) }
        return DynamicScreen(
            screenId = dto.screenId,
            schemaVersion = dto.schemaVersion,
            targetApp = dto.targetApp,
            template = template,
            theme = dto.theme?.let(::mapTheme),
            initialBindings = values.toDynamicValueMap(dto.initialBindings),
            presentations = presentations,
        )
    }

    /** Validates only currently supported static Button nodes within one render scope. */
    private fun validateButtonIds(components: List<DynamicComponent>) {
        val ids = mutableSetOf<String>()
        fun validate(element: DynamicElement) {
            if (element is ButtonElement) {
                require(element.id.isNotBlank() && ids.add(element.id)) {
                    "Dynamic Button IDs must be nonblank and unique within their render scope"
                }
            }
        }
        components.forEach { component ->
            component.elements.forEach(::validate)
            component.sections.forEach { section ->
                section.elements.forEach(::validate)
                section.groups.forEach { group ->
                    group.elements.forEach(::validate)
                }
            }
        }
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
