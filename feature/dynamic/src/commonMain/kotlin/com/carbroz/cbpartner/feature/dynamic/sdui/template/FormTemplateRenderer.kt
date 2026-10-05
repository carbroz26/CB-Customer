package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.carbroz.cbpartner.domain.model.dynamic.DynamicTemplate
import com.carbroz.cbpartner.feature.dynamic.registry.TemplateRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.horizontalAlignment
import com.carbroz.cbpartner.feature.dynamic.runtime.verticalArrangement

class FormTemplateRenderer : TemplateRenderer {
    @Composable
    override fun render(template: DynamicTemplate, context: DynamicRenderContext) {
        Column(
            modifier = context.applyLayout(Modifier, template.properties),
            verticalArrangement = template.properties.verticalArrangement(),
            horizontalAlignment = template.properties.horizontalAlignment(),
        ) {
            context.renderComponents(template.components)
        }
    }
}
