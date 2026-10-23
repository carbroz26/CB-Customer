# 137 — Native Android development Login preview using shared JSON v10

**Date:** 2026-10-23  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Status:** Source committed; **Android/Web compilation and real device acceptance NOT YET RUN**.

## Purpose / strict scope

Allow the user to inspect the exact JSON-driven Login v10 + packaged hero 02 on a real Samsung Galaxy A35 through the **native developmentDebug application**, without the backend. This is an opt-in **preview entry**; no second Store, renderer, DTO mapper or presentation runtime.

## Implementation

- The Web Login Preview and native Android development entry now call the same `parseLocalLoginPreview(raw)` function in the common `app` module. It runs the **existing DynamicResponseDto + DynamicResponseMapper** and provides the existing sample country catalog. The DTO and renderers remain unchanged. `LocalLoginPreview` replaces the previously Web-only named input; the Web Preview route remains supported.
- `App(localLoginPreview=...)` preserves existing development-only injection via the normal DynamicRepository, DynamicStore, DynamicFormValidator and dynamicRendererRegistry. The offline repository only serves the preview's initial Login destination; further network mutations intentionally fail. No backend integration or production behavior changed.
- `androidApp/MainActivity` reads the bundled `partner-login.json` asset only if `BuildConfig.DEBUG`, `AppEnvironment.Development`, and explicit intent extra `cbpartner.login_preview=true` all agree. If not requested, it follows normal Splash→Backend Bootstrap. Retention probe remains separately supported.
- `androidApp/build.gradle.kts` registers a **developmentDebug-only** assets root pointing directly to `webApp/src/wasmJsMain/resources/preview`, so the exact same v10 JSON file is bundled; **no duplicated JSON fixture**. Never bundle that fixture into staging or production.
- No Android-specific screen visuals or hardcoded Login layout. Country flags are only bundled where present; no invented icons.
- The user-supplied Hero 02 PNG continues to load from the already registered Compose resources.

## Commands (PowerShell)

```powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :app:jvmTest :androidApp:assembleDevelopmentDebug :webApp:wasmJsBrowserDevelopmentWebpack
adb devices -l
adb install -r .\androidApp\build\outputs\apk\development\debug\androidApp-development-debug.apk
adb shell am force-stop com.carbroz.cbpartner.android.dev
adb shell am start -n com.carbroz.cbpartner.android.dev/.MainActivity --ez cbpartner.login_preview true
```

If Gradle reports a different APK filename/path, use the `developmentDebug` APK from its output instead of guessing. On multiple ADB connections to the same physical phone, explicitly specify one serial using `adb -s`. No backend server, Web dev server or `adb reverse` is required in this local native mode.

## Acceptance gates

1. `app:jvmTest`, `androidApp:assembleDevelopmentDebug` and `webApp:wasmJsBrowserDevelopmentWebpack` must pass first.
2. On Samsung device, launch with the extra and verify hero 02, colors, type, header/card positioning, validation and Country Picker.
3. Relaunch **without** the extra and ensure normal Splash/Bootstrap path unchanged (backend needed for Login).
4. Verify only the `developmentDebug` variant contains the local JSON fixture. Neither staging nor production uses preview routing.
5. Keep source code and docs under V2; P0 visual acceptance remains open and P1 cleanup is not authorized yet.
