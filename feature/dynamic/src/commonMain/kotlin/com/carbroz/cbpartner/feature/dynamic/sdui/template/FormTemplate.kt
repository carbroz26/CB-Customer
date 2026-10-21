package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate as FormTemplateModel
import com.carbroz.cbpartner.feature.dynamic.registry.TemplateRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.DynamicAssetImage
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.dynamicImageContentScale
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class FormTemplate : TemplateRenderer<FormTemplateModel> {
    @Composable
    override fun render(template: FormTemplateModel, context: DynamicRenderContext) {
        val background = template.backgroundImage
        if (background == null) {
            RenderTemplateContent(template, context)
            return
        }

        Box(modifier = if (template.fillMaxSize) Modifier.fillMaxSize() else Modifier) {
            // Match the whole Template, including Header and Footer, without affecting measurement.
            DynamicAssetImage(
                source = background.source,
                legacyUrl = null,
                fallbackUrl = background.fallbackUrl,
                context = context,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = dynamicImageContentScale(background.contentScale),
                alignment = templateBackgroundAlignment(background.alignment),
            )
            RenderTemplateContent(template, context)
        }
    }

    @Composable
    private fun RenderTemplateContent(template: FormTemplateModel, context: DynamicRenderContext) {
        if (template.header != null || template.footer != null) {
            TemplateShell(template, context) { bodyModifier ->
                RenderBody(template, context, bodyModifier)
            }
        } else {
            RenderBody(template, context, Modifier.applyLayout(template))
        }
    }

    @Composable
    internal fun RenderBody(
        template: FormTemplateModel,
        context: DynamicRenderContext,
        modifier: Modifier,
    ) {
        val bodyModifier = if (template.scrollable) {
            modifier.verticalScroll(rememberScrollState())
        } else {
            modifier
        }

        if (template.orientation == "horizontal") {
            Row(
                modifier = bodyModifier,
                horizontalArrangement = template.horizontalArrangement.toHorizontalArrangement(),
                verticalAlignment = template.verticalAlignment.toVerticalAlignment(),
            ) {
                context.renderComponents(template.components)
            }
        } else {
            Column(
                modifier = bodyModifier,
                verticalArrangement = template.verticalArrangement.toVerticalArrangement(),
                horizontalAlignment = template.horizontalAlignment.toHorizontalAlignment(),
            ) {
                context.renderComponents(template.components)
            }
        }
    }
}

/** Backend-selected image placement; no business-screen decisions or UI overrides. */
internal fun templateBackgroundAlignment(value: String): Alignment = when (value) {
    "topStart" -> Alignment.TopStart
    "topCenter" -> Alignment.TopCenter
    "topEnd" -> Alignment.TopEnd
    "centerStart" -> Alignment.CenterStart
    "center" -> Alignment.Center
    "centerEnd" -> Alignment.CenterEnd
    "bottomStart" -> Alignment.BottomStart
    "bottomCenter" -> Alignment.BottomCenter
    "bottomEnd" -> Alignment.BottomEnd
    else -> throw IllegalArgumentException("Unsupported Template backgroundImage alignment")
}
