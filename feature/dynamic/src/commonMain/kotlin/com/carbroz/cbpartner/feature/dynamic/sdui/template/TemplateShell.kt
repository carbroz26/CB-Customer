package com.carbroz.cbpartner.feature.dynamic.sdui.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.template.FormTemplate
import com.carbroz.cbpartner.domain.model.dynamic.template.TemplateSlot
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicPadding

/** Places optional independent slots around the existing template body. */
@Composable
internal fun TemplateShell(
    template: FormTemplate,
    context: DynamicRenderContext,
    body: @Composable (Modifier) -> Unit,
) {
    Column(modifier = if (template.fillMaxSize) Modifier.fillMaxSize() else Modifier) {
        template.header?.let { Slot(it, context) }
        val bodyModifier = Modifier.applyDynamicPadding(template.padding)
        if (template.fillMaxSize) {
            Box(modifier = Modifier.weight(1f)) { body(bodyModifier) }
        } else {
            body(bodyModifier)
        }
        template.footer?.let { Slot(it, context) }
    }
}

@Composable
private fun Slot(slot: TemplateSlot, context: DynamicRenderContext) {
    if (slot.components.isEmpty()) return
    Column(modifier = Modifier.applyDynamicPadding(slot.padding)) {
        context.renderComponents(slot.components)
    }
}
