# 135 — Login v10 design-spec palette, typography and geometry reconciliation

**Date:** 2026-10-23  
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`  
**Scope:** Login-only reference typography/palette plus three reusable, strictly typed line-height additions.  
**Status:** Source/JSON/tests committed; Gradle and visual acceptance pending.  
**Previous authoritative implementation:** Login v9 + Doc 134. `VisualLoginPreview.kt` stays unchanged as an independent reference.

## Source authority and reconciliation

The user supplied the **CB-Partner Login Screen Design Spec** image (Samsung Galaxy A35 5G; 411 × 891dp design base). Its typography/color/component spec has priority over the earlier `VisualLoginPreview.kt` visual defaults when values differ. It **does not** authorize changing Country Picker styles, interaction contracts, screen identity, errors-on-edit or backend. Previous v9 remains preserved for rollback/reference.

### Approved palette (source image)

| Token | Color | v10 usage |
| --- | --- | --- |
| Primary blue | `#007AFF` | Continue background and legal link spans |
| Primary dark | `#0A3A8D` | Brand heading and Welcome title (already matched) |
| Secondary | `#6B7280` | Welcome subtitle; legal description unchanged |
| Placeholder | `#9CA3AF` | Phone InputElement placeholder |
| White | `#FFFFFF` | Form/input surface and Continue text |
| Error background | `#FEE2E2` | Editable phone region **only** |
| Error text/icon | `#EF4444` | Error message and field outer invalid border |
| Screen background/top gradient | `#F8FAFC` | **Reference only:** current FormTemplate uses a full-bleed photo and separate authored theme gradient, so do not falsely replace the image with a flat fill. |

The reference also lists other colors not assigned to any concrete Login element; do not invent mappings. Field label and country code retain `#18263B`, input text `#111827`, country divider `#D1D5DB` because the supplied typography table does not specify alternatives for them.

### Text typography

| Visual text | Size/weight/lineHeight/color |
| --- | --- |
| Brand name | 20sp / 700 / 28sp / `#0A3A8D` |
| Welcome heading | 32sp / 700 / 40sp / `#0A3A8D` |
| Subtitle | 16sp / 400 / 24sp / `#6B7280` |
| Input text and placeholder | 16sp / 400 / 24sp / `#111827` / `#9CA3AF` |
| Continue | 16sp / 600 / 24sp / `#FFFFFF` |
| Terms and Privacy link spans | 14sp / 500 / 20sp / `#007AFF` |
| Inline error | 14sp / 400 / 20sp / `#EF4444` |

The design board does not name a font family. **Do not force Roboto or another new typeface.** Existing platform font selection remains untouched; `fontWeight` is supplied from JSON.

### Geometry

- Logo box: 32×32dp, brand-name-to-title spacing: 12dp, title-to-subtitle spacing: 8dp.
- Form horizontal and top inner padding: 16dp, bottom padding **24dp** because board separately calls for 24dp below terms/system inset. Branding remains 24dp horizontal margin.
- Field: height 56dp, radius 16dp. Continue: height 56dp, radius 16dp.
- The v9 validation slot was 24dp and after-slot padding 24dp, total **48dp**. v10 makes error-message typography 14sp/20sp, slot **32dp**, bottom gap **16dp**, total **48dp** — preserving constant layout height before Continue, even with 6dp message top spacing. Explicitly **not** a claim that physical input-to-button separation is only 16dp: a continuously reserved error slot must occupy space.
- Not yet exact: 72dp **status-bar-relative** logo top offset versus existing component top padding 48dp; 24dp subtitle-to-photo gap and −32dp bottom card/photo overlap (photo is currently a full template backdrop, not a separate measured child). These need device-inset and image-crop geometry verification before any change. **No screen-specific layout hack or speculative numeric conversion in v10.**

## Generic implementation only

1. `InputElement.lineHeight: Double?` and `ButtonElement.lineHeight: Double?` in Domain, mapped in `ElementMapper`, rendered as authored `sp` (or `TextUnit.Unspecified`) by their existing renderers. Input placeholder reuses input TextStyle, so it inherits the authored line height.
2. `DynamicValidationMessageStyle.lineHeight: Double?` in Domain, read/validated by `SectionMapper` and passed to `FormFieldSection`'s inline error Text.
3. Existing `TextElement.lineHeight` is already supported; no new TextElement API.
4. All new metrics are optional and positive/finite if authored. Zero/missing does not fabricate a typography default. No new category, State/Store, Action, Screen-specific renderer or helper without purpose.
5. Country Picker data and type/selection contracts, requests, hero source, flags/icons, existing `clearOnEdit` and form-field error ownership are unchanged.

## Tests and gates

- `DynamicResponseMapperTest`: generic text/input/button authored line heights, rejects nonpositive values.
- `DynamicValidationMessageContractTest`: input 24sp and field inline message 20sp mapping, invalid zero line height.
- `partner-login-country-picker-parity-v10.json` byte-identical active Web fixture; JSON subtree outside `template.components` equals v9.
- Run owner Windows target tasks `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack`; only after passing, compare actual Web UI vs supplied spec/VisualPreview for typography, text wrapping, error slot, card geometry, and picker isolation. No tests or UI rendering have been run by the GitHub connector at commit time.
- Remaining visual gaps (red error icon, gray Continue when invalid, status inset and hero overlap) require their own approved semantic contracts and must not be treated as implemented.

**Rule:** Color/font/size correction must not become screen-name checks in Kotlin; all differing values live in v10 JSON.
