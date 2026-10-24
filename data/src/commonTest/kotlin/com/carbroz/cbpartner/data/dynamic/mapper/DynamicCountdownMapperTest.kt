package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicCountdownMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun map(countdowns: String) = DynamicResponseMapper(json).map(
        json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{
            "screenId":"screen","schemaVersion":"3.0.0","targetApp":"PARTNER",
            "countdowns":$countdowns,
            "template":{"id":"tpl","type":"form_template",
              "components":[{"id":"content","type":"stack_component","elements":[]}]}
            }}""",
        ),
    )

    @Test
    fun mapsDurationReferencesWithoutRenderingSpecificElement() {
        val result = map("""[{"id":"resend","durationSeconds":{"${'$'}state":"resendAfterSeconds"}}]""")
        val countdown = assertNotNull(result.data).countdowns.single()
        assertEquals("resend", countdown.id)
        assertEquals("resendAfterSeconds", countdown.durationStatePath)
    }

    @Test
    fun rejectsInvalidCountdownSourcesAndDuplicateIds() {
        assertFailsWith<IllegalArgumentException> {
            map("""[{"id":"resend","durationSeconds":{"${'$'}binding":"duration"}}]""")
        }
        assertFailsWith<IllegalArgumentException> {
            map("""[{"id":"resend","durationSeconds":{"${'$'}state":"duration"}},
                    {"id":"resend","durationSeconds":{"${'$'}state":"duration"}}]""")
        }
        assertFailsWith<IllegalArgumentException> {
            map("""[{"id":"","durationSeconds":{"${'$'}state":"duration"}}]""")
        }
    }

    @Test
    fun screensWithoutCountdownDeclarationRemainUnchanged() {
        assertEquals(emptyList(), map("[]").data?.countdowns)
    }
}
