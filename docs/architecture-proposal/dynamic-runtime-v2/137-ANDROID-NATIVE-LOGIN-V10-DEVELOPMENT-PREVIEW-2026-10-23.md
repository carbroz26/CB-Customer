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

### 2026-10-23 physical device crash and narrow packaging correction

On Samsung A35 with `androidApp:installDevelopmentDebug` successful, running the explicit preview command caused `java.io.FileNotFoundException: partner-login.json` in `MainActivity.onCreate(MainActivity.kt:27)`. This identifies an **APK asset packaging omission**, not a Dynamic UI or backend problem. The older `sourceSets.maybeCreate("developmentDebug").assets.srcDir(...)` registration was insufficient on this build despite successful compilation and installation.

**Correction:** register the original Web fixture path through the Android Gradle Plugin variant Sources API:
`androidComponents.onVariants(selector().withName("developmentDebug")) { variant -> variant.sources.assets!!.addStaticSourceDirectory(...) }`.
The actual implementation uses `checkNotNull` for a descriptive build-time failure. The local fixture is still not duplicated, and the preview remains opt-in and debug+development gated.

**Mandatory asset acceptance before launch:**

```powershell
.\gradlew.bat :androidApp:assembleDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
jar tf .\androidApp\build\outputs\apk\development\debug\androidApp-development-debug.apk | Select-String '^assets/partner-login.json
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

```

If the APK does not contain `assets/partner-login.json`, **do not attempt to launch**; report the APK listing and diagnose packaging. An Android Gradle `BUILD SUCCESSFUL` alone is not sufficient to claim this bug fixed. This correction is **committed but not yet built or device-verified**.

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

### 2026-10-23 correction 2: generated assets wired to Gradle task

The user verified the installed developmentDebug APK again, and `jar tf ... | Select-String "partner-login.json"` produced **no result**. Launching the preview closed the app, consistent with the previously observed missing-asset crash. The prior variant `addStaticSourceDirectory` approach was **not effective in this project** and must not be counted as verified.

Now `androidApp/build.gradle.kts` registers a dedicated `GenerateDevelopmentLoginPreviewAsset` Gradle task with annotated `@InputFile` and `@OutputDirectory`. The task copies the canonical, checked-in Web preview JSON file into `build/generated/assets/developmentLoginPreview/partner-login.json`. The `developmentDebug` variant alone registers that task's `DirectoryProperty` via `variant.sources.assets.addGeneratedSourceDirectory(task, Task::outputDirectory)`. This follows the Android Gradle Plugin's official generated-assets recipe; the task dependency is wired automatically by the variant Sources API. The source fixture is not duplicated in Git and staging/production are not targeted.

**Status:** source committed; no local Gradle execution or device verification available from GitHub connector.

**Mandatory two-stage verification before launch:**

```powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
jar tf .\androidApp\build\outputs\apk\development\debug\androidApp-development-debug.apk | Select-String "assets/partner-login.json"
```

Only if the archive listing contains `assets/partner-login.json`, launch with:

```powershell
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
```

If still empty, capture `./gradlew :androidApp:generateDevelopmentLoginPreviewAsset --info` and inspect `androidApp/build/generated/assets/developmentLoginPreview/partner-login.json` plus the APK listing. Do not claim success from `Status: ok` alone.
