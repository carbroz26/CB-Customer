package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.FormTemplate
import com.carbroz.cbpartner.feature.dynamic.registry.TemplateRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.toHorizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.toVerticalArrangement

class FormTemplateRenderer : TemplateRenderer {
    @Composable
    override fun render(template: FormTemplate, context: DynamicRenderContext) {
        Column(
            modifier = Modifier.applyLayout(template),
            verticalArrangement = template.verticalArrangement.toVerticalArrangement(),
            horizontalAlignment = template.horizontalAlignment.toHorizontalAlignment(),
        ) {
            context.renderComponents(template.components)
        }
    }
}
