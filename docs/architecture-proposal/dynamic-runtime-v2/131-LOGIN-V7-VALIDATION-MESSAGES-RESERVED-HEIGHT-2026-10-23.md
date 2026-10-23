# 131 — Login v7 validation messages and reserved height
**Date:** 2026-10-23
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Scope:** Generic validation contract and inline error layout only.

## Behavior
The approved VisualLoginPreview supplies distinct error messages. The owner's explicit requirement is live revalidation: 12-digit invalid error remains at 11 digits and clears once 10 digits become genuinely valid. The standalone Visual Preview currently clears its attempted flag immediately on edit; that preview remains protected and unchanged. Existing DynamicStore handles live revalidation after submit without a new Store.

## Typed changes
The input validation DTO/Domain/Mapper now supports optional `requiredMessage` and ordered `lengthMessages` with inclusive minLength/maxLength, used only to select error copy after validation fails. The existing `message` remains the fallback and the `phone_number` named validator still decides validity.
The Group's `validationPresentation.messageStyle.reserveHeight` explicitly reserves 24dp whether error is present or not, so the form does not jump. Shared binding errors continue to control group border and editable-only tint; country code and divider are unaffected.

## Files
New versioned `partner-login-country-picker-parity-v7.json`, byte-identical active Web fixture; v6 preserved. Targeted DTO mapping and validator tests added.
No assets, Theme, Store, Input surface, Button, Navigation, action handlers, backend or VisualLoginPreview changes.
The red error icon and invalid-state gray Continue style remain separate visual parity gaps.

## Verification
JVM tests, Android compilation, Webpack and Web screenshots are required; source checks alone do not establish runtime parity.
