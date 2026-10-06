package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.DynamicContractFixtures
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.DividerContent
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
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"typed","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","sections":[{"id":"section","type":"stack_section","groups":[{"id":"group","type":"stack_group","elements":[{"id":"text","type":"text","properties":{"text":"Hello"}},{"id":"image","type":"image","properties":{"url":"https://example.com/a.png"}},{"id":"input","type":"input","properties":{"keyboardType":"phone","maxLength":10,"leading":[{"type":"divider","properties":{"orientation":"horizontal","thickness":1}}]},"binding":{"key":"phone"}},{"id":"button","type":"button","properties":{"text":"Continue"}},{"id":"icon","type":"icon","properties":{"name":"arrow_forward","size":20}}]}]}]}]}}}""",
        )
        val screen = assertNotNull(DynamicResponseMapper().map(dto).data)
        assertIs<FormTemplate>(screen.template)
        assertIs<StackComponent>(screen.template.components.single())
        assertIs<StackSection>(screen.template.components.single().sections.single())
        assertIs<StackGroup>(screen.template.components.single().sections.single().groups.single())
        val elements = screen.template.components.single().sections.single().groups.single().elements
        assertIs<TextElement>(elements[0])
        assertIs<ImageElement>(elements[1])
        val input = assertIs<InputElement>(elements[2])
        assertIs<DividerContent>(input.leadingContent.single())
        assertIs<ButtonElement>(elements[3])
        assertIs<IconElement>(elements[4])
    }

    @Test
    fun mapsBootstrapDestinationToLogin() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.BOOTSTRAP_JSON)
        val response = DynamicResponseMapper().map(dto)
        val destination = assertNotNull(response.destination)
        assertEquals("partner_login", destination.screenId)
        assertEquals("tpl_P101L", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/api/v1/partner/screen/auth_login", destination.endpoint)
        assertEquals(null, response.data)
    }

    @Test
    fun mapsPartnerLoginContractIncludingBindingValidationActionsAndNestedContent() {
        val dto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.LOGIN_JSON)
        val response = DynamicResponseMapper().map(dto)
        val screen = assertNotNull(response.data)
        val group = screen.template.components.single().sections.single().groups.single()
        val input = assertIs<InputElement>(group.elements.single())
        val divider = assertIs<DividerContent>(input.leadingContent.single())
        val button = screen.template.components[0].sections[1].elements.single()
        val action = assertNotNull(button.actions["onClick"])

        assertEquals("partner_login", screen.screenId)
        assertEquals("tpl_P101L", screen.template.id)
        assertEquals("form_template", screen.template.type)
        assertEquals("login_content", screen.template.components.single().id)
        assertEquals("mobile_field_section", screen.template.components.single().sections.single().id)
        assertEquals("mobile_field", group.id)
        assertEquals("mobileNumber", input.binding?.key)
        assertEquals(true, input.validation?.required)
        assertEquals("^[6-9][0-9]{9}$", input.validation?.pattern)
        assertEquals("phone", input.keyboardType)
        assertEquals(10, input.maxLength)
        assertEquals("horizontal", divider.orientation)
        assertEquals(36.0, divider.width)
        assertEquals(2.0, divider.thickness)
        assertEquals("request", action.type)
        assertEquals(DynamicValue.StringValue("/api/v1/partner/auth/send_otp"), action.payload["endpoint"])
        assertEquals(DynamicValue.BooleanValue(true), action.payload["validate"])
        val body = assertIs<DynamicValue.ObjectValue>(action.payload["body"])
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024binding" to DynamicValue.StringValue("mobileNumber"))),
            body.value["phoneNumber"],
        )
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024context" to DynamicValue.StringValue("deviceId"))),
            body.value["deviceId"],
        )
        assertEquals("destination", (action.payload["responseMode"] as? DynamicValue.StringValue)?.value)
    }

    @Test
    fun mapsSendOtpAndOtpVerifyContracts() {
        val sendDto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.SEND_OTP_JSON)
        val sendResponse = DynamicResponseMapper().map(sendDto)
        val sendPayload = assertIs<DynamicValue.ObjectValue>(sendResponse.dataPayload)
        assertEquals(DynamicValue.StringValue("challenge-123"), sendPayload.value["challengeId"])
        assertEquals(DynamicValue.NumberValue(300.0), sendPayload.value["expiresInSeconds"])
        val destination = assertIs<DynamicValue.ObjectValue>(sendPayload.value["destination"])
        assertEquals(DynamicValue.StringValue("partner_otp"), destination.value["screenId"])
        assertEquals(DynamicValue.StringValue("tpl_P102O"), destination.value["templateId"])

        val otpDto = json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.OTP_JSON)
        val otpResponse = DynamicResponseMapper().map(otpDto)
        val otpScreen = assertNotNull(otpResponse.data)
        val phoneText = assertIs<TextElement>(otpScreen.template.components.first().elements.single())
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024context" to DynamicValue.StringValue("authFlow.phoneNumber"))),
            phoneText.spans.single().text,
        )
        val otpInput = assertIs<InputElement>(
            otpScreen.template.components[1].sections.single().groups.single().elements.single(),
        )
        assertEquals("otp", otpInput.binding?.key)
        assertEquals("^[0-9]{6}$", otpInput.validation?.pattern)
        val verifyButton = otpScreen.template.components[1].sections[1].elements.single()
        val verifyAction = assertNotNull(verifyButton.actions["onClick"])
        val verifyBody = assertIs<DynamicValue.ObjectValue>(verifyAction.payload["body"])
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024response" to DynamicValue.StringValue("data.challengeId"))),
            verifyBody.value["challengeId"],
        )
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024context" to DynamicValue.StringValue("authFlow.phoneNumber"))),
            verifyBody.value["phoneNumber"],
        )
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024binding" to DynamicValue.StringValue("otp"))),
            verifyBody.value["otp"],
        )
        assertEquals(
            DynamicValue.ObjectValue(mapOf("\u0024context" to DynamicValue.StringValue("deviceId"))),
            verifyBody.value["deviceId"],
        )
    }

    @Test
    fun rejectsUnknownUiElementType() {
        val dto = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"unknown","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl","type":"form_template","components":[{"id":"component","type":"stack_component","elements":[{"id":"unknown","type":"future_element"}]}]}}}""",
        )
        assertFailsWith<IllegalStateException> { DynamicResponseMapper().map(dto) }
    }
}
