package com.carbroz.cbpartner.feature.dynamic.sdui.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor

@Composable
internal fun DynamicAssetImage(
    url: String,
    fallbackUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Double? = null,
    color: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    var currentUrl by remember(url, fallbackUrl) { mutableStateOf<String?>(url) }

    if (currentUrl != null) {
        AsyncImage(
            model = currentUrl,
            contentDescription = contentDescription,
            modifier = modifier.then(size?.let { Modifier.size(it.dp) } ?: Modifier),
            contentScale = contentScale,
            colorFilter = color?.let { it.toColor()?.let(ColorFilter::tint) },
            onError = {
                currentUrl = if (currentUrl == url) fallbackUrl else null
            },
        )
    }
}
