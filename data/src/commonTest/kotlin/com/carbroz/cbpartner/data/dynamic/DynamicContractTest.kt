package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.InputElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val mapper = DynamicResponseMapper()

    @Test
    fun loginContract_preservesScreenTemplateThemeBindingAndRequestAction() {
        val response = map(loginJson)
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
        assertEquals("mobileNumber", input.binding?.key)
        assertEquals("request", input.actions["onClick"]?.type)
    }

    @Test
    fun otpContract_preservesSegmentedInputContextAndDestinationAction() {
        val response = map(otpJson)
        val screen = assertNotNull(response.data)
        assertEquals("partner_otp", screen.screenId)
        val otp = screen.template.components
            .flatMap { it.sections }
            .flatMap { it.groups }
            .flatMap { it.elements }
            .first { it.id == "otp_code_input" }
        val presentation = assertNotNull(otp.presentation)
        assertEquals("segmented", presentation.type)
        assertEquals("otp", otp.binding?.key)
        val verify = screen.template.components
            .flatMap { it.sections }
            .flatMap { it.elements }
            .first { it.id == "otp_verify_button" }
        assertEquals("request", verify.actions["onClick"]?.type)
        val phone = screen.template.components
            .flatMap { it.elements }
            .first { it.id == "otp_phone_number" }
        assertNotNull(phone.actions["onClick"]?.destination)
    }

    private fun map(value: String) = mapper.map(json.decodeFromString<DynamicResponseDto>(value))

    private companion object {
        const val loginJson = """
        {"status":200,"code":"SUCCESS","message":"Partner login screen fetched successfully.","data":{"screenId":"partner_login","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_P101L","type":"form_template","properties":{"orientation":"vertical","fillMaxSize":true},"components":[{"id":"login_content","type":"stack_component","sections":[{"id":"mobile_field_section","type":"stack_section","groups":[{"id":"mobile_field","type":"stack_group","elements":[{"id":"mobile_number","type":"input","properties":{"keyboardType":"phone","maxLength":10},"validation":{"required":true,"pattern":"^[6-9][0-9]{9}$","message":"Enter a valid 10-digit mobile number"},"binding":{"key":"mobileNumber"}}]}]}],"elements":[]}],"theme":{"theme":"light","statusBar":"transparent","properties":{"gradient":{"type":"linear","angle":135,"colors":[]}}}},"traceId":"req-7"}
        """

        const val otpJson = """
        {"status":200,"code":"SUCCESS","message":"Partner OTP screen fetched successfully.","data":{"screenId":"partner_otp","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_P102O","type":"form_template","components":[{"id":"otp_brand_content","type":"stack_component","elements":[{"id":"otp_phone_number","type":"text","actions":{"onClick":{"type":"navigate","destination":{"screenId":"partner_login","templateId":"tpl_P101L","templateType":"form_template","endpoint":"/api/v1/partner/screen/auth_login"}}}]},{"id":"otp_content","type":"stack_component","sections":[{"id":"otp_field_section","type":"stack_section","groups":[{"id":"otp_fields_group","type":"stack_group","elements":[{"id":"otp_code_input","type":"input","properties":{"keyboardType":"number","maxLength":6,"presentation":{"type":"segmented","count":6}},"binding":{"key":"otp"}}]},{"id":"otp_action_section","type":"stack_section","elements":[{"id":"otp_verify_button","type":"button","actions":{"onClick":{"type":"request","payload":{"method":"POST","endpoint":"/api/v1/partner/auth/verify_otp","responseMode":"destination"}}}}]}]}]}],"theme":{"theme":"light","properties":{}}},"traceId":"req-6"}
        """
    }
}
