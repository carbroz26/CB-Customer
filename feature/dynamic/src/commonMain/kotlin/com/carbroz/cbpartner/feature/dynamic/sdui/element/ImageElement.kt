package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.dynamicImageContentScale
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicPadding
import com.carbroz.cbpartner.feature.dynamic.runtime.applyImageSize
import com.carbroz.cbpartner.feature.dynamic.sdui.dynamicGestures
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.DynamicAssetImage

class ImageElement : ElementRenderer<ImageElementModel> {
    @Composable
    override fun render(element: ImageElementModel, context: DynamicRenderContext) {
        Row(modifier = Modifier.applyDynamicPadding(element.padding)) {
            context.renderContent(element.leadingContent)
            DynamicAssetImage(
                source = element.source,
                legacyUrl = element.url,
                fallbackUrl = element.fallbackUrl,
                context = context,
                contentDescription = element.semanticRole,
                modifier = Modifier.applyImageSize(element).dynamicGestures(element.actions, context.onAction, element.id),
                contentScale = dynamicImageContentScale(element.contentScale),
            )
            context.renderContent(element.trailingContent)
        }
    }
}
