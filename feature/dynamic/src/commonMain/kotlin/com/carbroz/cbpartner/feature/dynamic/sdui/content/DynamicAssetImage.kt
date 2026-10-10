package com.carbroz.cbpartner.feature.dynamic.sdui.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

@Composable
internal fun DynamicAssetImage(
    url: String,
    fallbackUrl: String?,
    fallbackPainter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Double? = null,
    color: String? = null,
    contentScale: ContentScale? = null,
) {
    var currentUrl by remember(url, fallbackUrl) {
        mutableStateOf(url.takeIf(String::isNotBlank) ?: fallbackUrl?.takeIf(String::isNotBlank))
    }
    val imageModifier = modifier.then(size?.let { Modifier.size(it.dp) } ?: Modifier)
    val colorFilter = color?.let { it.toColor()?.let(ColorFilter::tint) }

    if (currentUrl != null) {
        AsyncImage(
            model = currentUrl,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale ?: ContentScale.Fit,
            colorFilter = colorFilter,
            onError = {
                currentUrl = if (currentUrl == url) {
                    fallbackUrl?.takeIf { it.isNotBlank() && it != currentUrl }
                } else {
                    null
                }
            },
        )
    } else {
        Image(
            painter = fallbackPainter,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale ?: ContentScale.Fit,
            colorFilter = colorFilter,
        )
    }
}
