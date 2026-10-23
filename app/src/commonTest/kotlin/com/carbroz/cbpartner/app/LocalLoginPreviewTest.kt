package com.carbroz.cbpartner.app

import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

/** Native and Web load the same JSON through this mapper and country catalog. */
class LocalLoginPreviewTest {
    @Test
    fun mapsTypedDynamicScreenAndSharesCountryCatalog() {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
          "screenId":"auth_login","schemaVersion":"3.0.0","targetApp":"PARTNER",
          "template":{"id":"login","type":"form_template","components":[
            {"id":"form","type":"stack_component"}]}}}"""
        val preview = parseLocalLoginPreview(raw)
        val screen = assertNotNull(preview.response.data)
        assertEquals("auth_login", screen.screenId)
        assertEquals("form_template", screen.template.type)
        assertEquals(8, preview.countries.size)
        assertEquals(DynamicValue.StringValue("flags/in"), preview.countries[0]["flagAsset"])
        assertEquals(DynamicValue.StringValue("flags/us"), preview.countries[1]["flagAsset"])
    }

    @Test
    fun refusesMalformedOrMissingScreenFixtures() {
        assertFails { parseLocalLoginPreview("{}") }
        assertFails { parseLocalLoginPreview("{not valid json") }
    }
}
