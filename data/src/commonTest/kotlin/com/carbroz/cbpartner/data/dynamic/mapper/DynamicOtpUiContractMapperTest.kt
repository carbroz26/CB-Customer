package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

/** Typed source contract: all-slot validation, independent group message, completion gate. */
class DynamicOtpUiContractMapperTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsOtpPresentationWithoutParentErrorBorder() {
        val group = otpGroup(
            """{"binding":"otp","messageStyle":{"reserveSpaceWhenHidden":true,"minHeight":24,"fontSize":12}}""",
        )
        val button = """{"id":"verify","type":"button","properties":{
            "text":"Verify","enabledWhen":{"binding":"otp","length":6},
            "disabledBackground":{"color":"#BFDBFE"},"disabledTextColor":"#FFFFFF"
        }}"""
        val screen = assertNotNull(mapped(group, button))
        val component = assertIs<StackComponent>(assertIs<FormTemplate>(screen.template).components.single())
        val section = assertIs<StackSection>(component.sections.single())
        val mappedGroup = assertIs<StackGroup>(section.groups.single())
        val presentation = assertNotNull(mappedGroup.validationPresentation)
        assertNull(presentation.errorBorder)
        assertEquals(true, presentation.messageStyle?.reserveSpaceWhenHidden)
        assertEquals(24.0, presentation.messageStyle?.minHeight)
        val input = assertIs<InputElement>(mappedGroup.elements.single())
        assertEquals("—", input.presentation?.placeholder)
        assertEquals(DynamicBackground.Solid("#FFF1F2"), input.validationStyle?.background)
        assertEquals(DynamicBorder(1.5, "#DC2626"), input.validationStyle?.border)
        assertEquals(6, input.presentation?.count)
        val verify = assertIs<ButtonElement>(section.elements.single())
        assertEquals("otp", verify.enabledWhen?.binding)
        assertEquals(6, verify.enabledWhen?.length)
        assertEquals(DynamicBackground.Solid("#BFDBFE"), verify.disabledBackground)
        assertEquals("#FFFFFF", verify.disabledTextColor)
    }

    @Test
    fun legacyWholeRowPhoneErrorBorderStillMaps() {
        val group = otpGroup(
            """{"binding":"otp","errorBorder":{"width":1,"color":"#DC2626"}}""",
        )
        val screen = assertNotNull(mapped(group))
        val section = assertIs<StackSection>(
            assertIs<StackComponent>(screen.template.components.single()).sections.single(),
        )
        val mappedGroup = assertIs<StackGroup>(section.groups.single())
        assertEquals(DynamicBorder(1.0, "#DC2626"), mappedGroup.validationPresentation?.errorBorder)
    }

    private fun otpGroup(presentation: String): String =
        """{"id":"otpGroup","type":"stack_group","properties":{
            "orientation":"horizontal","validationPresentation":$presentation
        },"elements":[{"id":"otp","type":"input","properties":{
            "inputFilter":"digits","maxLength":6,"presentation":{
                "type":"segmented","count":6,"segmentWidth":56,"segmentHeight":56,
                "spacing":5,"placeholder":"—"
            },
            "validationStyle":{
                "background":{"color":"#FFF1F2"},
                "border":{"width":1.5,"color":"#DC2626"}
            }
        },"binding":{"key":"otp"},"validation":{"required":true,"pattern":"^[0-9]{6}$"}}]}"""

    private fun mapped(group: String, button: String = "") =
        DynamicResponseMapper(json).map(
            json.decodeFromString<DynamicResponseDto>(
                """{"status":200,"code":"SUCCESS","message":"OK","data":{
                    "screenId":"partner_otp","schemaVersion":"3.0.0","targetApp":"PARTNER",
                    "initialBindings":{"otp":""},
                    "template":{"id":"tpl_otp","type":"form_template","components":[{
                        "id":"screen","type":"stack_component","sections":[{
                            "id":"form","type":"stack_section",
                            "elements":[${if (button.isNotBlank()) button else ""}],
                            "groups":[$group]
                        }]
                    }]}
                }}""",
            ),
        ).data
}
