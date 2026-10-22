# 121 — Partner OTP UI-first contract: frozen visual structure and deferred behaviors

**Date:** 2026-10-22  
**Repository / branch:** `carbroz26/CB-Partner` / `architecture/dynamic-runtime-v2-discussion-freeze`  
**Decision:** **OTP UI-ONLY CONTRACT FROZEN; runtime fulfillment unverified.** Timer/Resend action, success/error response state machines, authentication and backend changes remain **NOT FROZEN**. The Login contract remains unchanged.

## Sources and precedence
1. Owner's October 22 OTP images (12- and 14-state boards, standalone OTP screenshot, A35 layout specification). The latest 14-state specification is a **visual reference**, not an automatic approval of all variant-specific behavior.
2. Existing integrated Login JSON `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-integration-v2.json` for Template style, Theme, hero, brand, full-width white bottom surface.
3. Owner-provided historical OTP JSON (`Pasted markdown(10).md`) for screen ID, six-digit binding, and old action examples; old green CarBroz layout is **not** the new UI authority.
4. V2 Docs 12/20/60/61/98/112/113/117/118/120 plus current DTO/Domain/Mapper/Renderer/Store/Actions/Navigation source.
5. Backend `carbroz26/carwash-backend` `feature/backend-migration` examined **read-only**; no backend change authorized.

## Frozen, data-owned UI structure
- Exactly one `form_template` with two typed `stack_component` instances: `otp_branding`, `otp_form`.
- OTP screen `partner_otp`, unique template ID `tpl_P102O`.
- Theme copied **unchanged** from Login (light, transparent statusBar, 135° four-stop gradient). Hero **only** under `template.properties.backgroundImage`, same URL-based source as Login. No hero property added to Theme.
- Full-width, white, top-corners-only rounded bottom Form with 24dp internal padding; 24dp outer gutters belong to the content, not to the card.
- Branding reuses Login text/logo construction. OTP title, two-line instruction, retained phone number and clickable Change phone number displayed above hero; phone text uses `$state.phoneNumber` and must not be hardcoded.
- One segmented Input bound to `otp`: 6 boxes, 56×56dp, 5dp gaps, radius 16, 24sp semibold centered numeric text, neutral border 1.5dp #D1D5DB, focus border 1.5dp #3B82F6. Invalid background #FFF1F2 and **per-box** border #DC2626; error beneath.
- Validation message area intended to be reserved at min 24dp height whether visible or hidden to prevent form jumping; Error icon and variable messages remain a renderer / error-schema gap.
- `otp_timer_text` is **static** in this freeze: `Resend OTP in 00:30`. It is a `TextElement`, **not** a ticking timer. The clock icon is a preview resource reference; availability not verified.
- `OR` is a `TextElement` with separate leading and trailing `DividerContent`; no new special divider element.
- `otp_resend_control` is deliberately **static, non-clickable text** with disabled color. It MUST NOT dispatch an OTP request yet.
- `otp_verify_button` reuses existing `request` action with `validate:true` and generic loading contract; request field/response/backend compatibility still subject to G4, so **not live accepted**.
- Help text matches the visual reference; no Terms/Privacy legal block is copied to OTP.

## Deliberate gaps; do not call source-compatible or implemented
| UI target in this JSON | Source finding / pending generic capability |
| --- | --- |
| `input.presentation.placeholder = "—"` | Existing SegmentedInput ignores per-slot placeholder |
| `input.validationStyle.border` | Existing mapper retains only error background; SegmentedInput uses its normal/focus border, not error border per slot |
| `validationPresentation.messageStyle.reserveSpaceWhenHidden/minHeight` | Existing mapper and StackGroup do not reserve hidden message layout |
| `button.enabledWhen/disabledBackground/disabledTextColor` | Existing Button DTO mapper/renderer lacks binding-controlled enabled/disabled appearance/action block |
| `otp_timer_text` text | Display-only; no countdown/expiry/restart/lifecycle |
| `otp_resend_control` | No API action or confirmation. Remains disabled throughout this UI-only revision |
| Current OTP focus/paste/backspace | SegmentedInput still renders one BasicTextField and visual segments; granular six-box behavior requires separate verification |
| Phone formatting | `$state.phoneNumber` shows actual retained value; `+91 98765 43210` formatting is not available as a generic formatter |
| Clock/logo/hero assets | Existing asset URLs and resource references require actual approved/reachable assets |
| Toast/confirmation/errors/locked state | 12/14-state boards disagree in recovery details; state machine intentionally deferred |

**Important:** These proposed UI extension keys are intentionally written into the **target JSON**, not claimed as already mapped. Current mapper may ignore unknown visual fields. Before claiming implementation/final UI acceptance, a narrowly scoped, typed generic mapping/rendering plan and regression tests are required; avoid silent no-op handling.

## Reused frozen actions / non-frozen endpoints
- `otp_change_number.onClick = navigate(mode:pop)` uses the existing single-action navigation owner, preserving previous Login entry where still on stack. Test root/pop edge cases separately.
- `otp_verify_button.onClick = request` expresses existing generic validation/response destination/loading behavior. **Endpoint and body are reference-only pending G4:** backend `verifyOtpSchema` allows `phoneNumber, code, challengeId, role`, but **rejects undeclared `deviceId`** (`additionalProperties:false`); role handling and post-OTP destination/token retention are not reconciled. Do not send this fixture as a production request before separate backend agreement.
- Server `RequestOtpHandler` currently enforces **60-second resend cooldown**; screenshot `00:30` is strictly static visual copy. No timing policy, ticker, resend activation, response retain, confirm modal, success toast, error card, timeout/expiry or attempts contract is frozen here.
- Inert `sequence` remains inert; one click emits one independently handled action. No ViewModel, screen-specific renderer, second Store, or invented `$response` binding.

## Next gates (discussion first)
1. Review OTP UI rendering in isolated Web preview; map all visual mismatches including segment widths on 411dp baseline and edge-to-edge form.
2. Review shared Login/OTP reserved validation geometry as one typed generic change; finalize 24dp versus dynamic multi-line text handling and accessibility scaling.
3. Freeze timer/resend separately: server-authoritative cooldown, timestamps and resume policy, enabled state, confirmation decision, replacement challenge retention, error and toast contracts.
4. Reconcile Verify OTP and Send OTP request/response/schema/role/session; backend still read-only.
5. Build/test gate: Domain/Data/Dynamic/App JVM, Web Wasm, Android compile, A35 interaction screenshots. No execution claimed for this JSON-only freeze.

**Only UI visual intent / JSON placement is frozen in this document.** Pending behavior is expressly not authorized for implementation, including timer and resend.
