# 125 — Login and Country Picker: actual Dynamic rendering parity

**Date:** October 22, 2026  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Stage:** Source-confirmed audit + initial bounded sheet, full-size form positioning and rounded-row clipping increments, **NOT** complete visual acceptance.

## Protected references

- Owner-approved Web-only visual reference `webApp/src/wasmJsMain/kotlin/com/carbroz/cbpartner/web/VisualLoginPreview.kt`: **unchanged**.
- Historical frozen integration target `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-integration-v2.json`: **unchanged**.
- Current incremental Dynamic parity JSON: `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-parity-v3.json`; Web `webApp/src/wasmJsMain/resources/preview/partner-login.json` is an exact copy.
- OTP contract v2 and all backend source remain untouched.

## Comparison of current source against approved visual reference

| Area | Approved visual reference | Actual Dynamic status or gap |
| --- | --- | --- |
| Main branding | Shield, 20sp title, 32sp heading, subtitle | Typed Text rendering exists; shield asset `images/cb_partner_logo` has no registered packaged resource |
| Theme/hero | Light blue/teal background and skyline placeholder | Frozen Theme 135° gradient is independent of Template hero; `/images/partner_login_hero.jpg` is unverified and may fail, no abstract skyline equivalent is allowed in production |
| White login card | Edge-to-edge, top-only 24dp rounding, internal 24dp margins | Typed component supports full width, shape, padding; actual on-device positioning/scroll parity not yet screenshot-tested |
| Phone input | Entire row red error border; number-only light red tint | Group validation and input background are supported as authored; dynamic validation message reserve is NOT supported on recovered baseline |
| Country source | 8 illustrative countries in visual prototype | Offline Dynamic preview previously had 4; aligned to 8 **UI-only** samples. Production CountryCatalog intentionally has no authorized data |
| Country Picker shell | Edge-to-edge, approximately 70% viewport height, white top-rounded sheet | Prior PresentationHost lacked backend height; now typed `heightFraction`, `background`, `shape`; existing typed StackComponent `fillMaxHeight` grants list finite height |
| Country Picker rows | Search, selected row, separators, empty state | Generic ListSection evaluator, renderer and selection exist; exact keyboard/scroll/selection/screenshots still unverified |
| Flags/icons | Drawable flags, search, close, chevron, selected check | `DynamicPackagedAssets.kt` maps no Login resource IDs. Do NOT substitute invented emoji/glyphs/flags |
| Legal | Two-line display, clickable Terms/Privacy | Dynamic spans/actions exist, but trusted `$config` values need verified owner-approved legal URL injection |
| Real OTP API | Request/retain/navigation to OTP | Backend contract agreement remains blocked; network errors/loading to compare after UI parity |

## Versioned JSON change (strictly scoped)
1. Add `data.presentations[country_picker].properties.heightFraction = 0.70` (owned by backend).
2. Add `data.presentations[country_picker].properties.background = {color:#FFFFFF}`.
3. Add `data.presentations[country_picker].properties.shape` for top-start/end 24dp and lower corners 0.
4. Add `data.presentations[country_picker].components[picker_content].properties.fillMaxHeight = true`.

All unrelated Theme, Template background image, country search, selection mapping, validation, actions and IDs are byte-for-byte semantically unchanged.

## Source implementation ownership
- `BottomSheetPresentation` owns typed size/surface properties, and `PresentationMapper` validates positive height fraction not exceeding 1.
- `DynamicPresentationHost` applies backend surface color/rounding/height without any country-picker-specific code or default token.
- `StackComponent` separately owns authored `fillMaxHeight`, allowing weighted `ListSection` to render within the bounded viewport.
- `FormTemplate` preserves the available viewport as the minimum vertical scroll-content height for a full-size, scrollable form; this allows backend-authored `spaceBetween` to position the bottom Form at screen bottom when it fits.
- `DynamicLayout.applyLayout(StackGroup)` clips children to its backend-authored shape, so the number-only error tint cannot bleed through the rounded whole-row border. The validation message remains outside the clipped row.
- Source changes are generic to future forms/sheets and preserve existing defaults when these optional properties are absent.

## Verification gates and stop conditions
- JSON parse + old-target semantic diff only for four paths.
- DTO/Domain mapper regression for known fraction and negative/out-of-range values; generic component height property.
- JVM tests, Android compile, Wasm webpack and actual visual 411×891 screenshot review: **NOT executed here**; do not claim success.
- Do not add graphics/policy mocks to production. Real flag/logo/icon/hero, trusted legal URLs and approved Country data are external acceptance blockers.
- Do not change `VisualLoginPreview.kt`, OTP or backend to fix generic Dynamic UI gaps.
- Next small increments: reserved validation layout, full-width/keyboard screenshots, authored legal line layout, icons/asset evidence and binding/selection validation; proceed only when verified source evidence supports them.

This increment is for **layout capacity**, not proof of pixel-perfect Dynamic parity.
