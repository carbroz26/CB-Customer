package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.domain.model.dynamic.element.ImageElement as ImageElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ImageElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicPadding
import com.carbroz.cbpartner.feature.dynamic.runtime.applyImageSize

class ImageElement : ImageElementRenderer {
    @Composable
    override fun render(element: ImageElementModel, context: DynamicRenderContext) {
        Row(modifier = Modifier.applyDynamicPadding(element.padding)) {
            context.renderContent(element.leadingContent)
            AsyncImage(
                model = element.url,
                contentDescription = element.semanticRole,
                modifier = Modifier.applyImageSize(element),
                contentScale = when (element.contentScale) {
                    "crop" -> ContentScale.Crop
                    "fillBounds" -> ContentScale.FillBounds
                    "fillHeight" -> ContentScale.FillHeight
                    "fillWidth" -> ContentScale.FillWidth
                    "inside" -> ContentScale.Inside
                    else -> ContentScale.Fit
                },
            )
            context.renderContent(element.trailingContent)
        }
    }
}