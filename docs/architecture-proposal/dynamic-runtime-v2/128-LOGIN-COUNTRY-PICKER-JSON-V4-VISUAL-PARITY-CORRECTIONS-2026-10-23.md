# 128 — Login + Country Picker JSON v4: Visual Preview parity corrections
**Date:** 2026-10-23  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Scope:** JSON-only, versioned; no production renderer, Store, backend, OTP, or VisualLoginPreview changes.

## Status
Applied safe, directly authorable styling/layout corrections from owner-approved Web visual preview to the current Login v3 JSON. This is a **JSON parity iteration, not 100% screenshot or behavior acceptance**. Source/visual runtime verification remains pending.

## Versioned files
- Immutable predecessor: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v3.json`
- New integrated target: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v4.json`
- Web actual-Dynamic fixture: `webApp/src/wasmJsMain/resources/preview/partner-login.json` (byte-identical to v4)
- Standalone approved visual: `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/VisualLoginPreview.kt` (unchanged)

## Applied corrections
1. Branding: image-content dimensions 26×30 and spacing 9; subtitle color #64748B.
2. Phone row: country selector minHeight 56, chevron size 12 and left spacing 5, divider start spacing 0; other separator/input spacing unchanged pending correct number-only tint geometry.
3. Continue: 16sp, #1769D2 background, radius 14, button-to-legal Section bottom padding 16.
4. Legal: one `legal_section` with two direct TextElements; centered 12sp agreement line, bottom spacing 4, second centered link line separated by `   |   `; both original legal `external_uri` actions and `$config` references preserved.
5. Country Picker: header padding start20/top8, title18sp/600, close minimum target32; search section top padding0, input 46 high, radius9, #D6E2F0 border, 13sp #18263B, search icon16 and spacing9.
6. Country list: one repeated row Group height49 with zero vertical padding, normal country-name weight400; keep existing selection, filtering, separators and stable identity.
7. Empty search fallback: retain `ListSection.emptySectionId` sibling StackSection ownership; simplify its Group to one centered "No countries found" text (14sp, #64748B) with padding28 to match visual prototype.

## Preserved / intentionally not changed
- Theme's frozen 135-degree gradient and Template URL hero placement (Document 120); branding 430dp max-width capacity not currently typed, and no new property introduced.
- Exactly one Template, the same 2 main Components, all 4 Form Sections, 3 phone-row Elements, 4 Picker Sections and all existing IDs except added `legal_links` and removed obsolete empty fallback element IDs.
- Validations, bindings, request destination/retain/mode, country selection mappings, external legal links, sheet fraction/shape and catalog.
- No unsupported JSON keys for reserved validation space, validator-specific copy/icon, validation-driven button style, per-row selected font/color, or exact input inner-padding contract.
- Preview-only dummy shapes/countries were not added to production assets/catalog; approved hero/logo/icons/legal URLs remain integration dependencies.

## Remaining gates
- Verify the generic DTO mapper accepts the modified JSON and execute targeted runtime tests.
- Compare the two Web preview routes at matched viewport/device density, including form bottom anchoring, input tint/clipping, keyboard, picker list/empty/selected states.
- Reconcile validation message/icon/reserved space and button invalid-state styling as a single typed contract only after verifying existing source limits.
- Do not claim parity until source, builds and visual tests provide evidence.
