# 123 — Login / OTP UI P0–P5 implementation checkpoint

**Date:** 2026-10-22  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Status:** Source implementation committed; Kotlin/Android/Web compilation and UI acceptance require separate execution. Backend unchanged.

## Frozen inputs and versioning
- Login historical integration: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-integration-v2.json`, preserved.
- OTP historical UI v2: `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json`, preserved.
- **Current implementation inputs:** `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-implementation-v3.json` and `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-implementation-v3.json`; the development Web resources are byte-for-byte copies.
- Both current targets retain the exact 135° Theme gradient and the Template-owned URL hero asset. No hero in Theme.

## Minimal JSON integration changes
- Login target adds `phone_input_row.validationPresentation.messageStyle.reserveSpaceWhenHidden=true`, `minHeight=24`. Existing whole-row red errorBorder and phone-number-only light-red background remain as frozen. Flag/code/divider background remains white.
- OTP target adds `otp_code_input.properties.placeholderColor="#B7C9E6"`. Existing all-six-slot red validationStyle remains; outer OTP Group has NO errorBorder. No network/server alert card, dialog or resend action.
- OTP timer remains a static TextElement; Resend remains non-clickable. Verify's illustrative request endpoint is offline-only and **not** approved for live API integration.

## Typed generic source implementation
- Domain: optional ValidationPresentation.errorBorder, reserved validation message metadata, Input validationStyle border and segmented presentation placeholder, Button enabledWhen and disabled color.
- Data: typed mapping with source-level checks; existing Login group errorBorder remains valid, OTP group can omit parent border.
- Dynamic Input renderer: six focusable BasicTextFields inside ONE InputElement, retaining a single String binding. Empty positions are encoded as `_` sentinel in Screen bindings (never a complete numeric OTP), with pure positional reducer. Validate and completion-gate reject values with missing slots; paste and backspace are positional.
- Dynamic Group renderer: reserves vertical validation slot when configured and grows for larger error messages.
- Dynamic Button renderer: disables gestures / applies authored disabled style until six ASCII digits present, and retains authored loading behavior when the request is pending.
- Web: `cbpartnerOtpPreview=1` loads current OTP fixture through same DTO mapper, Store and renderer as Login; static example phone in preview-only RuntimeState. No backend request is made for screen fetch, and verify requests are rejected by offline repository.
- Existing `cbpartnerVisualPreview=1` Login dummy corrected to whole-row red border with numeric-only tint.

## Remaining gates
1. Run `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack` on Windows. Resolve any compiler/test errors before labeling build successful.
2. Run Web OTP at `?cbpartnerOtpPreview=1`, Web Login at `?cbpartnerLoginPreview=1`, and dummy Login at `?cbpartnerVisualPreview=1`. Compare at 411×891 with approved 12/14-state design boards.
3. Complete user-device six-digit focus, backspace, paste, replacement, keyboard visibility and font-scaling tests. The existing request-action fixture is NOT a verified backend API contract.
4. No automatic resend timer, confirmation dialog, backend changes, success toast, server/network error card or screen-specific Store is authorized in this wave.

This checkpoint is not final UI acceptance. Authoritative acceptance requires user screenshots and test evidence after compilation.
