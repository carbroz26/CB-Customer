# 146 — Login v15: new resource assets + generic inline validation icon

**Date:** 2026-10-24
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Uploaded assets commit:** `cab83396b9b3b335abdef4143ca6227dbf5b74af`
**Status:** Source and versioned JSON implemented; Gradle and device regression pending.

## Scope and rules

The owner supplied nine PNG files. Login v15 activates exactly six approved final variants. These are registered via the existing explicit `DynamicPackagedAssets` and referenced in the existing typed Dynamic JSON. Historic asset keys and archived v14 JSON are preserved. Other three uploaded image variants remain unused.

| Placement | Resource key | File |
| --- | --- | --- |
| Login brand ImageContent | `images/cb_partner_logo_02` | `cb_partner_logo_02.png` |
| FormTemplate background | `images/partner_login_hero_04` | `partner_login_hero_04.png` |
| Country code arrow | `icons/chevron_down_01` | `icon_chevron_down_01.png` |
| Country Picker search | `icons/search_01` | `icon_search_01.png` |
| Country Picker close | `icons/close_01` | `icon_close_01.png` |
| Inline validation marker | `icons/error_alert_icon_01` | `error_alert_icon_01.png` |

## Reused generic contract

- Existing `DynamicValidationMessageStyle` gains optional `leadingContent: List<ElementContent> = emptyList()` (domain). No changes to how errors are computed.
- Existing `ElementContentMapper.mapList` converts `messageStyle.leading` to existing `ImageContent` via the normal typed resource source rules. No new icon model, specialized Login component, Store, or asset resolver.
- `FormFieldSection` displays `leadingContent` beside the message inside a Row **only if an error exists**. Without error, neither icon nor empty reserved slot appears.
- Authored validation asset is 16×16dp with 6dp end spacing; the existing `messageStyle.spacing.top=6dp` remains. Existing error border and clear-on-edit behavior remain.
- Validation contract test verifies asset source/size/spacing and rejects invalid width/unknown style fields. Packaged asset tests cover all six resources.

## Other authored JSON change

The Welcome heading-to-subtitle space is reduced by adjusting existing `welcome_title.spacing.bottom` from 8dp to 4dp, in response to owner screenshot feedback.

## Preserved

Existing six-category hierarchy; single Store; India-only `phone_number` validator; country list/catalog/selection/actions; modal and keyboard behavior; every UI dimension except welcome bottom spacing and added error icon row; all backend contracts. Unused uploaded assets `cb_partner_logo_01`, `error_alert_icon`, `partner_login_hero_03` remain inert.

## Verification and gating

- V15 JSON and the Web/Android development preview JSON must be byte-identical; previous v14 preserved for rollback.
- Owner to run `:data:jvmTest :feature:dynamic:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack :androidApp:installDevelopmentDebug` on Windows. GitHub editing does not execute Gradle.
- On Samsung A35 verify hero framing relative to form card, shield size/background transparency, Country Picker open/search/close/selection, normal no-error state, empty/invalid submit (red icon), clear-on-edit, keyboard and navigation-safe bottom.
- Do not claim exact artwork parity until screenshot acceptance; do not modify backend, native screen or UI JSON further without evidence.

**Contract remains generic; only new approved keys are authored.**
