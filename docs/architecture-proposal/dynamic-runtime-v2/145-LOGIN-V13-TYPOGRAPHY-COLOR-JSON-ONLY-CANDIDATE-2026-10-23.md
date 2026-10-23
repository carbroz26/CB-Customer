# 145 — Login v13: JSON-Only Typography and Palette Candidate

**Date:** 2026-10-23
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Parent:** `b4e1b723da7e47f5c1d56a8d4b5598e23dda769d`
**Status:** Owner-authorized JSON candidate committed; Gradle/device visual acceptance pending. v12 and v11 preserved as historical snapshots.

## Decision
The owner authorizes direct changes to already-supported JSON-authored colors, font sizes/weights, line heights, and typography to better match the original Login reference. **New Shield art and Hero image will be created separately.** No new frontend Kotlin development, no architectural change, no new renderer, no new properties, no backend work.

## v13 changes vs v12

| Element | Authored changes |
|---|---|
| `brand_name` | fontSize 22→24sp, lineHeight 30→32sp, color `#0A3A8D`→`#092F74` |
| `welcome_title` | fontSize 32→40sp, fontWeight 700→800, lineHeight 40→48sp, color `#0A3A8D`→`#092F74` |
| `welcome_subtitle` | fontSize 16→18sp, fontWeight 400→500, lineHeight 24→27sp, color `#6B7280`→`#52769F` |
| `phone_label` | fontSize 14→16sp, fontWeight 600→700, color `#18263B`→`#0A3A8D` |
| `country_selector` | fontSize 15→16sp, color `#18263B`→`#0A3A8D` |
| `mobile_number` | fontSize 16→17sp, placeholderColor `#9CA3AF`→`#91A6C1` |
| `continue_button` | fontSize 16→18sp, fontWeight 600→700, lineHeight 24→26sp, blue `#007AFF`→`#176AD6` |
| `legal_text` | fontSize 12→13sp, color `#6B7280`→`#7B8EA9` |
| `legal_links` | fontSize 14→15sp, fontWeight 500→600, lineHeight 20→22sp, each link-span blue `#007AFF`→`#176AD6` |

**Approximation warning:** These values are informed visual candidates, NOT extracted exact reference pixels or confirmed font family. Current typed `TextElement` has no `fontFamily` property; no new Kotlin support is approved.

## Future art direction: separately owned
- **Shield:** replacement artwork with stronger, proportional visible shield and crisp badge detail; use the already-approved explicit packaged asset contract when art is supplied. Current shield asset source, existing 44×44dp box, scale and spacing remain untouched for this candidate.
- **Hero:** replacement photo/art composition should place skyline starting just below `Access your partner account`; show worker from head through waist, entire washing arm and hand, visible car-washing action and car; avoid excess empty sky and avoid hiding the subject under the white form card. Current packaged resource `images/partner_login_hero_02`, `crop`, `center` remain unchanged until the replacement asset is approved.

## Unchanged and checks
- `data.template.properties` including the hero; `data.template.components[0].properties` and Shield leading image subtree.
- Login white card, its 56dp phone input and 56dp button geometry, 16dp input-to-button gap, focus/validation styles, clearOnEdit, request action and retained-binding contract.
- `data.presentations`: entire Country Picker and search/selection/empty fallback; `data.initialBindings`; system insets/IME and all backend contracts.
- India-only `phone_number` validator stays (Doc143). No changes to production Kotlin source, Swift, Gradle, or other assets.
- `29` structural leaf-level JSON paths differ; v13 versioned JSON and active Android/Web preview fixture must be byte-identical.

## Device / automated acceptance gates
Run `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack :androidApp:installDevelopmentDebug`. Capture Samsung A35 with keyboard closed and confirm `Welcome back` fits one line and subtitle/button/legal content does not clip. Test focus/keyboard dismissal, empty-invalid input, `clearOnEdit`, and Country Picker opening/search. Freeze visual values only after owner screenshot review. Final logo and hero parity are expressly pending replacement images.

**No claim of end-to-end backend success or exact typography match.** Previous v12 remains intact as rollback/reference.
