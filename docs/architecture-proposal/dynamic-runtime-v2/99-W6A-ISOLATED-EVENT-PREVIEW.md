# W6-A isolated event-probe instructions

**Status:** Temporary preview source committed; build and Android/Web native acceptance not yet verified.

This opt-in harness renders real Dynamic element renderers with static domain fixture values and records `onAction` and `onValueChange` callbacks. It does **not** execute the Store, ActionExecutor, backend or navigation. A logged `state` action is a diagnostic marker, not a State operation. The preview must not be used as evidence of end-to-end Store snapshot correctness.

Android (install debug build first):
```powershell
adb shell am force-stop com.carbroz.cbpartner.android
adb shell am start -n com.carbroz.cbpartner.android/.MainActivity --ez cbpartner.w6_preview true
```
Web: open `http://localhost:8080/?cbpartnerW6Preview=1` after `:webApp:wasmJsBrowserDevelopmentRun`.

Verify each host:
1. Tap button once: one `button.click`; long-press once: one `button.longClick`, **no** additional click.
2. Tap plain Text and long press: one matching event per gesture; tap clickable link span: `span.click`.
3. Focus regular field: exactly one `field.focus`; type a character: value updated before `field.change` in the log; press IME Done: one `field.submit`. Blur should not fire another `field.focus`.
4. Focus OTP: one `otp.focus`; entering fourth digit emits `otp.change` but no `otp.submit`; IME Done emits `otp.submit`.
5. Reset/clear log and repeat; verify no events on redraw without interaction.

Pending beyond this harness: combined span + parent element gestures, production Store intent/binding snapshot ordering, image/icon/content gestures, iOS native tests, and in-app navigation/effect delivery. Do not claim these PASS solely from this fixture. After evidence, remove harness and host hooks in a separately scoped cleanup.

Compile gate: `:data:jvmTest :feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug`.


## Owner-reported runtime evidence and scoped cleanup — 2026-10-17

Owner reported the W6 isolated fixture compiling and running on Android and Web: `:data:jvmTest :feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` BUILD SUCCESSFUL in 31s, 189 actionable tasks (26 executed, 163 up-to-date); Android debug APK installed on Samsung SM-A356E (one device), BUILD SUCCESSFUL in 46s; webpack 5.108.1 served Web preview at localhost:8080 (one nonblocking warning). Owner stated same behavior on Android and Web. Observed event logs include `text.click`, `text.longClick`, `span.click`, updated regular input value followed by `field.change`, and `field.submit`. Owner additionally confirms OTP submits only on explicit keyboard Done (not last digit), and keyboard dismisses on Done. Do not infer tests for unsupported interactions from these observations.

**Deferred by explicit owner choice:** Outside click to dismiss keyboard/focus (currently not working in observed context). The existing screen-root pointer handler remains untouched by this cleanup; it is not accepted as correct and needs a separate focused investigation. **Still pending evidence:** Button click/long-click nonduplication, `field.focus` / `otp.focus` transition counts, image/icon/content gesture behavior, concurrent Text parent + clickable span gestures, production Store binding snapshot, navigation/action effect delivery, and iOS. The fixture only logged renderer callbacks and did not execute actions through production Store.

**Temporary harness cleanup:** Delete `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/W6EventPreview.kt` and only W6-specific Android/Web opt-in branches/imports. Preserve the unrelated `RetainCompatibilityProbe`, normal `App` entry path, all renderer/event source and backend untouched. **Post-cleanup build not yet run by owner**; required gate `:feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug`. W6-A remains **PARTIALLY VERIFIED / NOT FULLY CLOSED**.
