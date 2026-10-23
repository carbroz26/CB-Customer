package com.carbroz.cbpartner.feature.dynamic.sdui.asset

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DynamicAssetImageTest {
    private val origin = "https://api.example.com"

    @Test
    fun acceptsOnlySameOriginOrRelativeAssetPaths() {
        assertEquals("https://api.example.com/assets/logo.png",
            trustedAssetUrl("https://api.example.com/assets/logo.png", origin))
        assertEquals("https://api.example.com/assets/logo.png",
            trustedAssetUrl("/assets/logo.png", origin))
        assertEquals("https://api.example.com/assets/logo.png",
            trustedAssetUrl("assets/logo.png", origin))
    }

    @Test
    fun rejectsUntrustedOriginsCredentialsAndUnsafePaths() {
        for (candidate in listOf(
            "https://external.example.com/assets/logo.png",
            "https://api.example.com.attacker.example/img.png",
            "http://api.example.com/assets/logo.png",
            "//external.example.com/image.png",
            "data:image/png;base64,ABCD",
            "file:///private/secret.png",
            "assets/../private.png",
            "/assets//logo.png",
            "/assets/%2e%2e/secret.png",
            "/assets/logo.png?token=private",
            "/assets/logo.png#fragment",
            "/assets\\logo.png",
            " /assets/logo.png",
            "assets/logo.png ",
            "assets/a b.png",
            "assets/\tlogo.png",
        )) assertNull(trustedAssetUrl(candidate, origin))
        assertNull(trustedAssetUrl("/assets/logo.png", null))
    }

    @Test
    fun onlyExplicitPackagedResourcesAreEligible() {
        assertTrue(validPackagedAssetId("flags/in"))
        assertTrue(validPackagedAssetId("images/cb_partner_logo"))
        assertFalse(validPackagedAssetId("../private"))
        assertFalse(validPackagedAssetId("icons/close.png"))
        for (key in listOf(
            "images/cb_partner_logo", "images/cb_partner_logo_02", "images/cb_partner_logo_03",
            "images/partner_login_hero", "images/partner_login_hero_02", "images/partner_login_hero_04", "images/partner_login_hero_05", "images/partner_login_hero_06",
            "flags/in", "flags/us",
            "icons/chevron_down", "icons/search",
            "icons/close", "icons/check_circle_blue",
            "icons/chevron_down_01", "icons/search_01", "icons/close_01",
            "icons/chevron_down_02", "icons/search_02", "icons/close_02",
            "icons/error_alert_icon_01",
        )) assertNotNull(packagedAssetResource(key), "Missing explicit packaged asset: " + key)
        assertNull(packagedAssetResource("flags/gb"))
        assertNull(packagedAssetResource("icons/unapproved"))
    }
}
