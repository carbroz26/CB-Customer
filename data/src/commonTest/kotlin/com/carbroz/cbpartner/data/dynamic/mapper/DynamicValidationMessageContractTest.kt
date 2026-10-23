package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class DynamicValidationMessageContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsDistinctMessagesAndReservedErrorRow() {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"test","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"form","type":"form_template","components":[{"id":"form","type":"stack_component","sections":[{"id":"field","type":"stack_section","groups":[{"id":"row","type":"stack_group","properties":{"validationPresentation":{"binding":"phone","errorBorder":{"width":1,"color":"#DC2626"},"messageStyle":{"fontSize":12,"spacing":{"top":6},"reserveHeight":24}}},"elements":[{"id":"phone","type":"input","binding":{"key":"phone"},"validation":{"required":true,"requiredMessage":"Required","message":"Invalid","lengthMessages":[{"minLength":6,"maxLength":9,"message":"Wrong length"},{"minLength":11,"message":"Wrong length"}]}}]}]}]}]}}}"""
        val screen = parse(raw)
        val form = assertIs<FormTemplate>(screen.template)
        val section = assertIs<StackSection>(
            assertIs<StackComponent>(form.components.single()).sections.single())
        val group = assertIs<StackGroup>(section.groups.single())
        assertEquals(24.0, group.validationPresentation?.messageStyle?.reserveHeight)
        val v = assertNotNull(assertIs<InputElement>(group.elements.single()).validation)
        assertEquals("Required", v.requiredMessage)
        assertEquals("Invalid", v.message)
        assertEquals(2, v.lengthMessages.size)
        assertEquals(6, v.lengthMessages[0].minLength)
        assertEquals(11, v.lengthMessages[1].minLength)
    }

    @Test
    fun rejectsInvalidLengthRangeAndZeroReservation() {
        for (rule in listOf("""{"message":"bad"}""",
            """{"minLength":10,"maxLength":9,"message":"bad"}""",
            """{"minLength":0,"message":""}""")) {
            assertFailsWith<IllegalArgumentException> {
                parse("""{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"test","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"t","type":"form_template","components":[{"id":"c","type":"stack_component","elements":[{"id":"i","type":"input","validation":{"lengthMessages":[$rule]}}]}]}}}""")
            }
        }
        assertFailsWith<IllegalArgumentException> {
            parse("""{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"test","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"t","type":"form_template","components":[{"id":"c","type":"stack_component","sections":[{"id":"s","type":"stack_section","groups":[{"id":"g","type":"stack_group","properties":{"validationPresentation":{"binding":"phone","errorBorder":{"width":1,"color":"#DC2626"},"messageStyle":{"reserveHeight":0}}}}]}]}]}}}""")
        }
    }

    private fun parse(raw: String) =
        assertNotNull(DynamicResponseMapper(json).map(json.decodeFromString<DynamicResponseDto>(raw)).data)
}
