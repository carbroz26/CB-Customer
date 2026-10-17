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
