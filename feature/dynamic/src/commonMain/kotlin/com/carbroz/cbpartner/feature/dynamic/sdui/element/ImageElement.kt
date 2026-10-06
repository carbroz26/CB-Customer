package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.domain.model.dynamic.element.DynamicElement
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout

class ImageElement : ElementRenderer {
    @Composable override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val model = element as? ImageElementModel ?: return
        val url = if (model.url.startsWith("/")) {
            context.contextValues["baseUrl"]?.trimEnd('/')?.plus(model.url) ?: model.url
        } else {
            model.url
        }
        Row {
            context.renderContent(model.leadingContent)
            AsyncImage(
            model = url,
            contentDescription = model.semanticRole,
            modifier = Modifier.applyLayout(model),
            contentScale = when (model.contentScale) {
                "crop" -> ContentScale.Crop
                "fillBounds" -> ContentScale.FillBounds
                "fillHeight" -> ContentScale.FillHeight
                "fillWidth" -> ContentScale.FillWidth
                "inside" -> ContentScale.Inside
                else -> ContentScale.Fit
            }
            )
            context.renderContent(model.trailingContent)
        }
    }
}
