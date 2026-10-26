package com.carbroz.cbpartner.feature.dynamic.sdui.asset

import com.carbroz.cbpartner.feature.dynamic.generated.resources.dialog_maintenance
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dialog_required_update
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dialog_optional_update
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dialog_connection
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dialog_server_error
import com.carbroz.cbpartner.feature.dynamic.generated.resources.dialog_secure_connection
import org.jetbrains.compose.resources.DrawableResource
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero_06
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero_07
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_search_02
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_close_02
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_chevron_down_02
import com.carbroz.cbpartner.feature.dynamic.generated.resources.cb_partner_logo_03
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero_04
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero_05
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_search_01
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_close_01
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_chevron_down_01
import com.carbroz.cbpartner.feature.dynamic.generated.resources.error_alert_icon_01
import com.carbroz.cbpartner.feature.dynamic.generated.resources.clock_icon_02
import com.carbroz.cbpartner.feature.dynamic.generated.resources.cb_partner_logo_02
import com.carbroz.cbpartner.feature.dynamic.generated.resources.Res
import com.carbroz.cbpartner.feature.dynamic.generated.resources.cb_partner_logo
import com.carbroz.cbpartner.feature.dynamic.generated.resources.flag_in
import com.carbroz.cbpartner.feature.dynamic.generated.resources.flag_us
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_check_circle_blue
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_chevron_down
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_close
import com.carbroz.cbpartner.feature.dynamic.generated.resources.icon_search
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero
import com.carbroz.cbpartner.feature.dynamic.generated.resources.partner_login_hero_02

/** Only approved, genuinely bundled assets belong in this explicit lookup. */
internal fun validPackagedAssetId(key: String): Boolean =
    key.matches(Regex("^(images|icons|flags)/[a-z0-9_]+$"))

internal fun packagedAssetResource(key: String): DrawableResource? = when (key) {
    "images/cb_partner_logo" -> Res.drawable.cb_partner_logo
    "images/cb_partner_logo_02" -> Res.drawable.cb_partner_logo_02
    "images/cb_partner_logo_03" -> Res.drawable.cb_partner_logo_03
    "images/partner_login_hero" -> Res.drawable.partner_login_hero
    "images/partner_login_hero_02" -> Res.drawable.partner_login_hero_02
    "images/partner_login_hero_04" -> Res.drawable.partner_login_hero_04
    "images/partner_login_hero_05" -> Res.drawable.partner_login_hero_05
    "images/partner_login_hero_06" -> Res.drawable.partner_login_hero_06
    "images/partner_login_hero_07" -> Res.drawable.partner_login_hero_07
    "flags/in" -> Res.drawable.flag_in
    "flags/us" -> Res.drawable.flag_us
    "icons/chevron_down" -> Res.drawable.icon_chevron_down
    "icons/chevron_down_01" -> Res.drawable.icon_chevron_down_01
    "icons/chevron_down_02" -> Res.drawable.icon_chevron_down_02
    "icons/search" -> Res.drawable.icon_search
    "icons/search_01" -> Res.drawable.icon_search_01
    "icons/search_02" -> Res.drawable.icon_search_02
    "icons/close" -> Res.drawable.icon_close
    "icons/close_01" -> Res.drawable.icon_close_01
    "icons/close_02" -> Res.drawable.icon_close_02
    "icons/check_circle_blue" -> Res.drawable.icon_check_circle_blue
    "icons/error_alert_icon_01" -> Res.drawable.error_alert_icon_01
    "icons/dialog_maintenance" -> Res.drawable.dialog_maintenance
    "icons/dialog_required_update" -> Res.drawable.dialog_required_update
    "icons/dialog_optional_update" -> Res.drawable.dialog_optional_update
    "icons/dialog_connection" -> Res.drawable.dialog_connection
    "icons/dialog_server_error" -> Res.drawable.dialog_server_error
    "icons/dialog_secure_connection" -> Res.drawable.dialog_secure_connection
    "icons/clock" -> Res.drawable.clock_icon_02
    else -> null
}
