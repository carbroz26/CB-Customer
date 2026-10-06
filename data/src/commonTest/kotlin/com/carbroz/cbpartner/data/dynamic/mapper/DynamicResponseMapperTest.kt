package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement
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
    fun mapsDynamicResponseTreeActionPayloadAndDestinations() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            DynamicContractFixtures.GENERIC_DESTINATION_RESPONSE_JSON,
        )

        val response = DynamicResponseMapper().map(dto)
        val screen = assertNotNull(response.data)
        val button = screen.template.components.single().sections.single().groups.single().elements.single()
        val action = assertNotNull(button.actions["onClick"])
        val actionDestination = assertNotNull(action.destination)
        val responseDestination = assertNotNull(response.destination)

        assertEquals("partner_login", screen.screenId)
        assertEquals("vertical", screen.template.orientation)
        assertEquals("request", action.type)
        assertEquals(DynamicValue.StringValue("/login"), action.payload["endpoint"])
        assertEquals(DynamicValue.BooleanValue(true), action.payload["enabled"])
        assertEquals("partner_otp", actionDestination.screenId)
        assertEquals("tpl_otp", actionDestination.templateId)
        assertEquals("form_template", actionDestination.templateType)
        assertEquals("/otp", actionDestination.endpoint)
        assertEquals("partner_dashboard", responseDestination.screenId)
        assertEquals("tpl_dashboard", responseDestination.templateId)
        assertEquals("dashboard_template", responseDestination.templateType)
        assertEquals("/api/v1/partner/screen/dashboard", responseDestination.endpoint)
    }

    @Test
    fun preservesArbitraryResponseDataForChainedActions() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"OTP_SENT","message":"OTP sent","data":{"challengeId":"challenge-1","expiresIn":60},"traceId":"trace-1"}""",
        )
        val response = DynamicResponseMapper().map(dto)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("challenge-1"), payload.value["challengeId"])
        assertEquals(DynamicValue.NumberValue(60.0), payload.value["expiresIn"])
        assertEquals(null, response.data)
        assertEquals(null, response.destination)
    }

    @Test
    fun mapsAllKnownTypedUiModels() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"typed","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","sections":[{"id":"section","type":"stack_section","groups":[{"id":"group","type":"stack_group","elements":[{"id":"text","type":"text","properties":{"text":"Hello"}},{"id":"image","type":"image","properties":{"url":"https://example.com/a.png"}},{"id":"input","type":"input","properties":{"keyboardType":"phone","maxLength":10},"binding":{"key":"phone"}},{"id":"button","type":"button","properties":{"text":"Continue"}},{"id":"icon","type":"icon","properties":{"name":"arrow_forward","size":20}},{"id":"divider","type":"divider","properties":{"orientation":"horizontal","thickness":1}}]}]}]}]}}}""",
        )
        val screen = assertNotNull(DynamicResponseMapper().map(dto).data)
        assertIs<FormTemplate>(screen.template)
        assertIs<StackComponent>(screen.template.components.single())
        assertIs<StackSection>(screen.template.components.single().sections.single())
        assertIs<StackGroup>(screen.template.components.single().sections.single().groups.single())
        val elements = screen.template.components.single().sections.single().groups.single().elements
        assertIs<TextElement>(elements[0])
        assertIs<ImageElement>(elements[1])
        assertIs<InputElement>(elements[2])
        assertIs<ButtonElement>(elements[3])
        assertIs<IconElement>(elements[4])
        assertIs<DividerElement>(elements[5])
    }

    @Test
    fun rejectsUnknownUiElementType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"unknown","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"unknown","type":"future_element"}]}]}}}""",
        )
        assertFailsWith<IllegalStateException> { DynamicResponseMapper().map(dto) }
    }
}