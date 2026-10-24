# 152 — OTP v3 JSON design target, reconciled against current Login v19

**Date:** 2026-10-24
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Source baseline:** `718aea4d90a6c32b385d2e40d3b982d25791515b`
**Contract:** `docs/architecture-proposal/dynamic-runtime-v2/partner-otp-ui-contract-v3.json`
**Status:** **DRAFT UI/JSON TARGET FOR REVIEW — NOT YET RUNTIME PARITY OR BACKEND FREEZE.**

## Authoritative choices

1. Reuse the actual current Login **v19** `form_template` layout, `images/partner_login_hero_07` with crop/center, `images/cb_partner_logo_03`, unchanged light Theme/gradient, same white bottom card (top corners 24dp, padding 16dp start/top/end and 24dp bottom), same primary button styling (56dp height, 16dp radius, #176AD6, white 18sp/700, 20dp spinner), and same branding/title/subtitle typography/colors. The OTP text content changes, **not** these style tokens. OTP keeps separate unique `tpl_P102O` templateId and two `stack_component` instances.
2. Screen structure: `otp_branding` (logo, title `Verify your number`, subtitle, dynamic phone display, Change phone pop action) + `otp_form` (OTP field, static timer, OR, static Resend, Verify request, help). No legal links or Country Picker copy.
3. Exactly **one** `InputElement` with `binding.key=otp`, `presentation.type=segmented`, `count=6`, digits keyboard/filter, `maxLength=6`, authored six equal 48×56dp slots with 6dp gaps and round16 corners. Width is 6×48 + 5×6 = 318dp (fits a 360dp content viewport after 16dp side padding); smaller viewports and accessibility scaling need actual layout testing. Slots show authored dash `—` when empty. Visual cell typography 24sp/700, brand blue, as an OTP-specific visual treatment; other Login typography remains unchanged. Full positional editing/paste/backspace rules retained from prior OTP decisions.
4. OTP field uses the **same conditional error behavior as current Login**: the inline message/icon renders **only when invalid**; **no reserved empty space, no minHeight placeholder**. `messageStyle` is an exact deep copy from Login v19 (14sp/400/#EF4444, 20sp lineHeight, top spacing 6dp, 16×16 `icons/error_alert_icon_01`, icon end spacing 6dp). `clearOnEdit=true`. All six segments become error tinted (#FEE2E2) and red bordered (#EF4444) together; **no extra outer group/field error border**.
5. `otp_timer_text` remains static `Resend OTP in 00:30`, with `icons/clock` leading asset resource. Doc151 already registered the key, temporarily mapping to `error_alert_icon_01`; real clock PNG replacement changes one registry mapping only, not JSON.
6. `OR` uses a TextElement with existing two `DividerContent` accessories; 112dp each, thickness1, spacing 12, not an invented OR component.
7. `Resend OTP` is static styled TextElement with no action and no false cooldown logic. No confirmation or request yet. `Verify & Continue` reuses Login button styling, generic loading and `validate:true` request vocabulary. The existing v2 Verify endpoint/body were **preserved for design continuity only, not verified against future backend schema**. No `enabledWhen` or disabledStyle keys that current renderer ignores.
8. Display phone is an authored India-only `+91 ` literal + dynamic `$state.phoneNumber` span. This is explicitly a *current IN visual target*, not a generic international formatter. Existing Login v19 retains `phoneNumber` and `challengeId`, not `countryCode`. Generalized dial prefix/spacing and safe state transfer must be revisited during the later backend/OTP journey contract; **Login v19 itself is untouched**.

## Typed mapper / renderer gaps which make v3 a TARGET, not currently runnable

- `FormFieldSection` currently requires the first element to be a **label** and exactly one direct Input. OTP design has no visible label, so v3 proposes a generic *label-optional field*, without inventing a blank label placeholder or screen-specific hack. `FormFieldSection` domain and mapper currently reject this v3 shape.
- `DynamicValidationPresentation.errorBorder` is currently mandatory in `SectionMapper` and domain. OTP requires **no outer error border**, so v3 omits it. A **generic optional errorBorder** is required. Login's authored errorBorder must continue rendering unchanged. Existing `FormFieldSection.RenderMessage` already renders only when an error exists, exactly as the owner requires: do not add reservation.
- `DynamicInputPresentation` / `DynamicValueMapper.inputPresentation` currently do not retain segmented `placeholder`. `SegmentedInput` currently shows empty strings and has a single sequential BasicTextField; it does not implement per-cell selection/replacement/delete/backspace/paste. Requires one bounded, generic segmented-input upgrade, no 6 InputElements or second Store.
- `DynamicValidationStyle` mapping/rendering currently retains only background, not a per-cell error border. Authoring `validationStyle.border` in v3 is deliberate **target-schema intent**, not proof the border will render.
- Current Login-style 44sp OTP heading is long; Samsung A35 wrapping, hero visible crop, keyboard/insets and 318dp OTP row must be reviewed on-device.
- Timer ticking, resend eligibility/action, challenge refresh, server error/expired code, Verify backend response, request body and destination/session retention remain **unfrozen**. No backend edits.

## Verification and implementation discipline

1. Review this JSON and attached design with owner, reconcile source-supported versus target-schema gaps; do not mislabel v3 as a frozen runnable contract.
2. Implement only missing **generic typed** mapper/domain/renderer functionality inside existing categories after review; preserve all Login v19 behavior without hacks, duplicated Store, new validators or overengineering.
3. Route a local OTP preview through the same real `DynamicResponseMapper → DynamicStore → registered renderer` path used by Login; verify independent of the old hardcoded VisualLoginPreview.
4. Run targeted JVM mapper/runtime tests and Android A35/keyboard/screenshots. Check no error space in normal state; all six error borders + background and message on submit; clearOnEdit; sequential typing and positional editing; OR divider; static timer/resend; hero/brand parity; changing number back navigation.
5. Only after UI and behavior parity are demonstrated and reviewed, freeze this JSON for subsequent backend implementation. The Login v19 JSON and active Login preview are not modified by this step.

**No runtime source changes in this v3 JSON design commit.**
