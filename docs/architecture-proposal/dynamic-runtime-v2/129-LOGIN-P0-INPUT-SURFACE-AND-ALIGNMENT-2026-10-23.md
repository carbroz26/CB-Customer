# 129 — P0 Login generic Input surface and authored alignment
**Date:** 2026-10-23
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Scope:** Narrow P0 source + versioned JSON update; approved VisualLoginPreview stays unchanged.

## Changed
- `StackSection(phone_label_section).horizontalAlignment = start` so the form label is start-aligned instead of the absent-property center mapping.
- For `phone_input_row`, Divider `spacing.end` becomes 0 and Input `padding.start` becomes 12; editable field's 12dp text inset is now *inside* its tinted surface. Both phone and country-search Inputs explicitly author `textAlign: start`.
- Generic non-segmented `InputElement` now applies authored background/border on its outer Input Row, enclosing leading/trailing Content. Authored padding is applied **after** surface styling, inside the field, rather than outside on the parent Row.
- `DynamicLayout.applyLayout(InputElement)` only applies dimensions/fill; segmented input retains the existing outer padding and per-segment border/background path.
- `InputSurfaceLayoutContractTest` checks typed mapping for label, phone padding and search leading content/surface.

## Scope guard
- Historical v4 remains unchanged. New source JSON: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v5.json`; active Web fixture is byte-identical to v5.
- Do not change Theme/hero, form/card layout, validation rules/messages, Store, action handlers, country list/selection, assets, legal URLs or OTP.
- New assets are still unavailable. Placeholder icons are not introduced.
- JVM/Android/Wasm build and screenshot comparison are acceptance checks and must be independently executed, not inferred from source inspection.
