# 150 — Retire unused named-validator extension (source and contract reconciliation)

**Date:** 2026-10-24  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Baseline HEAD:** `70aa50abb99339784d58d22d2970c415156e9c6f`  
**Status:** Bounded code/fixture cleanup authored; Gradle/device regression pending.

## Current owner decision and architecture rationale

The frontend is finalizing new Dynamic JSON before the backend is implemented to return it. `PartnerPhoneNumberValidator` and its app registration were removed in Doc149. No production or preview named Dynamic validator remains. `DynamicValidator` and `DynamicValidatorRegistry` therefore represent an unused optional extension mechanism, not a real six-category UI registry or an essential Clean Architecture boundary. Remove them and associated wiring without creating a replacement wrapper.

This **supersedes Doc149's instruction to retain the unused named extension**. Docs 143 and 148 remain historical records, not current evidence that the backend requires `phone_number`. No existing backend changes are made.

## Exact cleanup

- Delete `DynamicValidator.kt` and `DynamicValidatorRegistry.kt`.
- `DynamicModule.kt`: register the existing `DynamicFormValidator()` directly. No new Koin module/service.
- `DynamicFormValidator.kt`: remove unused registry constructor dependency and named-rule dispatch. Keep `required → pattern → equals`, error message selection, `clearOnEdit`, scope traversal and request gating unchanged.
- Domain `DynamicValidation`: remove `validator` and `DynamicValidatorSpec`, keeping typed declarative rules.
- DTO `DynamicValidationDto`: retire `DynamicValidatorDto` but deliberately retain a `validator: JsonElement?` **rejection sentinel**. With the project's `Json.ignoreUnknownKeys = true`, deleting the field outright would **silently accept** unsupported legacy named-validator payloads. `ElementMapper.mapValidation` explicitly rejects any non-null legacy `validator` value. This is a safety validation at the existing mapping boundary, not a plugin architecture or runtime handler.
- Convert historical Phase-1 data test fixture and its mapper assertions to generic `pattern` + `equals`; add mapping tests rejecting legacy object and malformed named validator values.
- Simplify feature unit tests to assert declarative rule and message behavior; remove tests that only exercised the discarded named API.

## Source ownership preserved

Backend-authored validation → existing typed DTO → existing mapper → `DynamicValidation` domain model → single `DynamicFormValidator` → existing `DynamicStore` gating/errors → existing generic FormFieldSection renderer. No duplicate module, resolver, Store, ViewModel, business-specific Kotlin validator, navigation/network edit.

Login **v19** versioned JSON and active Web/Android preview JSON are unchanged. Current India-only behavior `^[6-9][0-9]{9}$` and `equals(countryId, IN)` remains **authored in JSON only**, not reintroduced in Kotlin.

## Verification

Run targeted `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :androidApp:installDevelopmentDebug` locally. Device verification: required, digit count, invalid prefix, valid 10-digit IN, non-IN region, clearOnEdit and picker. No GitHub-only edits imply a passed Gradle build or E2E backend acceptance.

Future unsupported validation types must be contract-designed and mapped explicitly; do not resurrect speculative named validators, registry or arbitrary runtime Kotlin execution.
