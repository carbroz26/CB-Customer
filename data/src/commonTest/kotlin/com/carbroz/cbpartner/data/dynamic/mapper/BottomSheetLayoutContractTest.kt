package com.carbroz.cbpartner.data.dynamic.mapper

import com.carbroz.cbpartner.data.dynamic.model.response.DynamicResponseDto
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicShape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

/** Mapping regression for backend-authored generic bounded bottom-sheet layouts. */
class BottomSheetLayoutContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun mapsBoundedWhiteRoundedSheetAndFullscreenChildComponent() {
        val presentation = assertIs<BottomSheetPresentation>(parse(
            """{"heightFraction":0.7,"background":{"color":"#FFFFFF"},
               "shape":{"type":"roundedCorner","topStart":24,"topEnd":24,
                        "bottomStart":0,"bottomEnd":0}}""",
            """{"fillMaxWidth":true,"fillMaxHeight":true}""",
        ))
        assertEquals(0.7, presentation.heightFraction)
        assertEquals(DynamicBackground.Solid("#FFFFFF"), presentation.background)
        assertEquals(
            DynamicShape.RoundedCorner(
                topStart = 24.0, topEnd = 24.0, bottomStart = 0.0, bottomEnd = 0.0,
            ),
            presentation.shape,
        )
        val component = assertIs<StackComponent>(presentation.components.single())
        assertEquals(true, component.fillMaxHeight)
        assertEquals(true, component.fillMaxWidth)
    }

    @Test
    fun defaultUnboundedPresentationRemainsBackwardCompatible() {
        val presentation = assertIs<BottomSheetPresentation>(parse("{}","{}"))
        assertNull(presentation.heightFraction)
        assertNull(presentation.background)
        assertNull(presentation.shape)
        assertEquals(false, assertIs<StackComponent>(presentation.components.single()).fillMaxHeight)
    }

    @Test
    fun rejectsUnsafeSheetHeightFractions() {
        for (invalid in listOf("-1", "0", "1.01", "200")) {
            assertFailsWith<IllegalArgumentException>(invalid) {
                parse("""{"heightFraction":$invalid}""","{}")
            }
        }
    }

    private fun parse(properties: String, component: String): com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation {
        val response = json.decodeFromString<DynamicResponseDto>(
            """{"status":200,"code":"SUCCESS","message":"OK","data":{
               "screenId":"sample","schemaVersion":"3.0.0","targetApp":"PARTNER",
               "template":{"id":"tpl_sample","type":"form_template"},
               "presentations":[{"id":"picker","type":"bottom_sheet",
                  "properties":$properties,
                  "components":[{"id":"surface","type":"stack_component","properties":$component}]
               }]
            }}""",
        )
        return requireNotNull(DynamicResponseMapper(json).map(response).data).presentations.single()
    }
}
