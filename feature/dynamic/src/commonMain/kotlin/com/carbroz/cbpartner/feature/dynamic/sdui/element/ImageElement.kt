package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicPadding
import com.carbroz.cbpartner.feature.dynamic.runtime.applyImageSize
import com.carbroz.cbpartner.feature.dynamic.sdui.content.DynamicAssetImage
import com.carbroz.cbpartner.feature.dynamic.generated.resources.Res
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dynamic_image_fallback
import org.jetbrains.compose.resources.painterResource

class ImageElement : ElementRenderer {
    @Composable
    override fun render(dynamicElement: DynamicElement, context: DynamicRenderContext) {
        val element = dynamicElement as? ImageElementModel ?: return
        Row(modifier = Modifier.applyDynamicPadding(element.padding)) {
            context.renderContent(element.leadingContent)
            DynamicAssetImage(
                url = element.url,
                fallbackUrl = element.fallbackUrl,
                fallbackPainter = painterResource(Res.drawable.dynamic_image_fallback),
                contentDescription = element.semanticRole,
                modifier = Modifier.applyImageSize(element),
                contentScale = when (element.contentScale) {
                    "crop" -> ContentScale.Crop
                    "fillBounds" -> ContentScale.FillBounds
                    "fillHeight" -> ContentScale.FillHeight
                    "fillWidth" -> ContentScale.FillWidth
                    "inside" -> ContentScale.Inside
                    else -> null
                },
            )
            context.renderContent(element.trailingContent)
        }
    }
}
