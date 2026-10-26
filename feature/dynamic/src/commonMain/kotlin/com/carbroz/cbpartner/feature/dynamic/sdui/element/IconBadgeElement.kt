package com.carbroz.cbpartner.feature.dynamic.sdui.element

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.carbroz.cbpartner.domain.model.dynamic.element.IconBadgeElement as IconBadgeElementModel
import com.carbroz.cbpartner.feature.dynamic.registry.ElementRenderer
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicBackground
import com.carbroz.cbpartner.feature.dynamic.runtime.applyDynamicSpacing
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.DynamicAssetImage
import com.carbroz.cbpartner.feature.dynamic.sdui.asset.dynamicImageContentScale

/** Delegates all asset source validation/loading/fallback to DynamicAssetImage. */
class IconBadgeElement : ElementRenderer<IconBadgeElementModel> {
    @Composable
    override fun render(element: IconBadgeElementModel, context: DynamicRenderContext) {
        val border = element.border
        Box(
            modifier = Modifier
                .applyDynamicSpacing(element.spacing)
                .size(element.size.dp)
                .clip(CircleShape)
                .applyDynamicBackground(element.background, CircleShape)
                .then(
                    if (border == null) Modifier
                    else Modifier.border(
                        BorderStroke(
                            border.width.dp,
                            requireNotNull(border.color.toColor()) { "Invalid IconBadge border color" },
                        ),
                        CircleShape,
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            DynamicAssetImage(
                source = element.source,
                legacyUrl = null,
                fallbackUrl = null,
                context = context,
                contentDescription = null,
                size = element.imageSize,
                contentScale = dynamicImageContentScale(element.contentScale),
            )
        }
    }
}
