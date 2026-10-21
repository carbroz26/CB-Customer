package com.carbroz.cbpartner.feature.dynamic.sdui.asset

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicAssetSource
import com.carbroz.cbpartner.domain.model.dynamic.value.DynamicValue
import com.carbroz.cbpartner.feature.dynamic.runtime.DynamicRenderContext
import com.carbroz.cbpartner.feature.dynamic.runtime.toColor
import org.jetbrains.compose.resources.painterResource

/**
 * Single asset rendering boundary for Elements and Content.
 * Never silently load external origins or show generic fallback artwork.
 */
@Composable
internal fun DynamicAssetImage(
    source: DynamicAssetSource?,
    legacyUrl: String?,
    fallbackUrl: String?,
    context: DynamicRenderContext,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Double? = null,
    color: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val sourceValue = source?.let {
        (context.resolve(it.value) as? DynamicValue.StringValue)?.value
    }
    val imageModifier = modifier.then(size?.let { Modifier.size(it.dp) } ?: Modifier)
    val colorFilter = color?.toColor()?.let(ColorFilter::tint)

    if (source is DynamicAssetSource.Resource) {
        val key = sourceValue?.takeIf(::validPackagedAssetId)
        val resource = key?.let(::packagedAssetResource)
        if (resource == null) {
            LaunchedEffect(key) {
                runCatching {
                    Logger.withTag("CBPartnerFlow").d { "event=asset.unavailable kind=packaged" }
                }
            }
        } else {
            Image(
                painter = painterResource(resource),
                contentDescription = contentDescription,
                modifier = imageModifier,
                contentScale = contentScale,
                colorFilter = colorFilter,
            )
        }
        return
    }

    val rawPrimary = if (source is DynamicAssetSource.Url) sourceValue else legacyUrl
    val primary = rawPrimary?.let { trustedAssetUrl(it, context.trustedAssetOrigin) }
    if (primary == null) {
        LaunchedEffect(rawPrimary) {
            runCatching {
                Logger.withTag("CBPartnerFlow").d { "event=asset.unavailable kind=remote" }
            }
        }
        return
    }
    val fallback = fallbackUrl?.let { trustedAssetUrl(it, context.trustedAssetOrigin) }
        ?.takeIf { it != primary }
    var currentUrl by remember(primary, fallback) { mutableStateOf<String?>(primary) }
    currentUrl?.let { selected ->
        AsyncImage(
            model = selected,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = contentScale,
            colorFilter = colorFilter,
            onError = { currentUrl = if (selected == primary) fallback else null },
        )
    }
}

/** Allows absolute or relative asset paths only under the validated app API origin. */
internal fun trustedAssetUrl(raw: String, trustedOrigin: String?): String? {
    if (trustedOrigin.isNullOrBlank() || raw.isBlank() || raw != raw.trim()) return null
    val prefix = trustedOrigin.removeSuffix("/") + "/"
    val path = when {
        raw.startsWith(prefix) -> raw.removePrefix(prefix)
        raw.startsWith("//") || raw.contains("://") -> return null
        else -> raw.removePrefix("/")
    }
    if (path.isBlank() || path.split('/').any { part ->
            part.isBlank() || part == "." || part == ".." ||
                part.any { it == '?' || it == '#' || it == '\\' || it == '%' || it == '@' || it == ':' }
        }) return null
    return prefix + path
}

internal fun dynamicImageContentScale(value: String?): ContentScale = when (value) {
    null, "fit" -> ContentScale.Fit
    "crop" -> ContentScale.Crop
    "fillBounds" -> ContentScale.FillBounds
    "fillWidth" -> ContentScale.FillWidth
    "fillHeight" -> ContentScale.FillHeight
    "inside" -> ContentScale.Inside
    else -> error("Unsupported dynamic image contentScale")
}
