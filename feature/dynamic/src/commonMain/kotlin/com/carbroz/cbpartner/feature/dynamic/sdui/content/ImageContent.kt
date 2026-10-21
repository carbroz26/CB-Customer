package com.carbroz.cbpartner.feature.dynamic.sdui.content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.content.ImageContent as ImageContentModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementContentRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.DynamicAssetImage
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.dynamicImageContentScale
import com.carbroz.cbpartner.feature.dynamic.sdui.dynamicGestures

/** Accessory image renderer; distinct from standalone ImageElement. */
class ImageContent : ElementContentRenderer<ImageContentModel> {
    @Composable
    override fun render(content: ImageContentModel, context: DynamicRenderContext) {
        val dimensions = Modifier
            .then(content.width?.let { Modifier.width(it.dp) } ?: Modifier)
            .then(content.height?.let { Modifier.height(it.dp) } ?: Modifier)
        Box(modifier = Modifier.applyDynamicSpacing(content.spacing)) {
            DynamicAssetImage(
                source = content.source,
                legacyUrl = null,
                fallbackUrl = content.fallbackUrl,
                context = context,
                contentDescription = content.semanticRole,
                modifier = dimensions.dynamicGestures(content.actions, context.onAction, content.id),
                contentScale = dynamicImageContentScale(content.contentScale),
            )
        }
    }
}
