package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.sdui.dynamicGestures
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.DynamicAssetImage

class IconElement : ElementRenderer<IconElementModel> {
    @Composable
    override fun render(element: IconElementModel, context: DynamicRenderContext) {
        val requiredSelection = element.visibleWhenSelected
        if (requiredSelection != null && context.rowSelected != requiredSelection) {
            if (element.reserveSpaceWhenHidden == true) {
                Spacer(modifier = Modifier.applyLayout(element))
            }
            return
        }
        Row(
            modifier = Modifier
                .applyLayout(element)
                .dynamicGestures(element.actions, context.onAction, element.id),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            context.renderContent(element.leadingContent)
            DynamicAssetImage(
                source = element.source,
                legacyUrl = element.url,
                fallbackUrl = element.fallbackUrl,
                context = context,
                contentDescription = element.semanticRole,
                size = element.size,
                color = element.color,
            )
            context.renderContent(element.trailingContent)
        }
    }
}
