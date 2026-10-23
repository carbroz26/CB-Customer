# 143 — Interim India-Only Phone Validation Decision

**Date:** 2026-10-23
**Frontend:** `carbroz26/CB-Partner`, branch `architecture/dynamic-runtime-v2-discussion-freeze`
**Decision:** OWNER-APPROVED / FROZEN FOR CURRENT LOGIN SCOPE
**Scope:** Preserve the existing India-only Login client-validation policy until a separately approved country-wise backend contract is available.
**Change type:** Documentation only — no Kotlin, JSON, backend, or asset changes authorized by this decision.

## Decision and current source behavior

1. **No backend changes now.** In particular, do not modify `carbroz26/carwash-backend` on `feature/backend-migration` or `carbroz26/CB-Backend` on `development` under this task. Backend country-wise phone support and Send OTP request/response compatibility remain independently open.
2. **Keep existing India-only phone validation unchanged.** `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/validation/PartnerPhoneNumberValidator.kt` is already a named `DynamicValidator` for `phone_number`; it accepts the selected region `IN` only and exactly ten ASCII digits starting in `6..9` (`^[6-9][0-9]{9}$`). Missing, unknown or non-`IN` regions fail validation rather than using a permissive fallback. This is an interim product-specific implementation, not a generic Dynamic rule.
3. **Keep current DI and generic validation system.** `App.kt` registers this product validator; `DynamicModule.kt` composes `DynamicValidatorRegistry(getAll<DynamicValidator>())` and `DynamicFormValidator`. Preserve `required` → `pattern` → named-validator ordering, resolved `$binding.countryId` arguments, unknown/duplicate validator failure, `clearOnEdit`, and request `validate=true` gating. No new validation service, duplicate Store or renderer.
4. **Keep Login v11 JSON and the current Country Picker UI untouched.** The named validator key is `phone_number` with `arguments.region={$binding: countryId}`. The eight preview country rows are **UI-only examples**; they are **not** permission or evidence for international OTP support. Non-IN selections remain unable to pass current named validation. The current displayed phone error copy may be India-specific when another sample country is selected; product-wide country-wise UX remains deferred.
5. **Backend remains authoritative on actual OTP acceptance.** Frontend Login v11 request body (`phoneNumber`, `countryCode`, `deviceId`) is not verified compatible with the inspected `carwash-backend` requestOtp schema (only `phoneNumber`, `additionalProperties:false`). Do not claim Login→OTP end-to-end functionality or silently remove fields/change routes.

## Decisions explicitly deferred

- Replace/remove `PartnerPhoneNumberValidator` only as part of a future, separately approved, versioned regional-policy migration with actual backend-supported country/rule data and matching tests.
- Do not add international hardcoded regex rules, move India-only regex into `DynamicFormValidator`, or pretend `lengthMessages` validates input length independently. New declarative rule fields or a conditional-rule evaluator require source-based necessity and an approved wire contract.
- Do not alter Country Picker visual parity, flag assets, frontend JSON v11 or OTP UI contracts under this India-only decision.

## Existing verification evidence and remaining gates

- Existing `app/src/commonTest/.../ConsolidatedLoginIntegrationTest.kt` covers valid/invalid Indian numbers and fail-closed unknown regions. `feature/dynamic/src/commonTest/.../DynamicFormValidatorTest.kt` covers generic named/required/pattern/binding resolution and registry failures.
- Previous successful development build is evidence for the pre-existing implementation; **this document is not a new code build or backend E2E test**.
- Safe-Area/IME Android progress and remaining cross-platform gates are recorded in Doc142; continue those acceptance gates independently.
- **Next project work:** Login + Country Picker dynamic visual parity and remaining device regressions without waiting for backend changes. Later country-wise validation must undergo a separate owner decision and integration proof.

**Supersession note:** This owner decision supersedes only earlier *proposals* in Documents 118/140 to migrate India-only validation immediately. It does not supersede existing generic-validator architecture or authorize international acceptance.
