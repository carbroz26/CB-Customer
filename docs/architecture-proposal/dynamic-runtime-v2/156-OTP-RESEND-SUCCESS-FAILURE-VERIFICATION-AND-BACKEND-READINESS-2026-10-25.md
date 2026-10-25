# 156 — OTP Resend success/failure verification and backend-readiness handoff

**Date:** 2026-10-25
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Status:** Source-authored test coverage and offline failure fixture; **Gradle and device verification pending**. Backend integration not approved as implemented.

## Scope and evidence
- Preserve Login v19 and OTP v3 existing visual and validation contracts, clock icon, six-box input, Store and RequestAction vocabulary.
- Owner verified countdown-driven Resend enabled/disabled behavior on Android A35; this does **not** imply successful Resend action or failure recovery was device tested.
- Existing `DynamicCountdownStoreTest` has a positive retained-challenge and timer-restart scenario. Added failure 429-like repository exception, retained challenge preservation, no timer restart, generic action-error display, subsequent success and duplicate in-flight guard.
- Dedicated `cbpartner.otp_resend_failure` Android development preview Intent extra causes only OTP preview's fake repository to return a failed Resend call. Web opt-in query `cbpartnerOtpResendFailure=1` does the same. Production app/network/business behavior unchanged.
- Error preview intentionally shows the **existing** generic action failure text `Unable to complete action`. It does not claim dedicated 429 UI or an actual HTTP 429 parser has been verified.

## Minimal device verification steps
Run `:data:jvmTest :app:jvmTest :feature:dynamic:jvmTest :androidApp:assembleDevelopmentDebug` and install the debug APK.

**Success mode**: launch `com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity` with `--ez cbpartner.otp_preview true`. Wait until `00:00`, tap Resend, require `00:10` restart and immediate Resend disabled.

**Failure mode**: launch same activity with `--ez cbpartner.otp_preview true --ez cbpartner.otp_resend_failure true`. Wait until `00:00`, tap Resend. Require `Unable to complete action` visible (existing out-of-form error UI), `00:00` not restarted and Resend still eligible for a subsequent attempt. Do not claim challenge retention visible on screen; regression test verifies retention.

Do not use the physical backend while testing 10-second local fixtures: backend enforces a 60-second cooldown.

## Backend source reconciliation (read-only)
- `CB-Backend/development` `domains/identity/application/AuthUseCases.ts` returns `challengeId`, `phoneNumber`, `expiresInSeconds`, `resendAfterSeconds`, `isNewUser`, `destination` after Send OTP. The current backend policy is 60 seconds, 6-digit OTP, 300-second TTL. Status 429 `OTP_RESEND_COOLDOWN` / `OTP_RATE_LIMITED` are backend-owned.
- Actual route registered at `POST /api/v1/partner/auth/send_otp`; request body `{phoneNumber,deviceId}`. The current Login v19 frontend JSON instead references `/api/v1/auth/send_otp` and sends `countryCode` too; do not silently assume live compatibility.
- Actual Verify route `POST /api/v1/partner/auth/verify_otp`; body requires `challengeId,phoneNumber,otp,deviceId`, whereas the current OTP v3 JSON uses `/api/v1/auth/verify` and `code`. These are real integration mismatches.
- Backend SDUI `sdui/engine/src/screens/partner/PartnerOtpScreen.ts` still builds the older UI; it does not emit the newly frozen OTP v3 layout/countdown semantics. Thus **live frontend UI parity is not yet established**, even after requests are aligned.
- Backend success envelope is `{status,code,message,data,traceId}`. Frontend RequestAction `retain` currently reads `data.challengeId` and `data.resendAfterSeconds` from successful response data, which matches the backend's send result contract.

## Explicit next integration gate
1. Device-confirm success/failure mock behavior and all focused Gradle tests.
2. Reconcile current frontend Login/OTP **request URLs and body fields** with backend schemas; no UI changes.
3. Backend authoring migration: emit the exact approved OTP v3 screen JSON, including `data.countdowns`, live retained state references and request guards; preserve screen/template identity and app architecture. Do not replace this with the historical old OTP layout.
4. Verify `Send OTP -> OTP screen GET -> Resend -> Verify OTP` under actual backend, authoritative cooldown, challenge replacement and error responses, with safe test accounts and no OTP/token logging.
5. Keep explicit gates for error-code-specific presentation, deep sleep/resume deadline synchronization and session/destination security; do not claim them resolved by the offline mock.

**No backend source was modified during this wave.**

---

## 2026-10-25 later owner-observed verification addendum (supersedes initial "pending device verification" status in this document)

The owner **later verified** both flows on Samsung Galaxy A35 Android development preview:
- Countdown completed, Resend became enabled.
- **Failure preview:** tapping Resend displayed `Unable to complete action`, with **no timer restart**.
- **Success preview:** tapping Resend restarted the timer at `00:10`.
- Previously confirmed: timer disabled Resend during countdown and enabled it at zero.

These are **device observations for the isolated preview fake repository**. They do **not** verify live Backend 429, actual SMS delivery, Backend challenge lifetime, JWT/session storage or authenticated navigation. A previously supplied `BUILD SUCCESSFUL` log was on a stale local head `a12fce76` during Git remote-ref corruption; capture a fresh latest-HEAD test result before claiming all new tests passed on that exact commit. User's subsequent on-device failure behavior proves the updated APK functionality, not necessarily a full Gradle test report.

**Next authority:** Document [157](157-LOGIN-OTP-BACKEND-FRONTEND-EXACT-CONTRACT-RECONCILIATION-FREEZE-2026-10-25.md) is now the **frozen target contract** for Backend integration; [158](158-2026-10-25-COMPLETE-CHAT-HANDOFF-ARCHITECTURE-RUNTIME-AND-INTEGRATION-GATES.md) explains whole-system ownership and remaining work. No Backend production integration is completed by this addendum.
