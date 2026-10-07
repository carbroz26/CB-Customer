package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.DynamicContractFixtures
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
import com.carbroz.cbpartner.domain.model.dynamic.content.IconContent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicResponseMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsGenericScreenHierarchyAllElementsContentBindingsValidationAndActions() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_SCREEN_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        val screen = assertNotNull(response.data)
        val template = assertIs<FormTemplate>(screen.template)

        assertEquals("screen_generic", screen.screenId)
        assertEquals("3.0.0", screen.schemaVersion)
        assertEquals("PARTNER", screen.targetApp)
        assertEquals("tpl_generic", template.id)
        assertEquals("vertical", template.orientation)
        assertEquals(true, template.fillMaxSize)
        assertEquals(2, template.components.size)

        val header = template.components[0]
        assertIs<StackComponent>(header)
        assertEquals("header", header.id)
        assertEquals(3, header.elements.size)
        assertEquals(0, header.sections.size)

        val title = assertIs<TextElement>(header.elements[0])
        assertEquals("Welcome", title.text)
        assertEquals(1, title.spans.size)
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$context" to DynamicValue.StringValue("appVersion")),
            ),
            title.spans.single().text,
        )
        assertIs<IconContent>(title.leadingContent.single())
        assertIs<DividerContent>(title.trailingContent.single())
        assertEquals("navigate", title.actions["onClick"]?.type)

        val hero = assertIs<ImageElement>(header.elements[1])
        assertEquals("https://example.com/hero.png", hero.url)
        assertIs<IconContent>(hero.leadingContent.single())
        assertIs<IconContent>(hero.trailingContent.single())

        val icon = assertIs<IconElement>(header.elements[2])
        assertEquals("settings", icon.name)
        assertEquals("toolbar", icon.semanticRole)

        val form = template.components[1]
        assertEquals("form", form.id)
        assertEquals(0, form.elements.size)
        assertEquals(2, form.sections.size)

        val identity = form.sections[0]
        assertIs<StackSection>(identity)
        assertEquals("identity", identity.id)
        assertEquals(2, identity.elements.size)

        val fieldA = assertIs<InputElement>(identity.elements[0])
        val fieldB = assertIs<InputElement>(identity.elements[1])
        assertEquals("fieldA", fieldA.binding?.key)
        assertEquals("fieldB", fieldB.binding?.key)
        assertEquals(true, fieldA.validation?.required)
        assertEquals("^[^@]+@[^@]+\\.[^@]+$", fieldB.validation?.pattern)

        val actions = form.sections[1]
        assertEquals("actions", actions.id)
        assertEquals(0, actions.elements.size)
        assertEquals(1, actions.groups.size)

        val group = assertIs<StackGroup>(actions.groups.single())
        assertEquals("primary_group", group.id)
        val submit = assertIs<ButtonElement>(group.elements.single())
        assertEquals("Submit", submit.text)
        assertIs<IconContent>(submit.trailingContent.single())

        val request = assertNotNull(submit.actions["onClick"])
        assertEquals("request", request.type)
        val body = assertIs<DynamicValue.ObjectValue>(request.payload["body"])
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$binding" to DynamicValue.StringValue("fieldA")),
            ),
            body.value["fieldA"],
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$binding" to DynamicValue.StringValue("fieldB")),
            ),
            body.value["fieldB"],
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$context" to DynamicValue.StringValue("deviceId")),
            ),
            body.value["environmentId"],
        )
        assertEquals(
            DynamicValue.ObjectValue(
                mapOf("\$state" to DynamicValue.StringValue("referenceId")),
            ),
            body.value["previous"],
        )
    }

    @Test
    fun mapsArbitraryResponsePayloadWithoutTreatingItAsAScreen() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_RESPONSE_PAYLOAD_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        assertEquals(null, response.data)

        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("ref-123"), payload.value["referenceId"])
        assertEquals(DynamicValue.NumberValue(3.0), payload.value["count"])
        assertEquals(DynamicValue.BooleanValue(true), payload.value["enabled"])
        assertIs<DynamicValue.ArrayValue>(payload.value["items"])
        assertIs<DynamicValue.ObjectValue>(payload.value["nested"])
    }

    @Test
    fun mapsDestinationIndependentlyFromScreenPayload() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_RESPONSE_WITH_DESTINATION_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        val destination = assertNotNull(response.destination)
        assertEquals("screen_current", assertNotNull(response.data).screenId)
        assertEquals("screen_next", destination.screenId)
        assertEquals("tpl_next", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/next", destination.endpoint)
    }

    @Test
    fun preservesErrorResponsePayload() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_ERROR_JSON,
        )

        val response = DynamicResponseMapper(json).map(dto)
        assertEquals(422, response.status)
        assertEquals("VALIDATION_ERROR", response.code)
        assertEquals("Invalid input", response.message)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("email"), payload.value["field"])
    }

    @Test
    fun rejectsUnknownUiElementType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"unknown","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"unknown","type":"future_element"}]}]}}}""",
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper().map(dto)
        }
    }

    @Test
    fun rejectsUnsupportedContainerTypes() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"future_component"}]}}}""",
        )

        assertFailsWith<IllegalArgumentException> {
            DynamicResponseMapper().map(dto)
        }
    }

    @Test
    fun rejectsUnknownElementContentType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"button","type":"button","properties":{"leading":[{"type":"future_content"}]}}]}]}}}""",
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper().map(dto)
        }
    }
}
