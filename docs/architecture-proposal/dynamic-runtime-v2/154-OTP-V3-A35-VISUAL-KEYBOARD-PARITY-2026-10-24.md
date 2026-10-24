# 154 — OTP v3 A35 screenshot and keyboard parity fixes

**Date:** 2026-10-24
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `35bd8618f39e88ebddeb612fe8b690b61efdb7c9`
**Status:** Source implementation authored; Gradle/device parity unverified. OTP v3 remains a draft, Login v19 is frozen and untouched.

## Owner-provided Samsung A35 screenshot findings (10 items)

1. OTP title wraps. v3 JSON now uses 32sp, lineHeight40 while preserving the authored 800 weight and #0A337F, for a one-line A35 target. No hardcoded Compose font adjustment.
2. Inline error currently centers under the six boxes. v3 JSON changes `FormFieldSection.horizontalAlignment` to `start`; the existing generic field renderer therefore left-aligns the conditional message like Login. No renderer change, no hidden reserved space.
3–5,7. After last digit, Backspace, Done and outside tap, keyboard closes and reopens. Root structure was six separately focusable native `BasicTextField` nodes and a `LaunchedEffect(value, targetFocus)` that could refocus after user dismissal. Generic segmented rendering now owns exactly one native `BasicTextField` across six separately selectable *painted* cells, using `TextFieldValue` selection instead of transferring native focus. No delayed `LaunchedEffect`. The segmented field always offers Done, which clears focus and hides IME; normal input behavior is left unchanged. Filled-cell deletion clears only that position; empty-cell Backspace moves selection left; paste overwrites from the selected slot and truncates excess. One `otp` binding, no OTP-only handler/Store.
6. The timer icon appeared at far left because the existing Text renderer distributes a `fillMaxWidth` text's remaining width via weight, leaving the leading icon detached. v3 removes `fillMaxWidth` from this timer's TextElement and centers the parent `StackSection`; its `icons/clock` leading accessory now sits next to the timer text, using the existing renderer and resource alias. There is no independent image widget or new icon mechanism.
8. Subtitle-to-phone gap reduced with subtitle lineHeight24 and bottom spacing0; phone lineHeight24 and bottom spacing8. Typography colors/weight unchanged.
9. Card bottom padding reduced 24→12 so the help text is nearer to the card bottom.
10. Section spacing tuned: field bottom16→12, timer bottom14→10, separator bottom12→8, resend minHeight32→28 and bottom16→12, verify bottom12→10. Logo alignment switched to `start`, matching the provided reference. Other Login design tokens, card radius, color, input dimensions and primary button style are preserved.

## Deliberate boundaries

Only these existing locations change:
- `feature/dynamic/.../sdui/element/InputElement.kt` — generic segmented keyboard/focus implementation and pure decoder.
- `feature/dynamic/.../sdui/element/SegmentedEditTest.kt` — native edit replacement/paste/delete regression.
- `docs/.../partner-otp-ui-contract-v3.json` and exact matching `webApp/.../preview/partner-otp.json` — authoritative OTP-only authored visual settings.
- This decision record.

**Untouched:** Login v19 contract and preview; Generic `FormFieldSection`; other DTO/domain/mapper, app preview composition, backend, navigation, all other screen contracts. Timer and Resend remain static as approved; real clock upload and live cooldown are pending.

## Verification gate

The user runs only real-device testing. Build/Gradle commands:
```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :feature:dynamic:jvmTest :data:jvmTest :feature:dynamic:compileAndroidMain :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.otp_preview true
```
Check A35 title one line, six boxes and error start-edge, no error space while valid, close-on-Done/outside without reopen, soft-keyboard Backspace both filled/empty, number input/paste/replacement, timer accessory adjacency, subtitle/phone gap, footer/card padding, and overall card position. Then rerun the Login preview for regression. If Wireless ADB install times out, reconnect ADB and use the already built developmentDebug APK or Gradle install only after device responds. **Do not claim this wave is device verified or contract frozen until the user reports it.**
