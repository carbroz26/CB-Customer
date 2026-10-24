# 155 — Generic Countdown + Resend OTP: minimal contract freeze

**Date:** 2026-10-24
**Status:** Contract frozen for a focused frontend implementation; implementation and device verification are separate gates.
**Scope:** OTP v3 timer and resend eligibility only. Preserve Login v19 and OTP visual layout, MVI/UDF, single Store and all existing action vocabulary.

## Source-verified baseline
- Backend `CB-Backend/development` returns `SendOtpResult` fields `challengeId`, `phoneNumber`, `expiresInSeconds`, `resendAfterSeconds` and `destination` on successful send. `AUTH_SECURITY_POLICY.otp.resendCooldownMs` is currently 60000 (60 seconds). Failed cooldown uses HTTP 429 and code `OTP_RESEND_COOLDOWN`. **The frontend never overrides backend enforcement.**
- Login v19 RequestAction retains only `challengeId` and `phoneNumber` today; add the existing response path `data.resendAfterSeconds` to the same retain map, without styling changes.
- `DynamicReferenceResolver` supports `$state`, not `$response`; `RequestActionHandler` already retains successful response data with `responseMode: none`.
- Existing TextElement renders spans through `$state`; existing `TextElement` and IconContent suffice; no TimerElement, OTP-specific Store, new action type, or hardcoded render label.

## Wire contract (new screen-level optional declaration)
```json
"countdowns": [
  {"id": "resend", "durationSeconds": {"$state": "resendAfterSeconds"}}
]
```
- Location: `data.countdowns`, sibling to `data.template`, not Theme or Template properties.
- Each `id` must be a unique, safe identifier. `durationSeconds` is an exact one-key `$state` reference to a **nonnegative numeric duration** in seconds. Unsupported values and absent references fail closed (never enable resend). Bound the maximum to 86400 seconds; no implicit client default. Screens without `countdowns` are unchanged.
- Source of truth is backend response value retained in session state. Initial development OTP preview seeds `resendAfterSeconds = 10` through the local preview fixture; 10 is **not** embedded in the runtime or production OTP screen JSON.
- A single screen-entry-owned generic countdown owner measures elapsed monotonic time from successful screen load; ticks must recompute from elapsed time, not mutate a decrementing counter. Entry disposal cancels tick work. Conservative restart on re-entry is acceptable under this first relative-duration contract because server remains authoritative; exact server-relative deadline/synchronization requires a future backend timestamp contract.
- Successful RequestAction which retains a new referenced `resendAfterSeconds` restarts countdown. Failed, rejected, or cancelled actions do not restart it. Retained `challengeId` is replaced only on successful request.

## Computed read-only values exposed using existing `$state`
- `countdowns.resend.remainingText`: `mm:ss` with whole seconds rounded up (e.g. `00:10`, `00:09`, `00:00`).
- `countdowns.resend.ready`: boolean; true only at zero after a valid duration was received. Missing/invalid duration leaves `ready=false` and display `--:--`.
- These values are **screen-entry transient state**, not session-retained state and not new binding namespaces. Renderer overlays read-only computed values on `$state` solely for the active entry; screens without timers are unaffected.

## Existing TextElement — no new element category
```json
"spans": [{"text": "Resend OTP in "},
          {"text": {"$state": "countdowns.resend.remainingText"}}]
```
- Keep current `icons/clock` leading content, sizes, color, typography, alignment and layout unchanged.

## Resend eligibility + action
- `otp_resend_control` remains existing `TextElement`. Add author-controlled `disabledColor` to TextElement properties and a RequestAction `payload.enabledWhen` of `{"$state":"countdowns.resend.ready"}`.
- `payload.enabledWhen` is a generic conditional execution guard, evaluated immediately at dispatch by the existing Store; false/absent resolved value suppresses dispatch. Renderer also suppresses gesture and applies `disabledColor` while false. No network side-effect before eligibility and no reliance on visual disabling alone.
- At expiry, onClick uses existing `request` (POST send_otp, NONE, validate=false, responseMode=none) with `$state.phoneNumber`, `$context.deviceId`, retain `{challengeId: data.challengeId, resendAfterSeconds: data.resendAfterSeconds}`. No confirmation dialog. Request success restarts countdown; request failure does not.
- Backend 429 remains valid even if device clock is wrong or preview sends too early: the timer is UX only, not security.
- Development preview repository may return an offline mock successful Resend result to verify restart and challenge retention, **only** for the preview session. It must never contact or relax the backend rate limit.

## Mapping and ownership (fixed)
1. `DynamicScreenDto` optional typed countdown declaration; `DynamicResponseMapper` validates and maps to domain `DynamicScreen.countdowns`.
2. `DynamicStore` alone orchestrates countdown lifecycle/restarts and enforces generic action eligibility; a small pure runtime helper calculates remaining time.
3. `DynamicScreenState` carries transient computed countdown values; existing `DynamicRenderContext` + `$state` resolver and `TextElement` spans render them.
4. TextElement maps/uses optional author-supplied `disabledColor`; it adds no timer calculations.
5. Preview seeds test duration 10; backend duration stays 60. No new networking, screen-specific handler, ViewModel, or second Store.

## Verification gates
- Decoder rejects duplicate/blank ids, invalid `$state` expressions, invalid duration (negative, non-number, oversized); static screens unchanged.
- Runtime 10->9->...->0; suspend/resume elapsed-time behavior, cancellation on disposal, restored/failed requests, duplicate taps and guard; success replaces challenge and restarts; failure does not.
- Login v19 regression, OTP v3 unchanged visual styling, Android A35 + Web development previews.
- Server response/status contract must be reverified before live backend rollout; preview mock is not production proof.
