# 157 — Login v19 / OTP v3: Backend ↔ Frontend Exact Contract Reconciliation Freeze

**Date:** 2026-10-25
**State:** SOURCE-RECONCILED, TARGET CONTRACT FROZEN; INTEGRATION **NOT** IMPLEMENTED.
**Authority:** Source at the pinned heads below + the latest approved Dynamic Runtime V2 documents. Older screenshots, historical Backend OTP builder, and older frozen documents are not a license to revert newer approved UI.
**Frontend:** carbroz26/CB-Partner, branch architecture/dynamic-runtime-v2-discussion-freeze, SHA 146c208e2cb4744dafa7138fd588706c95d516c9.
**Backend:** carbroz26/CB-Backend, branch development, SHA b67b36141aa6ae9d0488c2582b0114d2abb09c86.
**Scope:** Guest startup → Login screen → Send OTP → OTP screen → Resend OTP → Verify OTP request and response-boundary handoff. This document records source facts, freezes the target wire contract, and separates unresolved launch gates. **Documentation only: no application source or backend code modified here.**

## 1. Evidence and supersession

Current visual/JSON authority:
- docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v19.json — Login v19 exact approved document, mirrored by webApp/.../preview/partner-login.json.
- docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json — OTP v3 exact approved document, mirrored by webApp/.../preview/partner-otp.json.
- Docs 151–156: latest OTP visual, keyboard/validation, countdown, success/failure preview decisions and source-level handoff.
- Docs 04, 12, 20, 62–65: reference/action/schema semantics; later approved source/doc decisions supersede earlier drafts where contradictory.
- Frontend: BootstrapMapper, DynamicResponseMapper, TemplateMapper, RequestActionHandler, DynamicStore, DynamicSession, DynamicRenderer, DynamicCountdownClock, LocalDynamicPreview, PartnerCountryCatalog.
- Backend: GetPartnerBootstrapUseCase, PartnerLoginScreen, PartnerOtpScreen, SduiModel, SduiBuilder, Action, Partner screen/auth routes/controllers, AuthUseCases, auth.dto, AuthSecurityPolicy, ResponseHelper.

Rules that cannot regress:
- Compose Multiplatform, Clean Architecture, Pure Store/MVI/UDF, SRP; keep single Dynamic Store, ActionExecutor, navigation/session owner and existing typed renderer registries.
- One Screen → one Template → multiple Components → Sections/Groups/Elements/Content under the currently supported concrete typed hierarchy.
- Do not add OTP-specific renderer, TimerElement, duplicate networking/state layer, ViewModel or speculative generic abstractions.
- Runtime references used for this flow are $binding, $context, $state and $literal. Do not introduce $response into Frontend JSON.
- Preserve Login v19 and OTP v3 appearance **exactly** (assets, sizes, colors, spacing, validation, country picker, OTP single segmented input, 6 boxes, timer icon and conditional errors). Only wire/action/data revisions approved below; do not silently replace the screens with the old Backend builders.

## 2. Source-truth discrepancy matrix

| Boundary | Current Frontend v19/v3 | Actual Backend development | Frozen target / owner |
| --- | --- | --- | --- |
| Guest bootstrap destination | Login screen partner_login, template tpl_P101L, form_template | Default config guest template **tpl_P101L** | Backend startup configuration MUST return tpl_P101L for new approved Login screen. Validate persisted config too; endpoint remains /api/v1/partner/screen/auth_login |
| GET Login document | Exact Login v19 JSON including country_picker | PartnerLoginScreen still old brand/layout and tpl_P101L | Backend GET emits approved v19-equivalent screen ID, template ID/type and visual/behavior structure |
| Send OTP POST | Login action endpoint **/api/v1/auth/send_otp**, body countryCode/phoneNumber/deviceId | Registered **/api/v1/partner/auth/send_otp**. SendOtpSchema parses phoneNumber/deviceId | Backend-authored Login action calls partner URL with phoneNumber=$binding.mobileNumber and deviceId=$context.deviceId; countryCode remains UI state but is NOT an additional SendOtp request field |
| Send OTP response | RequestAction mode=push, responseMode=destination, retain challengeId/phoneNumber/resendAfterSeconds | SendOtpResult contains all 3 + expiresInSeconds/isNewUser/destination | Keep current retain mapping, responseMode and push; backend response remains canonical status/code/message/data/traceId |
| OTP destination | partner_otp, tpl_P102O, form_template | SendOtpResult currently points to partner_otp/tpl_P102O/form_template, /api/v1/partner/screen/auth_otp | Preserve exact four-field identity and GET path; destination must match loaded screen |
| GET OTP document | OTP v3, includes countdowns, FormFieldSection and live TextElement countdown | PartnerOtpScreen still old UI; Backend screen schema lacks countdowns | Backend emits exact approved OTP v3, no historical OTP layout |
| Resend POST | /api/v1/partner/auth/send_otp, request phoneNumber=$state.phoneNumber and deviceId=$context.deviceId, guarded by countdowns.resend.ready | Same SendOtp route/body; Backend 60-second cooldown | Already compatible action URL and request body; keep responseMode=none, retain new challengeId/resendAfterSeconds and restart only on success |
| Verify POST | **/api/v1/auth/verify** with **code=$binding.otp** | **/api/v1/partner/auth/verify_otp** with required **otp**, challengeId/phoneNumber/deviceId | Backend-authored OTP v3 uses partner URL and otp=$binding.otp; preserve existing 6-digit validation; no new action |
| Verify success | Expects destination via existing RequestAction | VerifyOtpUseCase/Controller emits token, refreshToken, destination partner_dashboard/default_template | Destination is a four-field response; **authenticated session storage and next-screen renderability are separate blocking gates** |
| Country picker live data | Approved country_picker bottom sheet/filtered ListSection; development preview provides 8 countries | Frontend production PartnerCountryCatalog() has **no approved countries snapshot**; Backend old Login has no picker | Preserve picker UI, but establish backend-vetted supported region/country dataset and source ownership before declaring live country selection complete |
| Action/schema parity | Frontend request guard enabledWhen, pop navigation via payload.mode, present via payload.presentationId, dismiss without targetId | Backend Action/SduiModel uses a different present/dismiss/navigate shape and request payload schema lacks enabledWhen | Reconcile Backend authoring/schema to the current frozen Frontend JSON; do NOT add parallel Frontend adapters or fake actions |

## 3. Frozen envelope, identity, routes

Base envelope for these HTTP responses is:
~~~json
{
  "status": 200,
  "code": "SUCCESS",
  "message": "Request completed successfully.",
  "data": {},
  "traceId": "optional-trace-id"
}
~~~

Trace ID is optional; the exact message copy varies by operation. Status/HTTP failures retain the established Backend response/error contract. A screen response's data is a screen, not a request-result object. Action result data includes retained fields/destination, and does not masquerade as screen DTO.

**Four-field navigation destination** (not historical six fields):
~~~json
{
  "screenId": "partner_otp",
  "templateId": "tpl_P102O",
  "templateType": "form_template",
  "endpoint": "/api/v1/partner/screen/auth_otp"
}
~~~

Backend SduiModel.dynamicDestinationSchema and Frontend Domain DynamicDestination currently use **four** fields; method/authentication belong to request payloads, not the current destination model. Old docs asking for six destination fields are superseded for this implementation. DynamicRenderer actively checks returned screenId/template.id/template.type equality with destination; even a visually perfect screen will not render if these IDs differ.

**Guest startup**: GET /api/v1/partner/config/bootstrap → data.startup.destination = partner_login / tpl_P101L / form_template / /api/v1/partner/screen/auth_login. Backend GetPartnerBootstrapUseCase default currently uses tpl_P101L and persisted config may override it; both need reconciliation.

**GET Login**: GET /api/v1/partner/screen/auth_login → approved Login v19 data (screenId partner_login, schemaVersion 3.0.0, targetApp PARTNER, template.id tpl_P101L).

**GET OTP**: GET /api/v1/partner/screen/auth_otp → approved OTP v3 data (screenId partner_otp, schemaVersion 3.0.0, targetApp PARTNER, template.id tpl_P102O).

## 4. Frozen Send OTP and Resend actions

Both initial Send OTP and Resend use:
- POST /api/v1/partner/auth/send_otp
- authentication=NONE
- body JSON with exactly phoneNumber and deviceId, values resolved by existing reference namespaces
- Backend rejects/permits according to existing Identity security policy; do not weaken OTP issuance, cooldown, rate-limiting, device binding or SMS delivery.

Initial Login action body:
~~~json
{
  "phoneNumber": { "$binding": "mobileNumber" },
  "deviceId": { "$context": "deviceId" }
}
~~~

Initial action must keep validate=true, responseMode=destination, mode=push, retain:
~~~json
{
  "challengeId": "data.challengeId",
  "phoneNumber": "data.phoneNumber",
  "resendAfterSeconds": "data.resendAfterSeconds"
}
~~~

Successful Backend SendOtpResult data fields from source: message, challengeId (public opaque ID), phoneNumber, expiresInSeconds, resendAfterSeconds (number), isNewUser and destination. Backend currently sets resendAfterSeconds=60 and expiresInSeconds=300; never emit OTP plaintext/hash.

Resend action must keep:
~~~json
{
  "type": "request",
  "payload": {
    "method": "POST",
    "endpoint": "/api/v1/partner/auth/send_otp",
    "authentication": "NONE",
    "validate": false,
    "responseMode": "none",
    "enabledWhen": { "$state": "countdowns.resend.ready" },
    "body": {
      "phoneNumber": { "$state": "phoneNumber" },
      "deviceId": { "$context": "deviceId" }
    },
    "retain": {
      "challengeId": "data.challengeId",
      "resendAfterSeconds": "data.resendAfterSeconds"
    }
  }
}
~~~

Initial duration and each successful Resend response are server-issued seconds (number) retained in session state. Runtime derives countdowns.resend.remainingText and countdowns.resend.ready in **entry-local** transient state using monotonic time; TextElement displays it. Missing/invalid duration fails closed. Backend alone enforces cooldown; frontend cannot guarantee physical-server time agreement. Testing fixture 10 seconds is development-only; production remains Backend-authoritative 60 seconds currently.

Backend may return HTTP 429 OTP_RESEND_COOLDOWN or OTP_RATE_LIMITED and HTTP 503 OTP_DELIVERY_FAILED. On any failed/rejected/cancelled request, **no replacement challengeId** and **no timer restart**. Existing user-facing generic error "Unable to complete action" is the **currently verified** behavior. A specific cooldown message/retry-after synchronization has NOT been approved or implemented.

## 5. Frozen OTP v3 and Verify request

OTP GET must emit approved OTP v3 structural/layout JSON, including:
~~~json
"countdowns": [
  { "id": "resend", "durationSeconds": { "$state": "resendAfterSeconds" } }
]
~~~

Existing otp_timer_text TextElement renders spans "Resend OTP in " + $state.countdowns.resend.remainingText with icons/clock leading content. Existing otp_resend_control is a TextElement with the guarded onClick RequestAction; no TimerElement, independent OTP screen code, fake per-element coroutine or confirmation dialog.

Verify request:
~~~json
{
  "type": "request",
  "payload": {
    "method": "POST",
    "endpoint": "/api/v1/partner/auth/verify_otp",
    "authentication": "NONE",
    "validate": true,
    "responseMode": "destination",
    "body": {
      "challengeId": { "$state": "challengeId" },
      "phoneNumber": { "$state": "phoneNumber" },
      "otp": { "$binding": "otp" },
      "deviceId": { "$context": "deviceId" }
    }
  }
}
~~~

Backend VerifyOtpSchema also optionally accepts deviceModel, osVersion and fcmToken, but none are mandatory for this first payload; do not invent or add them until required. challengeId is currently validated as UUID by Backend; development preview's fake challengeId is not live-testable.

Backend returns token, refreshToken and destination on successful Verify. **Do not write tokens into generic $state or logs**. Secure session/authenticated request ownership must be separately source-audited and frozen before navigating into protected screens.

## 6. Backend SDUI contract migration required; no raw bypass

Current Backend sdui/engine/src/core/SduiModel.ts:
- screenSchema is strict and has screenId/schemaVersion/targetApp/template/theme/metadata; missing **initialBindings, presentations, countdowns**.
- requestActionSchema payload is strict and lacks **enabledWhen** (and frontend's push mode).
- current present/dismiss/navigate shapes do not match the approved Login v19 country picker events/pop mode.
- section/component/template schemas use restrictive alternative shapes. Approved Frontend FormFieldSection/ListSection/BottomSheet constructs and optional slots must be proved accepted. Do not relax every field to unknown or silence validator failures.
- Backend model still allows $response but Frontend's DynamicReferenceResolver **rejects** $response. No $response references may appear in emitted Login/OTP v3 documents.
- the old PartnerLoginScreen and PartnerOtpScreen composers build different colors/layout/styles. They are not suitable fallback screens.
- Backend SduiBuilder/Action.ts typed authoring interfaces must be reviewed alongside SduiModel and Backend screen regression tests; add only the missing concrete vocabulary actually used by current approved documents.
- Backend screen GET controllers correctly delegate to productionSduiService; keep this ownership rather than creating another hardcoded auth-screen HTTP response path or copying UI into controller.
- Cross-repo exact JSON fixture parity and destination-to-loaded-screen equality are required tests (compare meaningful screen fields without modifying the approved layout).

Backend must author the *current* approved documents through its existing engine/schema ownership. No duplicate renderer, DTO concept, magic conditional engine outside the typed screen contract or per-OTP special-case API.

## 7. Region/country, authentication and next screen: explicit BLOCKING gates, not silently frozen

**Country/phone policy**: Login v19 Country Picker's demo catalog (eight countries) exists only in development preview. Real Frontend PartnerCountryCatalog() returns no countries unless a vetted snapshot is injected. OTP phone display currently includes a literal +91, while send API only takes phoneNumber (no countryCode). We do NOT claim multi-country live support. Freeze only the exact existing SendOtpSchema body; separately decide vetted country source, allowed regions, country dialing/normalization, and state-to-SMS destination correctness *before live end-to-end authorization*. No fake countries, hardcoded unsupported dial-code logic or UI regression.

**Authenticated handoff**: Current Backend VerifyOtp returns destination partner_dashboard/default_template. Current Frontend TemplateMapper maps form_template only, and protected Backend /api/v1/partner/sdui/registry/:screenId requires JWT. Access/refresh token secure persistence and authenticated RequestHandler behavior have not been established by these Login/OTP preview tests. Treat successful Verify HTTP contract as frozen; navigation to an authenticated screen is NOT declared functional until secure session handling + renderable destination are independently reconciled. Do not add a mock Dashboard workaround.

**Error presentation**: Mock 429-like failure is not a live HTTP-status/code handling proof. Preserve backend error envelopes/codes; separately audit Frontend generic error extraction and approved UI presentation for a typed 429 response.

**Deadline/re-entry**: Duration-based monotonic countdown is active-entry semantics per Doc 155. Resume across process death/re-entry, server clock skew, retry-after headers and cross-device timing are not solved by a 10-second local fixture. No speculative deadline endpoint or unstated server timestamp contract may be introduced.

## 8. Strict implementation wave order (not yet authorized)

1. **Backend schema/authoring parity**: narrowly reconcile strict SduiModel and typed SduiBuilder/Action with current Frontend v19/v3 payloads, including screen.countdowns, presentations, FormFieldSection/ListSection and onClick guard/pop. Update Backend tests without weakening unrelated schema invariants.
2. **Backend screen document parity**: PartnerLoginScreen and PartnerOtpScreen emit the approved screen documents (or equivalent typed engine output) with exactly matching IDs and visual properties. GET controller and registry remain single owners. Bootstrap default/persisted guest destination reconciled to Login template ID.
3. **Auth action wire corrections**: emit SendOtp/VerifyOtp Backend-canonical URLs/body/retain shown above in Backend-authored JSON; prepare minimal corresponding changes to Frontend's canonical JSON fixtures if they will remain the editable contract. Do not change styling or existing generic renderer.
4. **Real transport verification**: GET bootstrap/login, POST Send, GET OTP, Resend success/rejection, Verify invalid/valid. Assert challenge replacement, timer behavior, duplicate taps, no OTP leakage, error codes and four-field destination parity; backend's 60-second policy remains untouched.
5. **Close separate blocking gates**: countries/phone-number policy, secure session storage, authenticated dashboard/next destination, typed 429 error UX, and timing restoration as specifically authorized. Keep Phase 5 outside the first OTP API parity implementation if not yet decided.

## 9. Verification acceptance and source safety

- Current source-only/preview confirmation: Android A35 timer disabled/enabled; Resend mock success restarts countdown; mock failure shows "Unable to complete action" without restart. User-confirmed on 2026-10-25. This is *not* live Backend authorization or network proof.
- Current Backend SendOtpSecurity policy: OTP length 6, TTL 300 s, cooldown 60 s, max attempts 5, window 15 min, max challenges 5; security must not be changed to accommodate preview timing.
- After future implementation, require unit/schema tests for strict action/screen DTOs and exact fixture parity, Backend route/controller tests and Login/OTP screen identity tests; Frontend :data:jvmTest :app:jvmTest :feature:dynamic:jvmTest :androidApp:assembleDevelopmentDebug, Samsung preview regression, then actual Backend environment test. Web preview separately.
- Avoid broad git operations or modifying unrelated branches. Preserve untracked local diagnostic files (notably CB-Partner-Live-Trace-V1.13.ps1). Preserve Clean Architecture dependency direction, SRP, diagnostic privacy and established Store/navigation ownership.
- If source changes after pinned SHAs, reconcile new source first. This freeze is **for target contracts and gates**, NOT a claim that Backend already emits them. Do not begin source implementation on the basis of this document without the next implementation authorization.

## 10. Freeze decision / next checkpoint

**FROZEN NOW:** canonical four-field destination, backend routes, request payload keys and references, success retention, backend-issued numeric cooldown, approved screen identities and exact visual source-of-truth, generic Resend runtime behavior, invariants and migration sequencing.

**EXPLICITLY OPEN:** Backend schema/authoring implementation, bootstrap persisted-config parity, live country support, authenticated session/next screen, typed 429 UX, full deadline restoration, cross-repo end-to-end integration tests.

**No production/frontend UI source changed during this reconciliation.**

> **2026-10-26 owner-approved identity correction:** Canonical Login v19 template ID is `tpl_P101L`; previously shipped frontend v19 used `tpl_P102L` and older design notes used `tpl_7K2M9Q`. Bootstrap and GET Login must agree on this exact ID at future backend migration; current Backend layout still needs full visual/behavior parity, so ID agreement alone is not integration completion.
