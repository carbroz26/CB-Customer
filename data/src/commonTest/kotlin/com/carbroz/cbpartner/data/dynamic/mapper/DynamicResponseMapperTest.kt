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
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicSpacing
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

        val header = assertIs<StackComponent>(template.components[0])
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
        val leadingIcon = assertIs<IconContent>(title.leadingContent.single())
        assertEquals(DynamicSpacing(1.0, 2.0, 3.0, 4.0), leadingIcon.spacing)
        val trailingDivider = assertIs<DividerContent>(title.trailingContent.single())
        assertEquals(DynamicSpacing(5.0, 6.0, 7.0, 8.0), trailingDivider.spacing)
        assertEquals("navigate", title.actions["onClick"]?.type)

        val hero = assertIs<ImageElement>(header.elements[1])
        assertEquals("https://example.com/hero.png", hero.url)
        assertIs<IconContent>(hero.leadingContent.single())
        assertIs<IconContent>(hero.trailingContent.single())

        val icon = assertIs<IconElement>(header.elements[2])
        assertEquals("https://backend.example/assets/settings.png", icon.url)
        assertEquals(
            "https://backend.example/assets/fallback.png",
            icon.fallbackUrl,
        )
        assertEquals("toolbar", icon.semanticRole)

        val form = assertIs<StackComponent>(template.components[1])
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

        val actions = assertIs<StackSection>(form.sections[1])
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
    fun mapsOptionalHeaderFooterSlotsWithoutMovingBodyComponents() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"slots","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","properties":{"padding":{"all":20}},"header":{"properties":{"padding":{"all":8}},"components":[{"id":"title","type":"stack_component","elements":[{"id":"text","type":"text","properties":{"text":"Location"}}]}]},"components":[{"id":"body","type":"stack_component","elements":[{"id":"text","type":"text","properties":{"text":"Body"}}]}],"footer":{"properties":{"padding":{"all":12}},"components":[]}}}}}""",
        )

        val template = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        assertEquals("body", assertIs<StackComponent>(template.components.single()).id)
        assertEquals(20.0, template.padding?.top)
        val header = assertNotNull(template.header)
        assertEquals(8.0, header.padding?.top)
        assertEquals("title", assertIs<StackComponent>(header.components.single()).id)
        assertEquals("Location", assertIs<TextElement>(header.components.single().elements.single()).text)
        val footer = assertNotNull(template.footer)
        assertEquals(12.0, footer.padding?.top)
        assertEquals(0, footer.components.size)
    }

    @Test
    fun existingBodyOnlyScreenHasNoHeaderOrFooterSlots() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.GENERIC_SCREEN_JSON)
        val template = assertIs<FormTemplate>(assertNotNull(DynamicResponseMapper(json).map(dto).data).template)
        assertEquals(null, template.header)
        assertEquals(null, template.footer)
        assertEquals(2, template.components.size)
    }

    @Test
    fun unknownHeaderComponentTypeFailsWithoutDroppingContent() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"slots","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"body","type":"stack_component"}],"header":{"components":[{"id":"new","type":"unsupported_component"}]}}}}""",
        )
        assertFailsWith<IllegalArgumentException> { DynamicResponseMapper(json).map(dto) }
    }

    @Test
    fun preservesNumericLookingJsonStringsAsStrings() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"phoneNumber":"+919309741971","otp":"111111","count":300}}""",
        )

        val response = DynamicResponseMapper(json).map(dto)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)

        assertEquals(
            DynamicValue.StringValue("+919309741971"),
            payload.value["phoneNumber"],
        )
        assertEquals(
            DynamicValue.StringValue("111111"),
            payload.value["otp"],
        )
        assertEquals(
            DynamicValue.NumberValue(300.0),
            payload.value["count"],
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
        assertEquals(DynamicValue.StringValue("fieldB"), payload.value["field"])
    }

    @Test
    fun rejectsUnknownUiElementType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"unknown","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"unknown","type":"future_element"}]}]}}}""",
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper(json).map(dto)
        }
    }

    @Test
    fun rejectsUnsupportedContainerTypes() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"future_component"}]}}}""",
        )

        assertFailsWith<IllegalArgumentException> {
            DynamicResponseMapper(json).map(dto)
        }
    }

    @Test
    fun rejectsUnknownElementContentType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"button","type":"button","properties":{"leading":[{"type":"future_content"}]}}]}]}}}""",
        )

        assertFailsWith<IllegalStateException> {
            DynamicResponseMapper(json).map(dto)
        }
    }

    @Test
    fun missingAssetUrlsMapToEmptyValuesForRendererFallback() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"asset_fallback","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_assets","type":"form_template","components":[{"id":"assets","type":"stack_component","elements":[{"id":"icon","type":"icon","properties":{}},{"id":"image","type":"image","properties":{}},{"id":"label","type":"text","properties":{"text":"Label","textAlign":"center","fillMaxWidth":true,"leading":[{"type":"icon","properties":{}}],"trailing":[{"type":"divider","properties":{"orientation":"vertical","height":20,"thickness":1}}]}}]}]}}}""",
        )

        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        val component = assertIs<StackComponent>(assertIs<FormTemplate>(screen.template).components.single())
        assertEquals("", assertIs<IconElement>(component.elements[0]).url)
        assertEquals("", assertIs<ImageElement>(component.elements[1]).url)
        val text = assertIs<TextElement>(component.elements[2])
        assertEquals("center", text.textAlign)
        assertEquals(true, text.fillMaxWidth)
        val leadingIcon = assertIs<IconContent>(text.leadingContent.single())
        assertEquals("", leadingIcon.url)
        assertEquals(null, leadingIcon.spacing)
        assertEquals(null, assertIs<DividerContent>(text.trailingContent.single()).spacing)
    }

    @Test
    fun categoryDispatchRejectsUnknownTemplateSectionAndGroup() {
        fun response(template: String): DynamicResponseDto = json.decodeFromString(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"invalid","schemaVersion":"3.0.0","targetApp":"PARTNER","template":$template}}""",
        )
        val templates = listOf(
            """{"id":"tpl","type":"future_template"}""",
            """{"id":"tpl","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"s","type":"future_section"}]}]}""",
            """{"id":"tpl","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"s","type":"stack_section","groups":[{"id":"g","type":"future_group"}]}]}]}""",
        )
        templates.forEach { template ->
            assertFailsWith<IllegalArgumentException> {
                DynamicResponseMapper(json).map(response(template))
            }
        }
    }

    @Test
    fun categoryDispatchPreservesMultipleComponents() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"multi","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"first","type":"stack_component"},{"id":"second","type":"stack_component"}]}}}""",
        )
        val screen = assertNotNull(DynamicResponseMapper(json).map(dto).data)
        assertEquals("tpl", screen.template.id)
        assertEquals("form_template", screen.template.type)
        assertEquals(2, screen.template.components.size)
        assertEquals("first", assertIs<StackComponent>(screen.template.components[0]).id)
        assertEquals("second", assertIs<StackComponent>(screen.template.components[1]).id)
    }
}
