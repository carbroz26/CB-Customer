# 153 — OTP v3 generic runtime and isolated preview integration

**Date:** 2026-10-24
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `90a933260d99fac676b5c8f0edc4bfc78c1d06f6`
**Status:** Implemented source/fixture wiring. GitHub changes are not proof of local JVM/Android/Web builds or Samsung visual acceptance. OTP v3 contract remains **draft** until device parity and owner review.

## Scope and ownership

The source remains Kotlin Multiplatform, Compose Multiplatform, Clean Architecture, MVI/UDF with one existing `DynamicStore` and one existing `DynamicFormValidator`. There is no OTP-specific component, template, Store, view-model, validator or duplicate renderer.

1. `FormFieldSection` keeps an optional typed label; a direct control is one bound Input with zero/one label. Composite controls still accept only a single Group with one Input and zero/one label. A missing label is not faked with a blank TextElement. `errorBorder` becomes optional: Login's JSON-authored field errorBorder retains precedence; OTP has no enclosing red outline. Conditional error message UI remains identical to Login, **never reserves hidden space**.
2. `DynamicInputPresentation.placeholder` retains the authored dash. `DynamicValidationStyle.border` retains the authored red per-segment border. In error state all six slots tint and border together; no field border outside them.
3. Segmented inputs continue to be one authored `InputElement` and one binding. Inside the generic renderer each cell is separately focusable/editable, with normal numeric IME, select/replace, sequential entry, deletion of occupied slot without shifting, empty-cell physical backspace moving to previous, and position-preserving paste into available slots. The one binding encodes temporarily empty interior slots with spaces, which fail a backend-authored full-match 6-digit pattern. No raw digit can pass validation until every cell is populated. This internal convention is **not** an OTP-specific Store field and should be reviewed with IME screenshots. Android soft-keyboard backspace on a completely empty cell needs device proof.
4. Input renderer/layout remains otherwise shared; existing Login phone visual behavior should be visually regression tested. Field validation and request suppression remain owned by the existing validator/Store.
5. Native and Web use one generic `LocalDynamicPreview` parser that feeds the real `DynamicResponseMapper`, normal application composition, dynamic Store and renderer. The original Login preview is preserved. Android developmentDebug generated assets copy both `partner-login.json` and `partner-otp.json` from the canonical Web resources; no redundant handwritten platform fixtures. Native preview intent `cbpartner.otp_preview`, Web query `?cbpartnerOtpPreview=1` are development-only.
6. OTP development preview injects fake retained state `phoneNumber=9876543210` and `challengeId=development-preview` for display/navigation rendering, not backend network. The offline repository rejects every request except loading its own fake screen; a failed Verify/Resend HTTP request is not product behavior.
7. `icons/clock` remains the registered temporary error-alert icon alias from Doc151; Resend text is static/nonclickable as v3. The JSON, action vocabulary and login theme/colors are untouched.

## Bounded verification

Local command:
```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.otp_preview true
```
Run ADB only after `BUILD SUCCESSFUL` to avoid launching an older installed APK. Login regression:
```powershell
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
```
Web: `?cbpartnerOtpPreview=1`, with normal Development Web run. OTP validation scenarios: empty -> required; 1–5 digits -> pattern; 6 digits -> no client error; editing clears error; invalid red backgrounds/borders for all six; normal no invisible validation spacing; filled-cell replace; middle-cell delete keeps other positions; empty-cell backspace; multi-digit paste; keyboard open; 318dp row fits Samsung A35; OR leading/trailing lines; static timer and Resend; change-phone pop. Recheck Login on same commit.

**Unfrozen:** production OTP send/verify backend compatibility, live countdown/cooldown, resend request, session/token, disabled button policy, error states beyond client pattern, real clock PNG. Do not claim v3 fully frozen after source commit alone.
