package com.carbroz.cbpartner.data.dynamic

import com.carbroz.cbpartner.data.bootstrap.mapper.BootstrapMapper
import com.carbroz.cbpartner.data.bootstrap.model.BootstrapResponseDto
import com.carbroz.cbpartner.data.dynamic.mapper.DynamicResponseMapper
import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicBackendFlowContractTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val dynamicMapper = DynamicResponseMapper()

    @Test
    fun bootstrapLoginSendOtpAndOtpContractsFormOneContinuousFlow() {
        val bootstrap = BootstrapMapper().map(
            json.decodeFromString<BootstrapResponseDto>(
                """{"status":200,"code":"SUCCESS","message":"Partner bootstrap completed","data":{"config":{"version":"1","maintenance":{"enabled":false,"title":null,"message":null},"update":{"required":false,"optional":false,"minimumVersion":"1.0.0","latestVersion":"1.0.0","storeUrl":null},"features":{"registrationEnabled":true,"individualPartnerEnabled":true,"organizationPartnerEnabled":true}},"startup":{"authenticated":false,"destination":{"screenId":"partner_login","templateId":"tpl_P101L","templateType":"form_template","endpoint":"/api/v1/partner/screen/auth_login"}}},"traceId":"req-bootstrap"}""",
            ),
        ).getOrThrow()

        assertEquals("partner_login", bootstrap.destination.screenId)
        assertEquals("tpl_P101L", bootstrap.destination.templateId)

        val login = assertNotNull(
            dynamicMapper.map(json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.LOGIN_JSON)).data,
        )
        assertEquals(bootstrap.destination.screenId, login.screenId)
        assertEquals(bootstrap.destination.templateId, login.template.id)

        val continueAction = login.template.components
            .flatMap { it.sections }
            .flatMap { it.elements }
            .first { it.id == "continue_button" }
            .actions["onClick"]
        assertEquals("request", continueAction?.type)
        assertEquals(
            DynamicValue.StringValue("/api/v1/partner/auth/send_otp"),
            continueAction?.payload?.get("endpoint"),
        )

        val sendOtp = dynamicMapper.map(
            json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.SEND_OTP_JSON),
        )
        val otpDestination = assertNotNull(sendOtp.destination)
        assertEquals("partner_otp", otpDestination.screenId)
        assertEquals("tpl_P102O", otpDestination.templateId)
        assertEquals(
            DynamicValue.StringValue("challenge-123"),
            assertIs<DynamicValue.ObjectValue>(sendOtp.dataPayload).value["challengeId"],
        )

        val otp = assertNotNull(
            dynamicMapper.map(json.decodeFromString<DynamicResponseDto>(DynamicContractFixtures.OTP_JSON)).data,
        )
        assertEquals(otpDestination.screenId, otp.screenId)
        assertEquals(otpDestination.templateId, otp.template.id)

        val verifyAction = otp.template.components
            .flatMap { it.sections }
            .flatMap { it.elements }
            .first { it.id == "otp_verify_button" }
            .actions["onClick"]

        assertEquals("request", verifyAction?.type)
        assertEquals(
            DynamicValue.StringValue("/api/v1/partner/auth/verify_otp"),
            verifyAction?.payload?.get("endpoint"),
        )
    }
}
