# 148 — Generic Dynamic Validation: Typed Equality and Login v19

**Date:** 2026-10-24
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `b6aaca710d0bdd2f692525db1d0f39e301dcaf4d`
**Status:** Source and tests authored; local Gradle/device verification still required.

## Minimum additive contract

Existing `DynamicFormValidator` remains the single generic validation evaluator. `required`, `pattern`, `lengthMessages` (error-copy selection only), `clearOnEdit`, and named validator semantics remain. No new engine, Store, manager, backend calls or renderer.

Optional `validation.equals` supports two typed Dynamic operands, resolved through existing `DynamicReferenceResolver` with `$binding`, `$context`, `$state` and `$literal`. Unknown, ambiguous and malformed reference objects fail at DTO mapping. If either operand resolves to null/missing, equality validation fails closed, even if both are null. Validation order: required → pattern → equals → existing named validator, only for nonblank optional inputs, preserving prior behavior.

## Login v19 local preview example

```json
{
  "required": true,
  "pattern": "^[6-9][0-9]{9}$",
  "equals": {
    "value": {"$binding": "countryId"},
    "expected": {"$literal": "IN"}
  },
  "message": "Please enter a valid 10-digit mobile number."
}
```

The v19 JSON replaces **only** the legacy `phone_number` validator with that authored pattern/equality rule; required/error-copy/clearOnEdit remain, along with all UI, assets, country picker, navigation, payload and form layout. The historical v18 file is preserved; development preview uses byte-identical v19 JSON.

Tests: data mapper equality mapping and forbidden references; feature-runtime India-only accepted/rejected cases, non-IN and missing country fail-closed, error messages, on-edit policy and unrelated binding comparison. Existing named-validator tests remain.

## Frozen compatibility boundary

**No backend change authorized.** If production backend continues to send `validator.key = "phone_number"`, its current `PartnerPhoneNumberValidator` and `App.kt` Koin registration remain necessary; they are deliberately NOT moved into Dynamic or Domain. Removing them now would break production wire compatibility. After backend explicitly adopts and verifies declarative validation, retire the legacy provider and associated DI/test wiring in a separately verified cleanup. Do not treat the local preview change as proof the backend has migrated.

Device/test gate: run `:data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :androidApp:installDevelopmentDebug`, then check Samsung A35 valid/invalid IN, US rejection, no missing-region fallback, red error icon, clearOnEdit, keyboard and Country Picker. At the time of this record, GitHub source is not equivalent to a local Gradle/device pass.
