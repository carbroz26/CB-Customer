package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.ui.Alignment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    fun unsupportedPositionDoesNotSilentlyUseFrontendDefault() {
        assertFailsWith<IllegalArgumentException> {
            templateBackgroundAlignment("middleCustom")
        }
    }
}
