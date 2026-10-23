# 136 — Login v10 hero image 02 asset integration

**Date:** 2026-10-23
**Repository branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Owner-supplied image commit:** `c058f3552ca7477e5dfa71caac42a012c0a04c3c` (`assets(dynamic): add revised login hero image`)
**Status:** Source committed; Gradle and browser preview not yet verified.

## Scope and explicit approvals

The owner replaced the Login hero artwork with `partner_login_hero_02.png`, placed at:
`feature/dynamic/src/commonMain/composeResources/drawable/partner_login_hero_02.png`.

Only the existing explicit packaged-asset lookup and the Login v10 hero source reference need updating. Previous hero files `partner_login_hero.png` and `partner_login_hero_01.png` remain unchanged. No alternative-fallback behavior or screen-specific Kotlin rendering is introduced.

## Implementation

- `DynamicPackagedAssets.kt`: add generated Compose resource import `partner_login_hero_02` and explicit key `images/partner_login_hero_02` mapped to `Res.drawable.partner_login_hero_02`. Preserve old mappings.
- `DynamicAssetImageTest.kt`: verify that the new key resolves to an available packaged drawable resource.
- `partner-login-country-picker-parity-v10.json`: change only `data.template.properties.backgroundImage.source.value` from `images/partner_login_hero` to `images/partner_login_hero_02`.
- `webApp/src/wasmJsMain/resources/preview/partner-login.json`: identical change; identical contents to versioned v10 JSON.
- Preserve `source.type="resource"`, `contentScale="crop"`, and `alignment="topCenter"`.

### Source verification

Uploaded PNG exists in GitHub repository with blob SHA `aa087ceccf54065db6a1f2960cfb8bdb9ffb9f8f`, size 1927274 bytes. Only the single hero source value differs between before/after Login JSON. All Login typography v10, Validation `clearOnEdit`, FormFieldSection, reserved error space, Country Picker JSON, Store and Button behavior remain unchanged.

### Required Windows build and visual gate

```powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack
```

After the build, hard-refresh actual Dynamic Web Login preview (the current local dev-server port; check server terminal) and inspect the new hero subject/crop relative to the overlaid form. No screenshot or build result is available from this GitHub-only write. Do not assert visual parity until user confirmation.
