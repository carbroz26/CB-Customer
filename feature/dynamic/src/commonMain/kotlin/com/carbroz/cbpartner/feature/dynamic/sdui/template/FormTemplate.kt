package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate as FormTemplateModel
import com.carbroz.cbpartner.feature.dynamic.registry.TemplateRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalArrangement
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class FormTemplate : TemplateRenderer<FormTemplateModel> {
    @Composable
    override fun render(template: FormTemplateModel, context: DynamicRenderContext) {
        if (template.header != null || template.footer != null) {
            TemplateShell(template, context) { bodyModifier -> RenderBody(template, context, bodyModifier) }
        } else {
            RenderBody(template, context, Modifier.applyLayout(template))
        }
    }

    @Composable
    internal fun RenderBody(template: FormTemplateModel, context: DynamicRenderContext, modifier: Modifier) {
        if (template.orientation == "horizontal") {
            Row(
                modifier = modifier,
                horizontalArrangement = template.horizontalArrangement.toHorizontalArrangement(),
                verticalAlignment = template.verticalAlignment.toVerticalAlignment(),
            ) {
                context.renderComponents(template.components)
            }
        } else {
            Column(
                modifier = modifier,
                verticalArrangement = template.verticalArrangement.toVerticalArrangement(),
                horizontalAlignment = template.horizontalAlignment.toHorizontalAlignment(),
            ) {
                context.renderComponents(template.components)
            }
        }
    }
}