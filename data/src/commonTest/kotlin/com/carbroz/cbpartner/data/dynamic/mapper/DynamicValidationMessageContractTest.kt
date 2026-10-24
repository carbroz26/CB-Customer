package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.content.ImageContent
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

/** Typed form-field mapping, including direct/profile and composite/phone controls. */
class DynamicValidationMessageContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val surface = """{"height":56,"fillMaxWidth":true,
        "background":{"color":"#FFFFFF"},
        "border":{"width":1,"color":"#CBD5E1"},
        "shape":{"type":"roundedCorner","cornerRadius":12}}"""
    private val label = """{"id":"label","type":"text",
        "properties":{"text":"First name","spacing":{"bottom":8}}}"""
    private val nameInput = """{"id":"first_name","type":"input",
        "properties":{"fillMaxWidth":true,"height":56},
        "binding":{"key":"firstName"}}"""
    private val phoneInput = """{"id":"phone","type":"input",
        "properties":{"height":56,"lineHeight":24,"padding":{"start":12},
            "validationStyle":{"background":{"color":"#FFF1F2"}}},
        "binding":{"key":"phone"},
        "validation":{"required":true,"requiredMessage":"Required","message":"Invalid",
            "clearOnEdit":true,
            "lengthMessages":[{"minLength":6,"maxLength":9,"message":"Wrong length"},
            {"minLength":11,"message":"Wrong length"}]}}"""
    private val inlineValidation = """{"binding":"phone",
        "errorBorder":{"width":1,"color":"#DC2626"},
        "messageStyle":{"fontSize":12,"lineHeight":20,"spacing":{"top":6},
        "leading":[{"type":"image","properties":{
            "source":{"type":"resource","value":"icons/error_alert_icon_01"},
            "width":16,"height":16,"contentScale":"fit","spacing":{"end":6}}}]}}"""

    @Test
    fun directFieldsCanBeReusedForFirstAndLastNameWithoutGroup() {
        for (key in listOf("firstName", "lastName")) {
            val field = parse(directField().replace("firstName", key))
            assertEquals(key, field.inputBinding)
            assertNotNull(field.controlInput)
            assertNull(field.controlGroup)
            assertEquals(56.0, field.controlSurface.height)
            assertEquals(8.0, field.label.spacing?.bottom)
            assertEquals(false, field.controlInput?.validation?.clearOnEdit ?: false)
            assertEquals(2, field.elements.size)
            assertEquals(0, field.groups.size)
        }
    }

    @Test
    fun compositePhoneMapsOneBoundInputAndConditionalMessageWithoutGroupValidation() {
        val field = parse(compositeField())
        assertNull(field.controlInput)
        val group = assertIs<StackGroup>(field.controlGroup)
        assertEquals(3, group.elements.size)
        assertEquals(1, field.groups.size)
        assertNull(group.border)
        assertNull(group.shape)
        assertEquals("phone", field.inputBinding)
        assertEquals(20.0, field.validationPresentation?.messageStyle?.lineHeight)
        val icon = assertIs<ImageContent>(
            field.validationPresentation?.messageStyle?.leadingContent?.single(),
        )
        val resource = assertIs<DynamicAssetSource.Resource>(icon.source)
        assertEquals(DynamicValue.StringValue("icons/error_alert_icon_01"), resource.value)
        assertEquals(16.0, icon.width)
        assertEquals(16.0, icon.height)
        assertEquals(6.0, icon.spacing?.end)
        assertEquals(1.0, field.validationPresentation?.errorBorder?.width)
        val input = assertIs<InputElement>(group.elements.last())
        assertEquals("Required", input.validation?.requiredMessage)
        assertEquals("Invalid", input.validation?.message)
        assertEquals(2, input.validation?.lengthMessages?.size)
        assertEquals(true, input.validation?.clearOnEdit)
        assertEquals(16.0, field.padding?.bottom)
        assertEquals(12.0, input.padding?.start)
        assertEquals(24.0, input.lineHeight)
        assertNotNull(input.validationStyle?.background)
    }

    @Test
    fun invalidDirectOrCompositeFieldsFailWithoutFallbackRendering() {
        val direct = directField()
        val invalid = listOf(
            direct.replace("[$label,$nameInput]", "[$label]"),
            direct.replace("[$label,$nameInput]", "[$label,$nameInput,$nameInput]"),
            direct.replace("\"key\":\"firstName\"", "\"key\":\"\""),
            direct.replace("\"horizontalAlignment\":\"start\"", "\"horizontalAlignment\":\"baseline\""),
            direct.replace("\"height\":56", "\"height\":0"),
            compositeField().replace("\"fontSize\":12", "\"reserveHeight\":24,\"fontSize\":12"),
            compositeField().replace("\"lineHeight\":20", "\"lineHeight\":0"),
            compositeField().replace("\"width\":16", "\"width\":0"),
            compositeField().replace("\"leading\":[", "\"unknown\":true,\"leading\":["),

            compositeField().replace("\"binding\":\"phone\"", "\"binding\":\"mismatch\""),
            compositeField().replace("\"orientation\":\"horizontal\"", "\"orientation\":\"horizontal\",\"border\":{\"width\":1,\"color\":\"#CCCCCC\"}"),
            compositeField().replace("\"orientation\":\"horizontal\"", "\"orientation\":\"horizontal\",\"validationPresentation\":$inlineValidation"),
        )
        invalid.forEachIndexed { index, sample ->
            assertFailsWith<RuntimeException>("invalid form field case $index") { parse(sample) }
        }
    }

    @Test
    fun declarativeEqualityMapsAndRejectsInvalidReferenceNamespaces() {
        val comparison = """"equals":{"value":{"${'":"countryId"},
            "expected":{"${'":"IN"}},"""}binding":"countryId"},
            "expected":{"\$literal":"IN"}},"""}literal":"IN"}},"""}binding":"countryId"},
            "expected":{"${'":"IN"}},"""}literal":"IN"}},"""
        val withEquality = phoneInput.replace(
            "\"clearOnEdit\":true,",
            comparison + "\"clearOnEdit\":true,",
        )
        val field = parse(compositeField().replace(phoneInput, withEquality))
        val input = assertIs<InputElement>(assertIs<StackGroup>(field.controlGroup).elements.last())
        val equality = assertNotNull(input.validation?.equals)
        assertEquals(DynamicValue.ObjectValue(mapOf(
            "\$binding" to DynamicValue.StringValue("countryId"),
        )), equality.value)
        assertEquals(DynamicValue.ObjectValue(mapOf(
            "\$literal" to DynamicValue.StringValue("IN"),
        )), equality.expected)
        val invalid = listOf(
            comparison.replace("\$binding", "\$response"),
            comparison.replace("countryId", ""),
            comparison.replace("{\"\$binding\":\"countryId\"}",
                "{\"\$binding\":\"countryId\",\"\$state\":\"x\"}"),
            comparison.replace("\"expected\":{\"\$literal\":\"IN\"}",
                "\"expected\":{\"\$config\":\"key\"}"),
        )
        invalid.forEach { malformed ->
            assertFailsWith<IllegalArgumentException> {
                parse(compositeField().replace(phoneInput,
                    withEquality.replace(comparison, malformed)))
            }
        }
    }

    @Test
    fun fieldlessGroupRejectsHistoricalPresentationFields() {
        val historical = compositeField().replace(
            "\"orientation\":\"horizontal\"",
            "\"orientation\":\"horizontal\",\"focusPresentation\":{\"binding\":\"phone\",\"border\":{\"width\":1,\"color\":\"#000000\"}}",
        )
        assertFailsWith<IllegalArgumentException> { parse(historical) }
    }

    private fun directField() = """{"id":"field","type":"form_field_section",
        "properties":{"horizontalAlignment":"start","fillMaxWidth":true,
            "controlSurface":$surface},
        "elements":[$label,$nameInput]}"""

    private fun compositeField() = """{"id":"field","type":"form_field_section",
        "properties":{"horizontalAlignment":"start","fillMaxWidth":true,
            "padding":{"bottom":16},
            "controlSurface":$surface,
            "focusPresentation":{"binding":"phone","border":{"width":1,"color":"#0A3A8D"}},
            "validationPresentation":$inlineValidation},
        "elements":[$label],
        "groups":[{"id":"row","type":"stack_group",
            "properties":{"orientation":"horizontal","fillMaxWidth":true},
            "elements":[{"id":"selector","type":"text","properties":{"text":"+91"}},
                {"id":"divider","type":"divider","properties":{
                    "orientation":"vertical","height":28,"thickness":1,"color":"#D1D5DB"}},
                $phoneInput]}]}"""

    private fun parse(sectionJson: String): FormFieldSection {
        val raw = """{"status":200,"code":"SUCCESS","message":"OK","data":{
            "screenId":"screen","schemaVersion":"3.0.0","targetApp":"PARTNER",
            "template":{"id":"tpl","type":"form_template","components":[{
                "id":"form","type":"stack_component","sections":[$sectionJson]}]}}}"""
        val mapped = DynamicResponseMapper(json).map(json.decodeFromString<DynamicResponseDto>(raw))
        val screen = assertNotNull(mapped.data)
        val component = assertIs<StackComponent>(
            assertIs<FormTemplate>(screen.template).components.single(),
        )
        return assertIs<FormFieldSection>(component.sections.single())
    }
}
