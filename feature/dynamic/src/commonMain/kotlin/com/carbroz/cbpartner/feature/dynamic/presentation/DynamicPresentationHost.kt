package com.carbroz.cbpartner.feature.dynamic.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.carbroz.cbpartner.domain.model.dynamic.action.DynamicAction
import com.carbroz.cbpartner.domain.model.dynamic.section.DynamicListCatalog
import com.carbroz.cbpartner.domain.model.dynamic.presentation.BottomSheetPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DialogPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentation
import com.carbroz.cbpartner.domain.model.dynamic.presentation.DynamicPresentationHandle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.registry.DynamicRegistry
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicBackground
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeShape
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

@Composable
fun DynamicPresentationHost(
    presentation: DynamicPresentation,
    registry: DynamicRegistry,
    trustedAssetOrigin: String? = null,
    listCatalog: DynamicListCatalog? = null,
    onListItemSelected: (String, String) -> Unit = { _, _ -> },
    bindings: Map<String, DynamicValue>,
    contextValues: Map<String, DynamicValue>,
    stateValues: Map<String, DynamicValue>,
    validationErrors: Map<String, String>,
    focusedBinding: String?,
    onAction: (DynamicAction) -> Unit,
    onButtonAction: (DynamicAction, String) -> Unit = { action, _ -> onAction(action) },
    pendingButtonId: String? = null,
    onValueChange: (String, DynamicValue) -> Unit,
    onFocusChange: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = DynamicRenderContext(
        registry = registry,
        trustedAssetOrigin = trustedAssetOrigin,
        bindings = bindings,
        contextValues = contextValues,
        stateValues = stateValues,
        validationErrors = validationErrors,
        focusedBinding = focusedBinding,
        onAction = onAction,
        onButtonAction = onButtonAction,
        pendingButtonId = pendingButtonId,
        onValueChange = onValueChange,
        onFocusChange = onFocusChange,
        listCatalog = listCatalog,
        onListItemSelected = onListItemSelected,
    )

    when (presentation) {
        is DialogPresentation -> Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = presentation.dismissOnBack == true,
                dismissOnClickOutside = presentation.dismissOnOutsideClick == true,
            ),
        ) {
            Column {
                context.renderComponents(presentation.components)
            }
        }

        is BottomSheetPresentation -> Dialog(
            onDismissRequest = {
                if (presentation.dismissOnBack == true) onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = presentation.dismissOnBack == true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(presentation.id, presentation.dismissOnOutsideClick) {
                        detectTapGestures {
                            if (presentation.dismissOnOutsideClick == true) onDismiss()
                        }
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                val surfaceShape = presentation.shape.toComposeShape()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(presentation.heightFraction?.let {
                            Modifier.fillMaxHeight(it.toFloat())
                        } ?: Modifier)
                        .clip(surfaceShape)
                        .applyDynamicBackground(presentation.background, surfaceShape)
                        .pointerInput(presentation.id) {
                            detectTapGestures(onTap = {})
                        },
                ) {
                    if (presentation.showHandle == true) {
                        presentation.handle?.let { DynamicPresentationHandle(it) }
                    }
                    context.renderComponents(presentation.components)
                }
            }
        }
    }
}

@Composable
private fun DynamicPresentationHandle(handle: DynamicPresentationHandle) {
    val width = handle.width ?: return
    val height = handle.height ?: return
    val color = handle.color?.toColor() ?: return
    val alignment = when (handle.alignment) {
        "start" -> Alignment.CenterStart
        "center" -> Alignment.Center
        "end" -> Alignment.CenterEnd
        else -> return
    }
    val shape = handle.cornerRadius?.let { RoundedCornerShape(it.dp) } ?: RectangleShape

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .applyDynamicSpacing(handle.spacing),
    ) {
        Box(
            modifier = Modifier
                .align(alignment)
                .width(width.dp)
                .height(height.dp)
                .background(color, shape),
        )
    }
}
