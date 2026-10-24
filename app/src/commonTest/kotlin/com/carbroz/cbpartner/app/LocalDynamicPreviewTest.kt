package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

/** Native and Web load the same JSON through this mapper and country catalog. */
class LocalDynamicPreviewTest {
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
    fun refusesMalformedOrMissingScreenFixtures() {
        assertFails { parseLocalDynamicPreview("{}") }
        assertFails { parseLocalDynamicPreview("{not valid json") }
    }
}
