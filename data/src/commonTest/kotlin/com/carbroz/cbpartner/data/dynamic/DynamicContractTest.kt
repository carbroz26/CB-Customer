package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = DynamicResponseMapper()

    @Test
    fun loginContract_preservesScreenTemplateThemeBindingAndRequestAction() {
        val response = map(DynamicContractFixtures.LOGIN_JSON)
        val screen = assertNotNull(response.data)

        assertEquals(200, response.status)
        assertEquals("SUCCESS", response.code)
        assertEquals("partner_login", screen.screenId)
        assertEquals("3.0.0", screen.schemaVersion)
        assertEquals("vertical", screen.template.orientation)
        assertNotNull(screen.theme)

        val input = screen.template.components
            .flatMap { it.sections }
            .flatMap { it.groups }
            .flatMap { it.elements }
            .first { it.id == "mobile_number" }

        val mobileInput = assertIs<InputElement>(input)
        assertEquals("mobileNumber", mobileInput.binding?.key)
        assertEquals("request", mobileInput.actions["onClick"]?.type)
    }

    @Test
    fun otpContract_preservesSegmentedInputContextAndDestinationAction() {
        val response = map(DynamicContractFixtures.OTP_JSON)
        val screen = assertNotNull(response.data)

        assertEquals("partner_otp", screen.screenId)
        assertEquals("tpl_P102O", screen.template.id)

        val otp = screen.template.components
            .flatMap { it.sections }
            .flatMap { it.groups }
            .flatMap { it.elements }
            .first { it.id == "otp_code_input" }

        val otpInput = assertIs<InputElement>(otp)
        assertEquals("segmented", assertNotNull(otpInput.presentation).type)
        assertEquals("otp", otpInput.binding?.key)

        val verify = screen.template.components
            .flatMap { it.sections }
            .flatMap { it.elements }
            .first { it.id == "otp_verify_button" }

        val verifyButton = assertIs<ButtonElement>(verify)
        assertEquals("request", verifyButton.actions["onClick"]?.type)

        val phone = screen.template.components
            .flatMap { it.elements }
            .first { it.id == "otp_phone_number" }

        val phoneText = assertIs<TextElement>(phone)
        assertNotNull(phoneText.actions["onClick"]?.destination)
    }

    @Test
    fun sendOtpResponse_preservesChallengeDataAndDestination() {
        val response = map(DynamicContractFixtures.SEND_OTP_JSON)

        assertEquals("OTP_SENT", response.code)
        val payload = assertIs<DynamicValue.ObjectValue>(response.dataPayload)
        assertEquals(DynamicValue.StringValue("challenge-123"), payload.value["challengeId"])
        assertEquals(DynamicValue.NumberValue(300.0), payload.value["expiresInSeconds"])
        assertEquals(DynamicValue.BooleanValue(true), payload.value["isNewUser"])

        val destination = assertNotNull(response.destination)
        assertEquals("partner_otp", destination.screenId)
        assertEquals("tpl_P102O", destination.templateId)
        assertEquals("form_template", destination.templateType)
        assertEquals("/api/v1/partner/screen/auth_otp", destination.endpoint)
    }

    private fun map(value: String): com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse =
        mapper.map(json.decodeFromString<DynamicResponseDto>(value))
}
