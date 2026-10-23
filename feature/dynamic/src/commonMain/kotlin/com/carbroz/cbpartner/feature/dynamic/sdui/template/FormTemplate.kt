package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.carbroz.cbpartner.domain.model.dynamic.component.StackComponent as StackComponentModel
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate as FormTemplateModel
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicBackground
import com.carbroz.cbpartner.feature.dynamic.registry.TemplateRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicBackground
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.DynamicAssetImage
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.dynamicImageContentScale
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class FormTemplate : TemplateRenderer<FormTemplateModel> {
    @Composable
    override fun render(template: FormTemplateModel, context: DynamicRenderContext) {
        Box(modifier = if (template.fillMaxSize) Modifier.fillMaxSize() else Modifier) {
            // The authored background fills the viewport, including system-bar areas.
            template.backgroundImage?.let { background ->
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
            }

            // Extend an authored bottom Component's surface behind the system
            // navigation bar. The Hero remains full-bleed everywhere else.
            // No fixed color or inset size is imposed by the renderer.
            template.bottomSystemBarBackground()?.let { surface ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .applyDynamicBackground(surface),
                )
            }

            // Keep Header, Body and Footer above system bars and the keyboard.
            // Insets are consumed by Compose; neither changes authored spacing or clips the hero.
            Box(
                modifier = (if (template.fillMaxSize) Modifier.fillMaxSize() else Modifier)
                    .safeDrawingPadding()
                    .imePadding(),
            ) {
                RenderTemplateContent(template, context)
            }
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
        // A verticalScroll child receives unbounded height. Preserve at least the
        // viewport height so an authored spaceBetween arrangement can keep the
        // last Component at the bottom when content is shorter than the screen.
        if (template.orientation != "horizontal" && template.scrollable && template.fillMaxSize) {
            BoxWithConstraints(modifier = modifier) {
                RenderVertical(
                    template,
                    context,
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight),
                )
            }
            return
        }

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
            RenderVertical(template, context, bodyModifier)
        }
    }

    @Composable
    private fun RenderVertical(
        template: FormTemplateModel,
        context: DynamicRenderContext,
        modifier: Modifier,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = template.verticalArrangement.toVerticalArrangement(),
            horizontalAlignment = template.horizontalAlignment.toHorizontalAlignment(),
        ) {
            context.renderComponents(template.components)
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

/**
 * Derive a system-navigation backdrop from the last authored surface only.
 * This does not change Component geometry, keyboard insets or the Hero crop.
 */
internal fun FormTemplateModel.bottomSystemBarBackground(): DynamicBackground? {
    if (!fillMaxSize || orientation == "horizontal") return null
    val bottomComponent = footer?.components?.lastOrNull() ?: components.lastOrNull()
    return (bottomComponent as? StackComponentModel)?.background
}
