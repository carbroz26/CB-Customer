package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

/** Native and Web load the same JSON through this mapper and country catalog. */
class LocalDynamicPreviewTest {

    @Test
    fun previewMayRetainMappedOtpDestinationWithoutChangingLogin() {
        val loginRaw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
            "screenId":"partner_login","schemaVersion":"3.0.0","targetApp":"PARTNER",
            "template":{"id":"tpl_P101L","type":"form_template",
                "components":[{"id":"form","type":"stack_component"}]}
        }}"""
        val otpRaw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
            "screenId":"partner_otp","schemaVersion":"3.0.0","targetApp":"PARTNER",
            "template":{"id":"tpl_P102O","type":"form_template",
                "components":[{"id":"form","type":"stack_component"}]}
        }}"""
        val login = parseLocalDynamicPreview(loginRaw)
        val withOtp = login.withNextScreenFixture(otpRaw)
        assertEquals("partner_login", login.response.data?.screenId)
        assertEquals(null, login.nextScreen)
        assertEquals("tpl_P102O", withOtp.nextScreen?.data?.template?.id)
        assertEquals("partner_login", withOtp.response.data?.screenId)
    }

    @Test
    fun previewParsesScreenAuthoredToastAndSuccessTrigger() {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
            "screenId":"partner_otp","schemaVersion":"3.0.0","targetApp":"PARTNER",
            "template":{"id":"tpl_otp","type":"form_template","components":[
                {"id":"main","type":"stack_component","elements":[{
                    "id":"resend","type":"text","properties":{"text":"Resend OTP"},
                    "actions":{"onClick":{"type":"request","payload":{
                        "endpoint":"/send","responseMode":"none",
                        "successPresentationId":"otp_resend_success"}}}}]}]},
            "presentations":[{
                "id":"otp_resend_success","type":"toast","properties":{
                    "placement":"top","durationMillis":5000,
                    "background":{"color":"#FFFFFF"},
                    "shape":{"type":"roundedCorner","cornerRadius":20},
                    "padding":{"all":12},
                    "progress":{"height":4,"trackColor":"#FFFFFF","fillColor":"#079553"}},
                "components":[{"id":"card","type":"stack_component"}]
            }]}}"""
        val preview = parseLocalDynamicPreview(raw)
        val pres = assertNotNull(preview.response.data).presentations.single()
        assertEquals("otp_resend_success", pres.id)
        val form = preview.response.data!!.template
        val element = form.components.single().elements.single()
        assertEquals(
            DynamicValue.StringValue("otp_resend_success"),
            (element as com.carbroz.cbpartner.domain.model.dynamic.element.TextElement)
                .actions["onClick"]?.payload?.get("successPresentationId"),
        )
    }

    @Test
    fun mapsTypedDynamicScreenAndSharesCountryCatalog() {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
          "screenId":"auth_login","schemaVersion":"3.0.0","targetApp":"PARTNER",
          "template":{"id":"login","type":"form_template","components":[
            {"id":"form","type":"stack_component"}]}}}"""
        val preview = parseLocalDynamicPreview(raw)
        val screen = assertNotNull(preview.response.data)
        assertEquals("auth_login", screen.screenId)
        assertEquals("form_template", screen.template.type)
        assertEquals(8, preview.countries.size)
        assertEquals(DynamicValue.StringValue("flags/in"), preview.countries[0]["flagAsset"])
        assertEquals(DynamicValue.StringValue("flags/us"), preview.countries[1]["flagAsset"])
    }

    @Test
    fun developmentPreviewRetainsNumericCooldownWithoutExposingDomainTypeToPlatformHosts() {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
          "screenId":"partner_otp","schemaVersion":"3.0.0","targetApp":"PARTNER",
          "template":{"id":"otp","type":"form_template","components":[
            {"id":"form","type":"stack_component"}]}}}"""
        val preview = parseLocalDynamicPreview(
            raw,
            initialState = mapOf("challengeId" to "local-test"),
            numericInitialState = mapOf("resendAfterSeconds" to 10.0),
        )
        assertEquals(DynamicValue.StringValue("local-test"), preview.initialState["challengeId"])
        assertEquals(DynamicValue.NumberValue(10.0), preview.initialState["resendAfterSeconds"])
    }

    @Test
    fun offlineFailureModeIsExplicitAndDefaultsOff() {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
          "screenId":"partner_otp","schemaVersion":"3.0.0","targetApp":"PARTNER",
          "template":{"id":"otp","type":"form_template","components":[
            {"id":"form","type":"stack_component"}]}}}"""
        assertEquals(false, parseLocalDynamicPreview(raw).resendFailureMode)
        assertEquals(true, parseLocalDynamicPreview(raw, resendFailureMode = true).resendFailureMode)
    }

    @Test
    fun refusesMalformedOrMissingScreenFixtures() {
        assertFails { parseLocalDynamicPreview("{}") }
        assertFails { parseLocalDynamicPreview("{not valid json") }
    }
}
