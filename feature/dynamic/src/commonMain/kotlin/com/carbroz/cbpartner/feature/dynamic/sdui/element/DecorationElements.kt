package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.domain.model.dynamic.element.IconElement as IconElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.IconElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.sdui.content.DynamicAssetImage

class IconElement : IconElementRenderer {
    @Composable
    override fun render(element: IconElementModel, context: DynamicRenderContext) {
        Row(
            modifier = Modifier.applyLayout(element),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            context.renderContent(element.leadingContent)
            DynamicAssetImage(
                url = element.url,
                fallbackUrl = element.fallbackUrl,
                contentDescription = element.semanticRole,
                size = element.size,
                color = element.color,
            )
            context.renderContent(element.trailingContent)
        }
    }
}
