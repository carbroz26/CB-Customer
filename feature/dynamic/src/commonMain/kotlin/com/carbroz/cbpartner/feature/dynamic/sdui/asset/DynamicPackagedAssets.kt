package com.carbroz.cbpartner.feature.dynamic.sdui.asset

import org.jetbrains.compose.resources.DrawableResource

/** Only approved, genuinely bundled assets belong in this explicit lookup. */
internal fun validPackagedAssetId(key: String): Boolean =
    key.matches(Regex("^(images|icons|flags)/[a-z0-9_]+$"))

internal fun packagedAssetResource(key: String): DrawableResource? = when (key) {
    // No approved Login images, icons or flags are currently packaged.
    // Register only real generated Res.drawable values when originals are supplied.
    else -> null
}
