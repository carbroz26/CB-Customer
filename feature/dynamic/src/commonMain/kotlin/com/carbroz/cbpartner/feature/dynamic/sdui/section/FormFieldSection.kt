package com.carbroz.cbpartner.feature.dynamic.sdui.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carbroz.cbpartner.domain.model.dynamic.section.FormFieldSection as FormFieldSectionModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValidationMessageStyle
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBorder
import com.carbroz.cbpartner.feature.dynamic.registry.SectionRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyBorder
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicBackground
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicPadding
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.runtime.toComposeShape
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment

/** Reusable label/control/error composition; delegates binding and validation to the existing Store. */
class FormFieldSection : SectionRenderer<FormFieldSectionModel> {
    @Composable
    override fun render(section: FormFieldSectionModel, context: DynamicRenderContext) {
        val error = context.validationErrors[section.inputBinding]
        val focused = context.focusedBinding == section.inputBinding
        val border = section.resolvedControlBorder(focused = focused, invalid = error != null)
        val surface = section.controlSurface
        val shape = surface.shape.toComposeShape()

        Column(
            modifier = Modifier
                .then(if (section.fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                .applyDynamicPadding(section.padding),
            horizontalAlignment = section.horizontalAlignment.toHorizontalAlignment(),
        ) {
            context.renderElement(section.label)
            Box(
                modifier = Modifier
                    .then(if (surface.fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                    .height(surface.height.dp)
                    .clip(shape)
                    .applyDynamicBackground(surface.background, shape)
                    .applyBorder(border, surface.shape),
            ) {
                section.controlInput?.let { context.renderElement(it) }
                section.controlGroup?.let { context.renderGroup(it) }
            }
            // No empty slot: inline message occupies space only while error is present.
            val style = section.validationPresentation?.messageStyle
            if (error != null && style != null) {
                RenderMessage(error, style)
            }
        }
    }

    @Composable
    private fun RenderMessage(message: String, style: DynamicValidationMessageStyle) {
        Text(
            text = message,
            modifier = Modifier.applyDynamicSpacing(style.spacing),
            color = style.color?.toColor() ?: Color.Unspecified,
            fontSize = style.fontSize?.sp ?: TextUnit.Unspecified,
            lineHeight = style.lineHeight?.sp ?: TextUnit.Unspecified,
            fontWeight = style.fontWeight?.let(::FontWeight),
        )
    }
}

/** Pure typed border precedence; no implicit visual tokens or screen-name checks. */
internal fun FormFieldSectionModel.resolvedControlBorder(
    focused: Boolean,
    invalid: Boolean,
): DynamicBorder? = when {
    invalid -> validationPresentation?.errorBorder ?: controlSurface.border
    focused -> focusPresentation?.border ?: controlSurface.border
    else -> controlSurface.border
}
