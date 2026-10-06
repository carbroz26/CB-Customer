package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicElementMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicValueMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.parseToJsonElement

class DynamicContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = DynamicResponseMapper()

    @Test
    fun genericScreen_preservesHierarchyElementsContentBindingsValidationAndActions() {
        val response = mapper.map(
            json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON),
        )
        val screen = assertNotNull(response.data)

        assertEquals(200, response.status)
        assertEquals("SUCCESS", response.code)
        assertEquals("screen_generic", screen.screenId)
        assertEquals("3.0.0", screen.schemaVersion)
        assertEquals("vertical", screen.template.orientation)
        assertNotNull(screen.theme)

        val elements = screen.template.components
            .flatMap { it.sections }
            .flatMap { it.groups }
            .flatMap { it.elements }

        val firstName = assertIs<InputElement>(elements.first { it.id == "first_name" })
        assertEquals("firstName", firstName.binding?.key)
        assertEquals(true, firstName.validation?.required)

        val email = assertIs<InputElement>(elements.first { it.id == "email" })
        assertEquals("email", email.binding?.key)
        assertNotNull(email.validation?.pattern)

        val submit = assertIs<ButtonElement>(elements.first { it.id == "submit" })
        assertIs<IconContent>(submit.trailingContent.single())
        assertEquals("arrow_forward", assertIs<IconContent>(submit.trailingContent.single()).name)

        val title = assertIs<TextElement>(
            screen.template.components
                .first { it.id == "header" }
                .elements
                .first { it.id == "title" },
        )
        assertIs<IconContent>(title.leadingContent.single())
        assertIs<DividerContent>(title.trailingContent.single())
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf(
                    "$context" to DynamicValue.StringValue("profile.displayName"),
                ),
            ),
            title.spans.single().text,
        )

        val request = assertNotNull(submit.actions["onClick"])
        assertEquals("request", request.type)
        val body = assertIs<DynamicValue.ObjectValue>(request.payload["body"])
        assertEquals(
            DynamicValue.StringValue("firstName"),
            (body.value["firstName"] as DynamicValue.ObjectValue).value["$binding"],
        )
        assertEquals(
            DynamicValue.StringValue("email"),
            (body.value["email"] as DynamicValue.ObjectValue).value["$binding"],
        )
        assertEquals(
            DynamicValue.StringValue("session.id"),
            (body.value["session"] as DynamicValue.ObjectValue).value["$context"],
        )
        assertEquals(
            DynamicValue.StringValue("data.referenceId"),
            (body.value["previous"] as DynamicValue.ObjectValue).value["$response"],
        )
    }

    @Test
    fun universalLeadingAndTrailingContent_mapsForAnyFirstClassElement() {
        val element = json.parseToJsonElement(
            """{"id":"generic_text","type":"text","properties":{"text":"Hello","leading":[{"type":"divider","properties":{"width":24,"thickness":2}}],"trailing":[{"type":"icon","properties":{"name":"edit","size":20}}]}}""",
        ).jsonObject

        val directMapper = DynamicElementMapper(json, DynamicValueMapper())
        val text = assertIs<TextElement>(directMapper.map(element))

        assertIs<DividerContent>(text.leadingContent.single())
        assertIs<IconContent>(text.trailingContent.single())
    }

    @Test
    fun genericResponsePayload_preservesNestedDataAndDestinationIndependently() {
        val payloadResponse = mapper.map(
            json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_RESPONSE_PAYLOAD_JSON),
        )
        val payload = assertIs<DynamicValue.ObjectValue>(payloadResponse.dataPayload)
        assertEquals(DynamicValue.StringValue("ref-123"), payload.value["referenceId"])
        assertEquals(DynamicValue.NumberValue(3.0), payload.value["count"])
        assertEquals(DynamicValue.BooleanValue(true), payload.value["enabled"])

        val destinationResponse = mapper.map(
            json.decodeFromString<DynamicResponseDto>(
                DynamicContractFixtures.GENERIC_RESPONSE_WITH_DESTINATION_JSON,
            ),
        )
        val destination = assertNotNull(destinationResponse.destination)
        assertEquals("screen_next", destination.screenId)
        assertEquals("tpl_next", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/next", destination.endpoint)
    }

    @Test
    fun genericErrorResponse_preservesErrorPayloadWithoutScreenAssumptions() {
        val response = mapper.map(
            json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_ERROR_JSON),
        )

        assertEquals(422, response.status)
        assertEquals("VALIDATION_ERROR", response.code)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("email"), payload.value["field"])
        assertEquals(DynamicValue.StringValue("invalid"), payload.value["reason"])
    }
}
