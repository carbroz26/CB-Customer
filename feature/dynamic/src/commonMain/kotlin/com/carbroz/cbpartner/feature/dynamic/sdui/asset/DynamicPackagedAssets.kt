package com.carbroz.cbpartner.feature.dynamic.sdui.asset

import org.jetbrains.compose.resources.DrawableResource
import com.carbroz.cbpartner.feature.dynamic.generated.resources.Res
import com.carbroz.cbpartner.feature.dynamic.generated.resources.cb_partner_logo
import com.carbroz.cbpartner.feature.dynamic.generated.resources.flag_in
import com.carbroz.cbpartner.feature.dynamic.generated.resources.flag_us
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_check_circle_blue
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_chevron_down
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_close
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_search
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero

/** Only approved, genuinely bundled assets belong in this explicit lookup. */
internal fun validPackagedAssetId(key: String): Boolean =
    key.matches(Regex("^(images|icons|flags)/[a-z0-9_]+$"))

internal fun packagedAssetResource(key: String): DrawableResource? = when (key) {
    "images/cb_partner_logo" -> Res.drawable.cb_partner_logo
    "images/partner_login_hero" -> Res.drawable.partner_login_hero
    "flags/in" -> Res.drawable.flag_in
    "flags/us" -> Res.drawable.flag_us
    "icons/chevron_down" -> Res.drawable.icon_chevron_down
    "icons/search" -> Res.drawable.icon_search
    "icons/close" -> Res.drawable.icon_close
    "icons/check_circle_blue" -> Res.drawable.icon_check_circle_blue
    else -> null
}
