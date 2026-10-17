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


## Owner Android/Web runtime evidence — 2026-10-17

Owner installed Android debug APK on Samsung SM-A356E: `:androidApp:installDebug` BUILD SUCCESSFUL in 48s (150 actionable tasks), installed on one device; `adb shell am start -n com.carbroz.cbpartner.android/.MainActivity --ez cbpartner.w5_preview true` returned Starting Intent. Web `:webApp:wasmJsBrowserDevelopmentRun` served at `http://localhost:8080/`, webpack 5.108.1 compiled with one warning in 2986 ms and remained running/idle. Owner provided screenshot of `http://localhost:8080/?cbpartnerW5Preview=1` and confirmed phone shows the same preview.

**Observed in Web screenshot:** isolated header/body/footer text appears in proper vertical order, Footer near bottom of gradient area, button gradient and four colored segmented input boxes visible, and screen background gradient present. This is **manual Android/Web visual smoke PASS for fixture reachability, slot order and visible gradient rendering**. Neither button clicks nor preview switching between `Body only` / `Empty slots` nor accurate independent padding measurement are proven by the provided screenshot. A screenshot does not independently verify precise angle/stop positions, platform pixel parity, input keyboard behavior, Coil URL→fallback→local placeholder, or iOS behavior. These remain explicit W5 acceptance limits, not observed defects.

**Build boundary:** The owner's prior `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` and later `:feature:dynamic:jvmTest :app:compileKotlinWasmJs :androidApp:assembleDebug` were successful on pre-preview commits, while the Web webpack preview build and Android install reported above demonstrate preview artifact execution. Do not invent a separate preview-specific full JVM suite result.

**Next narrow manual smoke:** Click `Body only` and `Empty slots` in both hosts; verify expected absence of Header/Footer and no layout shifts in empty mode. Confirm entered digits survive preview mode switch only if that is intended by harness behavior. Leave renderer and production Bootstrap unchanged unless a concrete repro appears. Temporary preview hooks must be removed or explicitly isolated before production closure.


## Three-mode owner follow-up — 2026-10-17

Owner supplied three Web screenshots of the opt-in W5 preview: (1) Header + Footer from prior evidence, (2) **Body only**, with header/footer absent and the body/button/segmented input rendered, and (3) **Empty slots**, with the same body rendered and no header/footer text. The segmented input's displayed digits changed in the third screenshot, supporting a basic input-interaction observation. Owner explicitly reports equivalent behavior on physical Android. **Scoped acceptance: PASS** for visually observable slot order, body-only compatibility, empty-slot rendering, visible screen/button/input gradients, and manually reported Android/Web consistency.

**Not proven:** exact padding measurement, pixel-accurate 135-degree angle/stop interpolation, Coil primary/fallback/local assets, true cross-platform pixel equivalence, or iOS runtime. No claim of separate screenshot attachments stored in repository. W5 preview is still present as explicit debug/query-only test harness; remove it in a separate scoped cleanup before final production closure, preserving the unrelated G1 retention probe unless its own gate authorizes removal. No new renderer change justified by these screenshots.
