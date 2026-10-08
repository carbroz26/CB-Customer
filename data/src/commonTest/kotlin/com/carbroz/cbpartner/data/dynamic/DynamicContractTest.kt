package com.carbroz.cbpartner.data.dynamic

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

class DynamicContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = DynamicResponseMapper(json)

    @Test
    fun genericScreen_preservesHierarchyElementsContentBindingsValidationAndActions() {
        val response = mapper.map(
            json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON),
        )
        val screen = assertNotNull(response.data)

        val elements = screen.template.components.flatMap { component ->
            component.elements +
                component.sections.flatMap { section ->
                    section.elements + section.groups.flatMap { it.elements }
                }
        }

        val fieldA = assertIs<InputElement>(elements.first { it is InputElement && it.binding?.key == "fieldA" })
        assertEquals("fieldA", fieldA.binding?.key)
        assertEquals(true, fieldA.validation?.required)

        val fieldB = assertIs<InputElement>(elements.first { it is InputElement && it.binding?.key == "fieldB" })
        assertEquals("fieldB", fieldB.binding?.key)
        assertNotNull(fieldB.validation?.pattern)

        val submit = assertIs<ButtonElement>(elements.first { it is ButtonElement && it.text == "Submit" })
        assertIs<IconContent>(submit.trailingContent.single())
        assertEquals("https://backend.example/assets/arrow-forward.png", assertIs<IconContent>(submit.trailingContent.single()).url)
        assertEquals("https://backend.example/assets/fallback.png", assertIs<IconContent>(submit.trailingContent.single()).fallbackUrl)

        val title = assertIs<TextElement>(
            screen.template.components.first { it.id == "header" }.elements
                .first { it is TextElement && it.text == "Welcome" },
        )
        assertIs<IconContent>(title.leadingContent.single())
        assertIs<DividerContent>(title.trailingContent.single())
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf(
                    "\$context" to DynamicValue.StringValue("appVersion"),
                ),
            ),
            title.spans.single().text,
        )

        val request = assertNotNull(submit.actions["onClick"])
        assertEquals("request", request.type)
        val body = assertIs<DynamicValue.ObjectValue>(request.payload["body"])
        assertEquals(
            DynamicValue.StringValue("fieldA"),
            (body.value["fieldA"] as DynamicValue.ObjectValue).value["\$binding"],
        )
        assertEquals(
            DynamicValue.StringValue("fieldB"),
            (body.value["fieldB"] as DynamicValue.ObjectValue).value["\$binding"],
        )
        assertEquals(
            DynamicValue.StringValue("deviceId"),
            (body.value["environmentId"] as DynamicValue.ObjectValue).value["\$context"],
        )
        assertEquals(
            DynamicValue.StringValue("referenceId"),
            (body.value["previous"] as DynamicValue.ObjectValue).value["\$state"],
        )
    }

    @Test
    fun concreteElementMapper_preservesNodeOwnedLayoutProperties() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"layout","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"text","type":"text","properties":{"text":"Hello","width":120,"maxWidth":240,"fillMaxWidth":true,"padding":{"horizontal":12,"vertical":4}}},{"id":"input","type":"input","properties":{"weight":0.5,"width":180,"maxWidth":220,"padding":{"all":6}},"binding":{"key":"value"}},{"id":"image","type":"image","properties":{"width":100,"height":80,"maxWidth":320,"maxHeight":180,"fillMaxWidth":true,"padding":{"all":8}}},{"id":"button","type":"button","properties":{"width":200,"maxWidth":280,"height":56,"fillMaxWidth":true,"padding":{"horizontal":16}}},{"id":"icon","type":"icon","properties":{"url":"https://backend.example/assets/edit.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":22,"padding":{"all":2}}}]}]}}}""",
        )
        val component = mapper.map(dto).data!!.template.components.single()

        val text = assertIs<TextElement>(component.elements[0])
        assertEquals(120.0, text.width)
        assertEquals(240.0, text.maxWidth)
        assertEquals(true, text.fillMaxWidth)
        assertEquals(12.0, text.padding?.start)

        val input = assertIs<InputElement>(component.elements[1])
        assertEquals(0.5, input.weight)
        assertEquals(180.0, input.width)
        assertEquals(220.0, input.maxWidth)
        assertEquals(6.0, input.padding?.top)
    }

    @Test
    fun concreteMapper_preservesLeadingAndTrailingContent() {
        val element = json.parseToJsonElement(
            """{"id":"generic_text","type":"text","properties":{"text":"Hello","leading":[{"type":"divider","properties":{"width":24,"thickness":2}}],"trailing":[{"type":"icon","properties":{"url":"https://backend.example/assets/edit.png","fallbackUrl":"https://backend.example/assets/fallback.png","size":20}}]}}""",
        ).jsonObject

        val text = com.carbroz.cbpartner.data.dynamic.mapper.TextElementMapper(
            json,
            DynamicValueMapper(),
        ).map(
            json.decodeFromJsonElement(
                com.carbroz.cbpartner.data.dynamic.model.element.TextElementDto.serializer(),
                element,
            ),
        )

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
        assertEquals(DynamicValue.StringValue("fieldB"), payload.value["field"])
        assertEquals(DynamicValue.StringValue("invalid"), payload.value["reason"])
    }
}