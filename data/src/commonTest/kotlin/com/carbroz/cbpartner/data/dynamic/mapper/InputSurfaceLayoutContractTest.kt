package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.DividerElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicArrangement
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

/** Mapping regression for generic authored Input surface and field insets. */
class InputSurfaceLayoutContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsStartAlignedLabelAndInternalPhoneFieldInsets() {
        val response = parse(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{
                "screenId":"input_surface","schemaVersion":"3.0.0","targetApp":"PARTNER",
                "template":{"id":"tpl_surface","type":"form_template","components":[{
                    "id":"form","type":"stack_component","sections":[{
                        "id":"label_section","type":"stack_section",
                        "properties":{"horizontalAlignment":"start"},
                        "elements":[{"id":"label","type":"text","properties":{"text":"Mobile number"}}]
                    },{
                        "id":"phone_section","type":"stack_section","groups":[{
                            "id":"phone_row","type":"stack_group",
                            "properties":{"orientation":"horizontal","height":56,
                                "shape":{"type":"roundedCorner","cornerRadius":12}},
                            "elements":[{
                                "id":"divider","type":"divider","properties":{
                                    "orientation":"vertical","height":28,"thickness":1,
                                    "color":"#D1D5DB","spacing":{"end":0}}
                            },{
                                "id":"phone","type":"input","properties":{
                                    "height":56,"textAlign":"start",
                                    "padding":{"start":12,"end":12},
                                    "validationStyle":{"background":{"color":"#FFF1F2"}}
                                },"binding":{"key":"mobile"}}
                            ]
                        }]
                    }]
                }]}}}""",
        )
        val template = assertIs<FormTemplate>(assertNotNull(response.data).template)
        val form = assertIs<StackComponent>(template.components.single())
        val label = assertIs<StackSection>(form.sections[0])
        assertEquals(DynamicArrangement.Start, label.horizontalAlignment)
        val row = assertIs<StackGroup>(assertIs<StackSection>(form.sections[1]).groups.single())
        val divider = assertIs<DividerElement>(row.elements[0])
        assertEquals(0.0, divider.spacing?.end)
        val input = assertIs<InputElement>(row.elements[1])
        assertEquals("start", input.textAlign)
        assertEquals(56.0, input.height)
        assertEquals(DynamicPadding(start = 12.0, end = 12.0), input.padding)
        assertEquals(DynamicBackground.Solid("#FFF1F2"), input.validationStyle?.background)
    }

    @Test
    fun mapsSearchInputSurfaceWithLeadingContentAndInternalInsets() {
        val response = parse(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{"screenId":"search_surface","schemaVersion":"3.0.0","targetApp":"PARTNER","template":{"id":"tpl_search","type":"form_template"},"presentations":[{"id":"search","type":"bottom_sheet","components":[{"id":"picker","type":"stack_component","sections":[{"id":"search_row","type":"stack_section","elements":[{"id":"search_input","type":"input","binding":{"key":"query"},"properties":{"height":46,"fillMaxWidth":true,"textAlign":"start","padding":{"start":12,"end":12},"border":{"width":1,"color":"#D6E2F0"},"shape":{"type":"roundedCorner","cornerRadius":9},"leading":[{"type":"icon","properties":{"source":{"type":"resource","value":"icons/search"},"size":16,"spacing":{"end":9}}}]}}]}]}]}]}}""",
        )
        val screen = assertNotNull(response.data)
        val sheet = assertIs<BottomSheetPresentation>(screen.presentations.single())
        val component = assertIs<StackComponent>(sheet.components.single())
        val section = assertIs<StackSection>(component.sections.single())
        val field = assertIs<InputElement>(section.elements.single())
        assertEquals("start", field.textAlign)
        assertEquals(46.0, field.height)
        assertEquals(DynamicPadding(start = 12.0, end = 12.0), field.padding)
        assertEquals(1, field.leadingContent.size)
        assertNotNull(field.border)
        assertNotNull(field.shape)
    }

    private fun parse(raw: String) =
        DynamicResponseMapper(json).map(json.decodeFromString<DynamicResponseDto>(raw))
}
