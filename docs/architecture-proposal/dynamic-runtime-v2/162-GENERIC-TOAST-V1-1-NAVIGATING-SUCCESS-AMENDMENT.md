# 162 — Generic Toast V1.1 — Login Send and OTP Resend Success Amendment

**Owner approval:** 2026-10-26. **Baseline:** fa6a59bd9a9d949c45226a4a09504177171f8a2a on `architecture/dynamic-runtime-v2-discussion-freeze`. **Status:** frozen scope; implementation and consolidated device verification required. **Backend:** `CB-Backend/development` unchanged.

## Objective and precise contract

Reuse the existing V1 `ToastPresentation`, app-root renderer, white progress track, small tick, 0%-to-100% 5-second auto-dismiss. Enable the **same optional request field** `successPresentationId` for both existing response modes:
- `responseMode:"none"` (OTP Resend): commit retained challenge/countdown, then emit one local Toast from current screen only on successful request. No presentation field means no Toast.
- `responseMode:"destination",mode:"push"` (Login Continue): on successful Send OTP response with valid `data.destination`, capture `otp_sent_success` Toast **from original Login screen before navigation**, atomically retain challenge/recipient, enqueue ordered Navigate then Toast effects in the **same existing session batch**. Only deliver associated Toast when navigation succeeds. Do not replay it after failed nav or rotation.
- Request failures, validation errors, absent or invalid Toast presentation, stale actions or missing destination never show success Toast. A destination response with immediate `presentation` remains invalid, as in V1; do not add a new response-level combined contract. No new action, executor, Store, navigation queue or renderer.
- Login uses `A (first) OTP has been sent to`, OTP Resend uses `A new OTP has been sent to`. Text, icon, green/white styling and progress remain completely JSON authored; the generic Toast renderer has no auth-specific branching.
- **No phone/OTP logging**. Login Toast uses existing initial Login country-code/mobile-number bindings from source screen (no extra live Backend field); Resend retains current preview formatting pending backend migration. Backend may omit `successPresentationId` per authored screen action to suppress a Toast. Per-request response-driven enable/disable remains a separate future contract decision.

## Approved file manifest

1. `feature/dynamic/.../action/RequestActionHandler.kt`: accept `successPresentationId` for both modes; keep response-level presentation and navigation mutually exclusive; return existing `Navigate` result with optional authored ID.
2. `feature/dynamic/.../action/DynamicActionResult.kt`: optional ID on existing `Navigate`; normal navigation unaffected.
3. `feature/dynamic/.../store/DynamicStore.kt`: resolve `ToastPresentation` from **origin screen**, snapshot binding/context/state before navigation; commit state + ordered `Navigate`, `Toast` in one accepted batch. No double state commit.
4. `app/.../ApplicationContent.kt`: when navigation dispatch fails, suppress any Toast in the same batch; keep existing session consumer/ack contract, no alternate host. `DynamicSession.kt` remains unchanged.
5. `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v19.json` and `webApp/src/wasmJsMain/resources/preview/partner-login.json`: identical controlled amendment. Add `otp_sent_success` complete Toast presentation and optional success ID in Continue action. Keep canonical `tpl_P101L` and Login/Country Picker UI unchanged. OTP `otp_resend_success` JSON remains unchanged.
6. Preview acceptance requirement: current opt-in Login preview cannot simulate successful Send OTP navigation or load OTP, while OTP Resend preview can. Add **only necessary development-only routing** in existing `App.kt`, `LocalDynamicPreview.kt`/Android `MainActivity.kt` to return authoritative OTP fixture on successful Login preview, without touching production networking or adding mock UI. This is source-backed additional scope required for actual Samsung A35 end-to-end verification; no separate module.
7. Focused `RequestActionHandlerTest`, `DynamicStoreTest`, and app-level effect/preview tests; consolidated Gradle build/tests and Samsung A35 check. Do not claim green build before evidence.

## Acceptance
- Login success → OTP destination (not blank) → **A first OTP has been sent to ...**, from original Login bindings, only once. Resend success → **A new OTP ...**, same root renderer. Both 0 to 100% five-second progress and dismiss; screen interaction remains available.
- Network failures/invalid form/nav rejection produce **no success toast**; login without `successPresentationId` navigates without Toast.
- Existing six Dialogs, Splash, Country Picker, OTP box/cooldown/back behavior, rotation retention, Backend untouched.
- Android + Web compile/tests, then device screenshots and navigation/rotation/failure test. Static GitHub diff does not equal runtime acceptance.

## Implementation checkpoint — 2026-10-26

V1.1 source committed on `architecture/dynamic-runtime-v2-discussion-freeze`. Changes limited to `RequestActionHandler`, `DynamicActionResult.Navigate`, `DynamicStore`, the already-existing app effect consumer, the approved Login JSON + mirror, existing opt-in Android/Web preview loading, and focused tests. Navigation and Toast are accepted in one ordered `DynamicSession` effect batch. If navigation dispatch fails, only the associated Toast from the same batch is suppressed; session/host/DI are not duplicated. Login Toast message reads `$binding.countryCode` + `$binding.mobileNumber` from **original Login** bindings, Resend Toast V1 remains authored separately. Canonical Login `tpl_P101L`, OTP `tpl_P102O`, existing six Dialogs and Backend remain unchanged.

**Evidence limit:** Repository/source-level inspection only. Build, unit tests, actual OTP navigation success and visual screenshot are NOT verified in this context. Do not treat source commit or this note as runtime acceptance. To verify, run consolidated JVM+Android+Web tasks, install developmentDebug, then test `--ez cbpartner.login_preview true` valid phone + Continue, `--ez cbpartner.otp_preview true` Resend after countdown, and failure mode; validate correct text, no success Toast on failed request, no duplicate after rotation.
