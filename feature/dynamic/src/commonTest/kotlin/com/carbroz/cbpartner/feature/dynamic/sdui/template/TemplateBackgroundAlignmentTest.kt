package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.ui.Alignment
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.template.TemplateSlot
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TemplateBackgroundAlignmentTest {
    @Test
    fun allBackendDefinedPositionsMapWithoutScreenConditions() {
        val supported = mapOf(
            "topStart" to Alignment.TopStart,
            "topCenter" to Alignment.TopCenter,
            "topEnd" to Alignment.TopEnd,
            "centerStart" to Alignment.CenterStart,
            "center" to Alignment.Center,
            "centerEnd" to Alignment.CenterEnd,
            "bottomStart" to Alignment.BottomStart,
            "bottomCenter" to Alignment.BottomCenter,
            "bottomEnd" to Alignment.BottomEnd,
        )
        supported.forEach { (key, expected) ->
            assertEquals(expected, templateBackgroundAlignment(key))
        }
    }

    @Test
    fun navigationBackdropUsesOnlyAuthoredBottomSurface() {
        val white = DynamicBackground.Solid("#FFFFFF")
        val gray = DynamicBackground.Solid("#E5E7EB")
        val header = StackComponent(id = "branding")
        val form = StackComponent(id = "form", background = white)
        val template = FormTemplate(
            id = "login",
            fillMaxSize = true,
            components = listOf(header, form),
        )
        assertEquals(white, template.bottomSystemBarBackground())
        assertNull(template.copy(components = listOf(header)).bottomSystemBarBackground())
        assertNull(template.copy(fillMaxSize = false).bottomSystemBarBackground())
        assertNull(template.copy(orientation = "horizontal").bottomSystemBarBackground())
        assertEquals(
            gray,
            template.copy(
                footer = TemplateSlot(components = listOf(StackComponent(id = "footer", background = gray))),
            ).bottomSystemBarBackground(),
        )
    }

    @Test
    fun unsupportedPositionDoesNotSilentlyUseFrontendDefault() {
        assertFailsWith<IllegalArgumentException> {
            templateBackgroundAlignment("middleCustom")
        }
    }
}
