package com.carbroz.cbpartner.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicBinding
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent
import com.carbroz.cbpartner.domain.model.dynamic.element.ButtonElement
import com.carbroz.cbpartner.domain.model.dynamic.element.InputElement
import com.carbroz.cbpartner.domain.model.dynamic.element.TextElement
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicInputPresentation
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicTextSpan
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.dynamicRendererRegistry
import com.carbroz.cbpartner.feature.dynamic.renderer.DynamicRenderer

/**
 * Temporary opt-in W6-A renderer event probe.
 * Captures callbacks only: no HTTP, Store, session, action execution or navigation.
 * It proves native renderer event delivery; it does not prove Store commit ordering.
 */
@Composable
fun W6EventPreview() {
    var input by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var events by remember { mutableStateOf(emptyList<String>()) }
    val registry = remember { dynamicRendererRegistry() }
    val fixture = remember { w6EventFixture() }
    val bindings = mapOf(
        "w6_text" to DynamicValue.StringValue(input),
        "w6_otp" to DynamicValue.StringValue(otp),
    )
    fun record(event: String) {
        events = (events + event).takeLast(12)
    }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)) {
            Text("W6-A native event probe — isolated; no backend", style = MaterialTheme.typography.titleMedium)
            Text("Tap and LONG-PRESS the button. Focus, type and press keyboard Done in each input.")
            Text("Completing OTP must NOT auto-submit. Tap the linked text separately.")
            DynamicRenderer(
                registry = registry,
                bindings = bindings,
                onValueChange = { key, value ->
                    val next = (value as? DynamicValue.StringValue)?.value.orEmpty()
                    if (key == "w6_text") input = next
                    if (key == "w6_otp") otp = next
                    record("value updated: $key = $next")
                },
                onAction = { action ->
                    val label = (action.payload["event"] as? DynamicValue.StringValue)?.value ?: action.type
                    record("ACTION $label | text=$input | otp=$otp")
                },
            ).Render(fixture.first, fixture.second)
            Row {
                TextButton(onClick = { events = emptyList() }) { Text("Clear event log") }
                TextButton(onClick = { input = ""; otp = ""; events = emptyList() }) { Text("Reset") }
            }
            Text("Event log (latest 12)", style = MaterialTheme.typography.titleSmall)
            events.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

private fun w6EventFixture(): Pair<DynamicResponse, DynamicDestination> {
    fun action(name: String) = DynamicAction(
        type = "state",
        payload = mapOf("event" to DynamicValue.StringValue(name)),
    )
    val elements = listOf(
        ButtonElement(
            id = "w6_button", text = "Tap / long press this button", height = 50.0,
            fillMaxWidth = true,
            actions = mapOf("onClick" to action("button.click"), "onLongClick" to action("button.longClick")),
        ),
        TextElement(
            id = "w6_text_element", text = "Tap / long press plain text",
            fontSize = 18.0,
            actions = mapOf("onClick" to action("text.click"), "onLongClick" to action("text.longClick")),
        ),
        TextElement(
            id = "w6_link", spans = listOf(
                DynamicTextSpan(
                    text = DynamicValue.StringValue("Tap this clickable text span"),
                    underline = true,
                    action = action("span.click"),
                ),
            ),
            fontSize = 18.0,
        ),
        TextElement(id = "w6_text_title", text = "Regular field (focus / edit / IME Done)", fontSize = 16.0),
        InputElement(
            id = "w6_field", binding = DynamicBinding("w6_text"),
            placeholder = "Tap here and type", fontSize = 18.0, fillMaxWidth = true,
            actions = mapOf(
                "onFocus" to action("field.focus"),
                "onValueChange" to action("field.change"),
                "onSubmit" to action("field.submit"),
            ),
        ),
        TextElement(id = "w6_otp_title", text = "OTP (type 4 digits, then press keyboard Done)", fontSize = 16.0),
        InputElement(
            id = "w6_otp_input", binding = DynamicBinding("w6_otp"), keyboardType = "number",
            maxLength = 4,
            presentation = DynamicInputPresentation(
                type = "segmented", count = 4, spacing = 8.0,
                segmentWidth = 58.0, segmentHeight = 56.0,
            ),
            actions = mapOf(
                "onFocus" to action("otp.focus"),
                "onValueChange" to action("otp.change"),
                "onSubmit" to action("otp.submit"),
            ),
        ),
    )
    val response = DynamicResponse(
        status = 200, code = "OK", message = "W6 fixture", traceId = null,
        data = DynamicScreen(
            screenId = "w6_events", schemaVersion = "3.0.0", targetApp = "PARTNER",
            template = FormTemplate(
                id = "w6_template",
                components = listOf(StackComponent(id = "w6_component", elements = elements)),
            ),
            theme = null,
        ),
    )
    return response to DynamicDestination("w6_events", "w6_template", "form_template", "/w6-preview")
}
