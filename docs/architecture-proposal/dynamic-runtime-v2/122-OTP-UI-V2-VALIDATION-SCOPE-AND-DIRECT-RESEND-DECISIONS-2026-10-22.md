# 122 — OTP UI v2 validation ownership and Resend behavior

**Date:** 2026-10-22  
**Decision state:** Frozen UI corrections; source changes and timer/resend implementation NOT authorized by this document.  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`

This document supersedes contradictory portions of Document 121. The original `partner-otp-ui-contract-v1.json` and Doc121 are preserved. The revised JSON-only target is `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v2.json`; existing Login JSON and backend remain unchanged.

## A. Login input: shared border vs number-only background (owner-confirmed)

- The **entire** `phone_input_row` is outlined red `#DC2626` in validation error state, including country flag, country code, divider and phone input. Its rounded shape remains 12dp.
- Only the editable `mobile_number` area has light-red background `#FFF1F2`. The flag, code and divider remain white/neutral.
- No independent red border around only the editable field. Use current `StackGroup.validationPresentation.errorBorder` for the whole-row outline and `InputElement.validationStyle.background` for inner tint.
- Validation message reserves layout height while hidden to prevent flicker; exact generic implementation is pending.
- Existing standalone Web visual preview currently has a *number-only* error border and therefore needs correction. Do not alter the already-correct Login JSON to compensate.

## B. OTP: one error state, six invalid slots together

- Exactly **one** segmented InputElement with six boxes and a single `otp` binding.
- Invalid OTP: **all six** segment borders red `#DC2626`, **all six** backgrounds `#FFF1F2`, regardless of focused/current digit. Never mark only one box red.
- All empty boxes display the visible dash `—`. Approved editing rules: sequential input; replace selected slot only; delete a filled slot without shifting; backspace on empty slot moves to previous; paste begins at selected slot and overwrites sequentially, ignoring excess.
- **No additional red border on the outside of the group** of six boxes. Therefore v2 omits `otp_fields_group.properties.validationPresentation.errorBorder`; input's `validationStyle.border` already declares the per-segment outline.
- One inline OTP-related validation message below the boxes. Keep its area reserved while hidden (`messageStyle.reserveSpaceWhenHidden`, `minHeight` UI target). Code-invalid/expired messages must not be confused with API transport failures.
- This omission needs a **small generic typed source change**: `DynamicValidationPresentation.errorBorder` is currently mandatory and `GroupMapper` currently requires it; make errorBorder optional without changing Login. Existing segmented renderer also ignores `validationStyle.border`, dash placeholder and positional edits.

## C. OTP errors, Resend button and timer

- No network/server error card, nor generic “Unable to verify OTP” error box, **inside the bottom OTP form**. Only OTP input validation errors appear under input.
- How network/server failures are surfaced outside the form remains pending; they must not silently be treated as success.
- **No “Request New OTP?” confirmation dialog.** On future enablement, Resend OTP directly invokes one existing `request` action. No new action vocabulary or `sequence`.
- Timer/cooldown eligibility, API payload, backend-authoritative deadline, new challenge retention, success feedback and timer restart are **separate pending freeze gates**.
- Thus this v2 JSON intentionally keeps **static** `otp_timer_text = "Resend OTP in 00:30"` and non-clickable `otp_resend_control` with no action. Verify's illustrative Request Action and loading remain as v1, not backend-verified; do not send it as a production request without schema reconciliation.
- No Resend confirmation `presentation` is present or should be added.

## Exact revision and test status

Only **one OTP JSON path** differs from v1: `data.template.components[1].sections[0].groups[0].properties.validationPresentation.errorBorder` is removed. Existing invalid segment style, Theme gradient, Template hero URL, OTP typing target, OR divider, Change phone navigation, static timer and Verify loading are all unchanged.

This is a **UI-only frozen contract**, not a claim that the current data mapper or renderer accepts/renders v2. Compilation and runtime visual verification remain pending.

## Planned implementation order (requires subsequent implementation authorization)

1. Fix **isolated dummy preview Login**: whole-row red border; number-only light-red background.
2. Typed generic error style: nullable Group validation errorBorder; Input validationStyle border for all segmented boxes; reserved message space for Login/OTP.
3. Segmented dash display and focus/cursor/replacement/backspace/paste behavior, preserving the one-input architecture with no duplicate Store.
4. Generic Verify enabled/disabled visuals, existing loading, responsive form layout and consistent two-sided OR dividers.
5. Web visual preview, JVM/Android/Wasm tests, A35 screenshots.
6. **Deferred**: live timer/resend flow, backend changes, confirmation (explicitly rejected), and network-error presentation outside OTP form.

Do not change the current `theme.properties.gradient` or Template-owned `backgroundImage`; no Theme hero property is permitted.
