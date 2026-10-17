# W5-001 — Isolated Android/Web Visual Preview

**Status:** Preview source created for owner runtime verification; no visual PASS claimed. **Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.

This source-only test harness is reachable only by explicit Android debug intent `cbpartner.w5_preview=true` or Web query `?cbpartnerW5Preview=1`. The ordinary Bootstrap/DI/session/navigation/API path remains unchanged. The fixture is programmatic domain-level data, not a network response, and therefore validates renderer behavior rather than mapper/transport correctness.

Modes: Header + Footer, Body only, Empty slots. Rendered content has a four-stop 135-degree screen background, button and segmented Input. Header/Footer labels and body label allow visual order/padding checks. The fixture has no backend dependencies.

**Android after installation:**
```powershell
adb shell am force-stop com.carbroz.cbpartner.android
adb shell am start -n com.carbroz.cbpartner.android/.MainActivity --ez cbpartner.w5_preview true
```
To return to normal app: `adb shell am force-stop com.carbroz.cbpartner.android` then standard launcher `adb shell monkey -p com.carbroz.cbpartner.android -c android.intent.category.LAUNCHER 1`.

**Web:** Run the existing `:webApp:wasmJsBrowserDevelopmentRun` task and append `?cbpartnerW5Preview=1` to the served application URL. Omit the parameter to open the normal application.

**Acceptance:** Visually compare Header/Body/Footer order, body-only and empty-slot behavior, independent padding, four distinct color transitions, angle convention, equal gradient treatment for Button and segmented Input, Android/Web parity. Actual pixel-perfect angle convention and Coil primary/fallback/local asset network failure are **not** proven by this fixture. Those require dedicated asset responses or tests and should not be marked PASS from this harness. iOS requires macOS.

**Verification required:** `:app:compileKotlinWasmJs :androidApp:assembleDebug` and Android/Web manual evidence before acceptance. Test hooks are temporary and excluded from production closure.
