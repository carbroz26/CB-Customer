package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.domain.model.dynamic.DynamicElement
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyLayout
import com.carbroz.cbpartner.feature.dynamic.runtime.number
import com.carbroz.cbpartner.feature.dynamic.runtime.string

class ImageElement : ElementRenderer {
    @Composable
    override fun render(element: DynamicElement, context: DynamicRenderContext) {
        val properties = element.properties
        val rawUrl = properties.string("url").orEmpty()
        val url = if (rawUrl.startsWith("/")) {
            context.contextValues["baseUrl"]?.trimEnd('/')?.plus(rawUrl) ?: rawUrl
        } else rawUrl
        val contentScale = when (properties.string("contentScale")) {
            "crop" -> ContentScale.Crop
            "fillBounds" -> ContentScale.FillBounds
            "fillHeight" -> ContentScale.FillHeight
            "fillWidth" -> ContentScale.FillWidth
            "inside" -> ContentScale.Inside
            else -> ContentScale.Fit
        }

        AsyncImage(
            model = url,
            contentDescription = properties.string("contentDescription") ?: properties.string("semanticRole"),
            modifier = context.applyLayout(Modifier, properties),
            contentScale = contentScale,
        )
    }
}
