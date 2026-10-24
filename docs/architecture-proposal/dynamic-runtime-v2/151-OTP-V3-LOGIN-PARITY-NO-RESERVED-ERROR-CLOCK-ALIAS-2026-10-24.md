# 151 — OTP v3 design corrections: Login parity, non-reserved error, staged clock asset

**Date:** 2026-10-24
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Status:** Owner design corrections accepted; clock resource alias staged in source. **No OTP v3 JSON or OTP UI renderer freeze is implied.**

## Owner's latest explicit corrections (override historical OTP v1/v2 visual decisions)

1. **Never reserve invisible OTP validation-message space.** Use the **same conditional inline-error behavior as the current working Login FormFieldSection**: render message/icon only while that Input has an error. When no error, no error row occupies height. This supersedes Docs 121/122's `messageStyle.reserveSpaceWhenHidden`, `minHeight`, and any OTP-specific forced spacer proposal. Layout shifting on validation is acceptable because the owner has selected Login parity.
2. **Keep Login v19's typography, colors, and visual styling as the OTP design system.** Do not change Login itself or invent OTP-specific visual defaults. Apply authored JSON values:
   - Brand logo `images/cb_partner_logo_03`; brand text 28sp / 700 / `#0A337F` with 44×44 logo and 11dp end spacing.
   - Template hero `images/partner_login_hero_07`, `crop`, `center`; unchanged Login Theme/gradient.
   - Main title follows Login `Welcome back` style: 44sp / 800 / `#0A337F`, lineHeight 52. OTP wording is `Verify your number`; wrapping and hero overlap must be checked on A35 rather than hardcoded per-screen.
   - Instruction/subtitle follows Login: 19sp / 500 / `#5C79A3`, lineHeight 28 (where applicable); text/body/link styles must be explicitly authored from the relevant Login treatment.
   - White `otp_form` surface: `#FFFFFF`, 24dp top corners only; adapt margins/padding from working Login's 16dp side/top, 24dp bottom, rather than old OTP v2's incompatible 24dp internal side padding.
   - Input validation: **same Login red** `#EF4444`; 14sp / 400 / lineHeight 20 with top spacing 6dp; 16×16 `icons/error_alert_icon_01` leading image, 6dp end spacing. Use Login's `#FEE2E2` invalid background in OTP segments where approved; every slot border red together, no outer group border.
   - Primary Verify button follows Login: 56dp height, 16dp radius, `#176AD6` background, 18sp / 700 white label and existing loading behavior.
3. **OTP remains one segmented Input, one `otp` binding**; reuse typed validation `required` and `pattern = ^[0-9]{6}$`, Store and action gating. No six separate Inputs, hardcoded phone/OTP renderer or extra Store. All existing 6-slot input editing requirements remain, including focus, replace, delete/backspace, paste.
4. **Clock asset setup now:** The OTP JSON's stable resource key remains `icons/clock`. The explicit packaged-asset lookup temporarily maps it to the already-bundled `error_alert_icon_01` (owner-approved placeholder), with no additional PNG and no unsatisfied generated resource import. On approval/upload of a real clock PNG, **change only that one asset registry mapping** to its generated drawable; JSON, markup and behavior remain unchanged. Compile-time Compose asset registration does **not** auto-switch when a PNG is uploaded; a one-line Kotlin mapping edit and rebuild are still needed.
5. The timer text stays **static preview copy**; this alias adds no timer, cooldown, resend request, or lifecycle logic. OTP Verify endpoint/response and the live clock/Resend behavior stay pending separate freeze.

## Current source gap / no false "ready" claim

Old OTP v2 JSON is an **unimplemented historical UI-only draft**. Its `StackGroup.properties.validationPresentation` is rejected by current `GroupMapper`. Current `FormFieldSection` requires a label and control surface; segmented `InputElement` ignores OTP-specific error border/placeholder and does not implement individual positional edit semantics. Therefore **do not simply copy v2 and claim working OTP**. The forthcoming v3 contract must choose the smallest typed, generic error presentation that fits OTP without altering working Login, and be rendered/verified via the normal mapper, Store and UI pipeline. `TextElement` with leading/trailing `DividerContent` supports OR. No OTP-specific special divider or extra form store.

**Scope of this commit:** clock alias + asset registry coverage + this decision record. Login v19 JSON, login preview, backend, OTP v1/v2 history, UI renderer and runtime validation are unchanged.
