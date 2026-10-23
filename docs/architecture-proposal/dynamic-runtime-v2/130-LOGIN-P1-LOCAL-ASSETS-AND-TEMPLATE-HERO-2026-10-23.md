# 130 — P1 Local assets and packaged Template hero
**Date:** 2026-10-23
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Status:** Source/JSON changes committed; local JVM/Android/Web execution and visual acceptance pending.

## Source inspection and decisions
The owner committed 9 PNG files in Compose drawable subdirectories: 1 logo, 4 icons, 2 flags (IN/US), and 2 hero alternatives. Move these exact blobs to the immediate `composeResources/drawable/` directory so Compose generates stable `Res.drawable` accessors; preserve the alternative hero `partner_login_hero_01.png` unused, without overwriting it.

Register only *existing* assets in `DynamicPackagedAssets`, and preserve the existing fail-closed behavior for all unsupported keys (GB/AE/SA/QA/KW/OM country flags are not yet supplied).

## Hero
Historical Template hero JSON v5 is URL-backed, intended for a trusted backend asset origin. The current owner explicitly requested displaying the committed *local* hero in Web preview. To support this generically, TemplateBackgroundImage now accepts the preexisting typed `DynamicAssetSource` (URL **or** resource). TemplateMapper retains validation for contentScale/alignment; URL support is preserved, resource sources do not allow fallbackUrl. No hardcoded Login image is introduced into a renderer.

The versioned JSON target `partner-login-country-picker-parity-v6.json` selects `source: {"type":"resource","value":"images/partner_login_hero"}`, and the Web fixture matches it byte for byte. A future backend may supply a URL source as before. Theme gradient, positions, layout, form input, legal, list, bindings, actions, Store, and `VisualLoginPreview.kt` remain unchanged.

## Tests and remaining work
Updated `DynamicAssetImageTest` to assert real packaged registrations and fail-closed absent keys. `DynamicResponseMapperTest` retains original URL mapping coverage and adds a resource Template hero example. Kotlin builds must generate resource accessors and pass `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack` before runtime acceptance. Pixel/screen parity requires a Web screenshot.

Existing flags: India and United States only. Do not invent country images or generic fallbacks. Potential asset file-size optimization is a distinct future task; source PNGs are currently used as provided.
