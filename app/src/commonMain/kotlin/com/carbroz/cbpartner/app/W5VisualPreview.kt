package com.carbroz.cbpartner.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.DynamicScreen
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.template.TemplateSlot
import com.carbroz.cbpartner.domain.model.dynamic.theme.DynamicTheme
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicGradientStop
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicInputPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicPadding
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.dynamicRendererRegistry
import com.carbroz.cbpartner.feature.dynamic.renderer.DynamicRenderer

/**
 * Isolated W5 visual evidence: no transport, Bootstrap, Store, DI or navigation.
 * Explicit host opt-in only; never part of normal application startup.
 */
@Composable
fun W5VisualPreview() {
    var mode by remember { mutableStateOf(0) }
    var digits by remember { mutableStateOf("1234") }
    val modes = listOf("Header + Footer", "Body only", "Empty slots")
    val fixture = remember(mode) { w5Fixture(mode) }
    val registry = remember { dynamicRendererRegistry() }

    MaterialTheme {
        Column {
            Text("W5 isolated renderer preview (no network)", modifier = Modifier.padding(8.dp))
            Row {
                modes.forEachIndexed { index, title ->
                    TextButton(onClick = { mode = index }) {
                        Text(if (mode == index) "• $title" else title)
                    }
                }
            }
            DynamicRenderer(
                registry = registry,
                bindings = mapOf("w5_digits" to DynamicValue.StringValue(digits)),
                onValueChange = { key, value ->
                    if (key == "w5_digits") digits = (value as? DynamicValue.StringValue)?.value.orEmpty()
                },
            ).Render(fixture.first, fixture.second)
        }
    }
}

private fun w5Fixture(mode: Int): Pair<DynamicResponse, DynamicDestination> {
    val templateId = "w5_tpl"
    val screenId = "w5_visual"
    val palette = listOf(
        DynamicGradientStop("#FF5E74", 0.0),
        DynamicGradientStop("#FFC857", 0.28),
        DynamicGradientStop("#46C9AD", 0.55),
        DynamicGradientStop("#5264D9", 1.0),
    )
    val gradient = DynamicBackground.LinearGradient(angle = 135.0, colors = palette)
    fun label(id: String, text: String) = TextElement(
        id = id, text = text, fontSize = 15.0, color = "#142036",
        padding = DynamicPadding(4.0, 5.0, 4.0, 5.0),
    )
    fun component(id: String, vararg elements: com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement) =
        StackComponent(id = id, elements = elements.toList(), fillMaxWidth = true)
    val body = component(
        "w5_body",
        label("body_title", "BODY — body padding 24; header/footer padding 0"),
        ButtonElement(
            id = "gradient_button", text = "Button — 135° four-stop gradient",
            height = 54.0, fillMaxWidth = true, textColor = "#142036",
            background = gradient,
        ),
        label("otp_label", "Segmented Input — same 135° four-stop gradient"),
        InputElement(
            id = "gradient_otp", binding = DynamicBinding("w5_digits"),
            presentation = DynamicInputPresentation(
                type = "segmented", count = 4, spacing = 6.0,
                segmentWidth = 54.0, segmentHeight = 56.0,
                background = gradient,
            ),
        ),
        label("screen_label", "Screen background — 135°, stops: 0 / 0.28 / 0.55 / 1"),
    )
    val template = FormTemplate(
        id = templateId,
        fillMaxSize = true,
        padding = DynamicPadding(24.0, 16.0, 24.0, 16.0),
        components = listOf(body),
        header = when (mode) {
            0 -> TemplateSlot(components = listOf(component("w5_header", label("header_text", "HEADER — independent padding 0"))))
            2 -> TemplateSlot()
            else -> null
        },
        footer = when (mode) {
            0 -> TemplateSlot(components = listOf(component("w5_footer", label("footer_text", "FOOTER — independent padding 0"))))
            2 -> TemplateSlot()
            else -> null
        },
    )
    fun valNum(value: Double) = DynamicValue.NumberValue(value)
    fun valString(value: String) = DynamicValue.StringValue(value)
    val screenGradient = DynamicValue.ObjectValue(
        mapOf(
            "angle" to valNum(135.0),
            "colors" to DynamicValue.ArrayValue(palette.map { stop ->
                DynamicValue.ObjectValue(mapOf(
                    "color" to valString(stop.color),
                    "stop" to valNum(requireNotNull(stop.stop)),
                ))
            }),
        ),
    )
    val response = DynamicResponse(
        status = 200,
        code = "SUCCESS",
        message = "W5 isolated preview",
        data = DynamicScreen(
            screenId = screenId,
            schemaVersion = "3.0.0",
            targetApp = "PARTNER",
            template = template,
            theme = DynamicTheme(theme = null, statusBar = null, properties = mapOf("gradient" to screenGradient)),
        ),
        traceId = null,
    )
    return response to DynamicDestination(screenId, templateId, "form_template", "/preview/w5")
}
