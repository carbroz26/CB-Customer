# 149 — Retire the Unused Partner Phone Validator; Correct v19 Regression

**Date:** 2026-10-24
**Frontend:** `carbroz26/CB-Partner`, branch `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `31a84556340098286ce93a4bcdb7cd048e7e7c47`
**Status:** Source committed; owner Windows Gradle and Samsung device verification pending.

## New owner clarification supersedes outdated compatibility assumption

The Login SDUI contract is being authored **from scratch on the frontend first**. The backend does **not** currently emit `validation.validator.key="phone_number"`; only after the new contract is finalized will the backend be implemented to return the exact approved JSON. Consequently Document 148's claim that the current backend requires preserving `PartnerPhoneNumberValidator` is obsolete. Document 143's *India-only behavior* still applies; its temporary phone-specific class retention is superseded.

## Minimal removals

- Delete `app/validation/PartnerPhoneNumberValidator.kt` and its app Koin factory/imports. Do not move the same India-specific regex to another module.
- Delete `ConsolidatedLoginIntegrationTest.kt`, whose three test cases exclusively exercised that discarded implementation. The field-agnostic `DynamicFormValidatorTest` now covers the same India-only acceptance, unsupported/missing country, validation messages, and clear-on-edit directly through `pattern` + `equals` and the existing generic runtime.
- Keep `DynamicFormValidator`, typed DTO/domain/mapper, `DynamicValidator` extension interface and registry unchanged for this bounded removal. The named extension has no product-specific registration, and no extra validator manager/engine is introduced. Deciding to retire the whole optional named extension is a separate source-contract concern, not a prerequisite for removing the unused phone class.
- Login v19 JSON and Country Picker, actions, Store, renderers, assets, keyboard and backend remain byte-for-byte unchanged.

## Failed test root cause

The owner ran Gradle and `:feature:dynamic:jvmTest` failed in `declarativeEqualityAndPatternPreserveIndiaOnlyRulesWithoutNamedValidators` at line 230. Input `"98765 43210"` is **11 characters**, not 10. The generic validator correctly chooses the pre-authored `lengthMessages.minLength=11` text `"Mobile number must be 10 digits."`, not the fallback `message`. Correct the test expectation. Add separate `"9876 43210"` (10 characters including a space) to assert invalid-format generic pattern messaging. This is a **test correction**, not a production validation behavior change.

## Test/acceptance

```powershell
git pull --ff-only origin architecture/dynamic-runtime-v2-discussion-freeze
.\gradlew.bat :domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :androidApp:installDevelopmentDebug '-Pcbpartner.api.baseUrl=http://127.0.0.1:3000'
adb shell am start -S -W -n "com.carbroz.cbpartner.android.dev/com.carbroz.cbpartner.android.MainActivity" --ez cbpartner.login_preview true
```

Gradle must finish with `BUILD SUCCESSFUL` before ADB launch can be treated as evidence for the new APK. Confirm invalid Indian prefixes/length/spacing, valid Indian numbers, country selection fail-closed, error message icon, clearOnEdit and IME. No GitHub-only edit may claim local Gradle/device success.

**No unrelated refactor, extra app registration, backend changes or screen-specific Kotlin validation.**
