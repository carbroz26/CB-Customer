package com.carbroz.cbpartner.feature.dynamic.renderer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.carbroz.cbpartner.domain.model.DynamicDestination
import com.carbroz.cbpartner.domain.model.dynamic.DynamicResponse
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLinearGradient
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

class DynamicRenderer(
    private val registry: DynamicRegistry,
    private val trustedAssetOrigin: String? = null,
    private val onAction: (DynamicAction) -> Unit = {},
    private val onValueChange: (String, DynamicValue) -> Unit = { _, _ -> },
    private val onFocusChange: (String, Boolean) -> Unit = { _, _ -> },
    private val bindings: Map<String, DynamicValue> = emptyMap(),
    private val contextValues: Map<String, DynamicValue> = emptyMap(),
    private val stateValues: Map<String, DynamicValue> = emptyMap(),
    private val validationErrors: Map<String, String> = emptyMap(),
    private val focusedBinding: String? = null,
) {
    @Composable
    fun Render(
        response: DynamicResponse,
        destination: DynamicDestination,
        onRenderCommitted: () -> Unit = {},
    ) {
        val screen = response.data ?: return

        if (
            screen.screenId != destination.screenId ||
            screen.template.id != destination.templateId ||
            screen.template.type != destination.templateType
        ) {
            return
        }

        val templateRenderer = registry.template(screen.template)
            ?: error("No Dynamic template renderer registered for model class: " + screen.template::class)
        var reported by remember(screen) { mutableStateOf(false) }
        SideEffect {
            if (!reported) {
                reported = true
                onRenderCommitted()
            }
        }

        val context = DynamicRenderContext(
            registry = registry,
            trustedAssetOrigin = trustedAssetOrigin,
            bindings = bindings,
            contextValues = contextValues,
            stateValues = stateValues,
            validationErrors = validationErrors,
            focusedBinding = focusedBinding,
            onAction = onAction,
            onValueChange = onValueChange,
            onFocusChange = onFocusChange,
        )

        val focusManager = LocalFocusManager.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .screenTheme(screen.theme?.properties)
                .pointerInput(focusManager) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                },
        ) {
            templateRenderer.render(screen.template, context)
        }
    }

    private fun Modifier.screenTheme(
        properties: Map<String, DynamicValue>?,
    ): Modifier {
        val gradient =
            properties?.get("gradient") as? DynamicValue.ObjectValue
                ?: return this

        val stops = (gradient.value["colors"] as? DynamicValue.ArrayValue)
            ?.value.orEmpty().mapNotNull { item ->
                val value = (item as? DynamicValue.ObjectValue)?.value ?: return@mapNotNull null
                val color = (value["color"] as? DynamicValue.StringValue)?.value?.toColor()
                    ?: return@mapNotNull null
                val stop = (value["stop"] as? DynamicValue.NumberValue)?.value
                stop to color
            }
        val angle = (gradient.value["angle"] as? DynamicValue.NumberValue)?.value ?: 0.0
        return applyLinearGradient(angle, stops)
    }
}
