# 139 — Android host navigation-bar safe-area correction

**Date:** 2026-10-23  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Prior source:** `f51062adcbcc2534d6ed7d537f65edc77cddc707`  
**Decision:** User approved **first fixing the generic Android bottom navigation-bar overlap**, then reviewing logo/header/hero geometry.  
**Status:** GitHub source committed; compilation and device visual acceptance pending.

## Evidence and source audit

The owner-provided Samsung A35 scrcpy screenshot shows Login v11 `Terms & Conditions | Privacy Policy` under the system three-button navigation area. APK builds/launch and v11 reserved-validation-space removal were confirmed by the owner before this change.

The native `androidApp/MainActivity.kt` previously called `setContent { App(...) }` without a parent navigation-bar inset. This Android app targets API 36. On Android 15+ edge-to-edge can place app content behind the system bar. Dynamic FormTemplate already applies JSON-authored spacing; injecting hardcoded bottom dp into Login, FormFieldSection, or components would violate backend styling ownership.

## One-boundary implementation

The Android-native `MainActivity.setContent` now wraps its single application content in:
```kotlin
Box(Modifier.fillMaxSize().navigationBarsPadding()) { /* same App/retention probe */ }
```

- The Android Activity host is the one place responsible for platform navigation inset. This is **not a Login-specific workaround**: preview and normal bootstrap/navigation share it, across all Android development/staging/production builds.
- `navigationBarsPadding()` adapts to three-button/gesture navigation and consumes the inset so nested consumers do not inadvertently double-pad. It does **not** impose a fixed dp value from Kotlin.
- No explicit status-bar top padding has been added; logo/status offset is still an independent next audit. Nav inset may change the available template height and thus photo crop and card position; recheck those after device verification.
- The system inset is separate from the JSON-authored Login card bottom padding 24dp; that value is preserved.
- Native Android-only change: Web behavior, Kotlin common Dynamic renderer/Store, JSON v11, Country Picker, hero image, actions, theme/palette and validation remain untouched.
- The Android system navigation area is outside the padded application content. Verify its background and contrast on the phone; any appearance change would need a separate platform decision, not an assumed color default.

## Acceptance

```powershell
cd D:\CarBroz\CB-Partner
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
```

**On Samsung A35:** verify Terms and Privacy links render wholly above the three-button navigation bar, retain authored card bottom spacing, and remain clickable; note that logo top/hero/card may shift due to viewport height change. Change phone navigation mode to gesture temporarily to check adaptive inset (do not hide system nav in code). Open keyboard and country picker to spot regressions; screenshot/owner acceptance is the gate. A successful Gradle build alone is not visual acceptance.

## Deferred after gate

1. Compare actual native Login after inset against owner original reference for status-bar-to-logo, logo box/title baseline, welcome/subtitle alignment/fonts, card vertical geometry, and Hero worker+car+trees visible frame.
2. Reconcile approved design board vs standalone image where palette/font/geometry conflict. Do not silently change those values.
3. P1 other SRP cleanup remains outside scope.

## 2026-10-23 owner build log: compilation dependency correction

The owner built commit `445cfa7a986c9ed64f3f28acc25002310c6f215f` and `:androidApp:compileDevelopmentDebugKotlin` failed: `Unresolved reference 'foundation'`, `Box`, `fillMaxSize`. The root cause is that `androidApp` previously declared only `libs.androidx.activity.compose` and did not expose the Compose Foundation layout library needed by the activity's new `navigationBarsPadding`/Box imports. Downstream composables in `:app` or `:feature:dynamic` do not make these symbols directly available for `:androidApp` compilation.

Narrow correction: apply the **existing** `cbpartner.compose` convention plugin in `androidApp/build.gradle.kts`, and declare `implementation(compose.foundation)`. Compose dependencies already use this convention in `:feature:dynamic`. No change to Android Activity UI, app behavior, other modules, JSON, validation, Country Picker, Hero or visual spacing. **Build remains unverified until the owner reruns the Gradle command.**

## 2026-10-23 cross-platform architecture correction / latest state (see Doc 140)

The owner explicitly established **Kotlin Multiplatform + Compose Multiplatform commonMain-first** across Android/iOS/Web; platform-specific source only for genuinely OS-bound functionality, after source ownership review. The current MainActivity-only navigationBarsPadding is a **provisional Android workaround**, not a final shared-layout decision. Do NOT just move it blindly into app/commonMain: first audit full-bleed Hero/background vs interactive content safe insets, platform-specific inset ownership/consumption, Web behavior, iOS root and modal/keyboard effects; minimize duplicated padding. Android build after the previous missing-Foundation fix at commit 21229a450e4c001554c70ffbdeef982d1bf415ab has **not yet been confirmed by the owner**. Before visual parity work: build, launch special JSON preview, verify bottom Terms and nav inset on Samsung. Coordinate commonFirst change independently of product validation/phone policy.
