package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.IconBadgeElement
import com.carbroz.cbpartner.domain.model.dynamic.group.StackGroup
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.dynamic.section.StackSection
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlinx.serialization.json.Json

class GenericDialogContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun parse(size: Int = 76, imageSize: Int = 43) =
        DynamicResponseMapper(json).map(json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":null,
            "presentation":{"id":"maintenance","type":"dialog",
            "properties":{"dismissOnBack":false,"dismissOnOutsideClick":false},
            "components":[{"id":"surface","type":"stack_component",
              "properties":{"background":{"color":"#FFFFFF"},"shape":{"type":"roundedCorner","cornerRadius":18}},
              "sections":[
                {"id":"icon","type":"stack_section","elements":[
                  {"id":"badge","type":"icon_badge","properties":{
                    "size":$size,"imageSize":$imageSize,
                    "background":{"color":"#E2F1FF"},
                    "source":{"type":"resource","value":"icons/dialog_maintenance"}}}
                ]},
                {"id":"info","type":"stack_section",
                  "properties":{"orientation":"horizontal","background":{"color":"#EAF4FF"},
                    "shape":{"type":"roundedCorner","cornerRadius":12},
                    "border":{"width":1,"color":"#DDEBFF"}},
                  "elements":[{"id":"clock","type":"icon",
                    "properties":{"source":{"type":"resource","value":"icons/clock"},"size":20}}],
                  "groups":[{"id":"lines","type":"stack_group","elements":[
                    {"id":"first","type":"text","properties":{"text":"Current version"}},
                    {"id":"second","type":"text","properties":{"text":"Required version"}}
                  ]}]
                }
              ]}]
            }}""",
        ))

    @Test
    fun mapsResponseDialogBadgeAndSupportiveSectionWithoutNewGroupNesting() {
        val dialog = assertIs<DialogPresentation>(parse().presentation)
        assertEquals(false, dialog.dismissOnBack)
        val surface = assertIs<StackComponent>(dialog.components.single())
        val badge = assertIs<IconBadgeElement>(
            assertIs<StackSection>(surface.sections.first()).elements.single(),
        )
        assertEquals(76.0, badge.size)
        assertEquals(43.0, badge.imageSize)
        val info = assertIs<StackSection>(surface.sections[1])
        assertEquals("horizontal", info.orientation)
        assertEquals(DynamicBackground.Solid("#EAF4FF"), info.background)
        assertEquals(1.0, info.border?.width)
        val group = assertIs<StackGroup>(info.groups.single())
        assertEquals(2, group.elements.size)
    }

    @Test
    fun rejectsCenterImageBiggerThanBadgeAndInvalidSizes() {
        assertFailsWith<IllegalArgumentException> { parse(size = 30, imageSize = 43) }
        assertFailsWith<IllegalArgumentException> { parse(size = 0, imageSize = 0) }
    }
}
