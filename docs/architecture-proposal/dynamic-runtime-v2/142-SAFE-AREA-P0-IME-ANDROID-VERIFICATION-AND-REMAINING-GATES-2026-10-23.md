# 142 — Safe-Area P0.1: Android Login IME verification and remaining gates

**Date:** 2026-10-23
**Repository/branch:** `carbroz26/CB-Partner` / `architecture/dynamic-runtime-v2-discussion-freeze`
**Verified implementation HEAD prior to this record:** `d8c98f3861f1f767100730b1fa45c89f41ca3d2b`
**Scope:** Record owner-provided native Samsung A35 evidence without expanding the already implemented two-file keyboard change.
**Status:** **Android normal Login system-bar safety + focused Login keyboard gap: device visually verified.** Cross-platform Safe-Area P0 is **not fully frozen**; later-build Country Picker/IME regression, Android gesture navigation and iOS runtime remain separate acceptance gates.

## Implemented source and ownership

1. Earlier common-first P0 commit `130d15568b492897c2dc4a4fe15258c4d859f987`:
   - `feature/dynamic/.../sdui/template/FormTemplate.kt`: template background remains full viewport; interactive template foreground uses shared Compose `safeDrawingPadding()`.
   - `feature/splash/.../SplashScreen.kt`: shared safe drawing boundary around foreground while decorations remain full-bleed.
   - `androidApp/MainActivity.kt`: removed Android-only `navigationBarsPadding()` wrapper.
   - `androidApp/build.gradle.kts`: removed the now-unneeded direct Compose Foundation dependency.
2. P0.1 commit `d8c98f3861f1f767100730b1fa45c89f41ca3d2b`:
   - `FormTemplate.kt` retains `safeDrawingPadding()` and adds `imePadding()` on the common foreground box, preserving the same scroll/spaceBetween and backdrop renderer.
   - `androidApp/src/main/AndroidManifest.xml`: sets `MainActivity` `windowSoftInputMode="adjustResize"`.
3. No Login JSON, Country Picker JSON, DynamicStore, FormFieldSection, InputElement, action handler, validator, backend, or navigation contract changed in these commits. No new manager/renderer/platform Login UI was introduced.

## Owner device and build evidence

- Before P0.1 the Samsung screenshots showed mobile-number keyboard-open positioning with unexpected blank/hero gap below the form; Country Picker and search had been exercised on the earlier safe-area build.
- The owner pulled the P0.1 commit, ran `:feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack :androidApp:installDevelopmentDebug`, and provided an IDE terminal screenshot showing **BUILD SUCCESSFUL** and a successful Android app launch; detailed per-test results are not independently available here.
- The new Samsung A35 screenshot shows the focused mobile-number input, Continue and legal text in the foreground immediately above the IME/keyboard toolbar, with no previous unwanted gap. The owner explicitly confirmed in chat: **"ab keyboard wala issue solve ho gaya he"**. This is **device-accepted Login keyboard behavior**.
- Earlier screenshots of normal Login at commit `130d1556` established navigation links above the three-button system navigation area, and the template backdrop remains visible. New screenshots do not establish final Login artwork, typography or crop parity.

## Still-open verification (do not assert passed)

1. Country Picker search and keyboard-open regression **on commit `d8c98f38`** (earlier-build screenshots alone are not evidence for the latest code).
2. IME dismissal restores the prior Login layout; focused field and Continue remain accessible during keyboard transitions; validation-error/keyboard combinations.
3. Android gesture navigation variant (current device screenshots use three-button mode).
4. iOS real runtime safe-area/IME/dialog behavior requires macOS; Web browser layout regression is not a substitute for iOS testing.
5. Device appearance of system bars and hero crop; the latter belongs to later Login visual-parity work, not the Safe-Area P0.1 fix.

## Next work (separate gate)

- Complete only the targeted remaining regressions above; mark Android Safe-Area P0 accepted independently from unexecuted iOS device checks.
- Then audit **Generic Validation Architecture**: the existing `app/validation/PartnerPhoneNumberValidator` currently implements `phone_number` with India-specific checks and is registered from `App.kt`. Login v11 refers to that validator by key and passes `$binding.countryId`; `DynamicFormValidator` already handles required/pattern/named validators and clear-on-edit. Reconcile the actual selected backend phone/country and send-OTP contracts before any removal or migration. Do not duplicate domain validation in Dynamic, guess international phone policy, or edit backend without approval.
- Login visual parity (logo, alignment, hero crop, colors) follows the validation architecture gate.

**Rule:** This document records a confirmed Android Login fix and honest remaining gates; it is not permission to redesign the six-category Dynamic hierarchy, the Store, actions or UI JSON.
