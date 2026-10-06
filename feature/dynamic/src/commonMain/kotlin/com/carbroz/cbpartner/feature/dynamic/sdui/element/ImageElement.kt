package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout

class ImageElement : ElementRenderer {
    @Composable override fun render(element: ImageElementModel, context: DynamicRenderContext) {
        val url = if (element.url.startsWith("/")) context.contextValues["baseUrl"]?.trimEnd('/')?.plus(element.url) ?: element.url else element.url
        AsyncImage(model = url, contentDescription = element.semanticRole, modifier = Modifier.applyLayout(element),
            contentScale = when (element.contentScale) {
                "crop" -> ContentScale.Crop; "fillBounds" -> ContentScale.FillBounds; "fillHeight" -> ContentScale.FillHeight
                "fillWidth" -> ContentScale.FillWidth; "inside" -> ContentScale.Inside; else -> ContentScale.Fit
            })
    }
}