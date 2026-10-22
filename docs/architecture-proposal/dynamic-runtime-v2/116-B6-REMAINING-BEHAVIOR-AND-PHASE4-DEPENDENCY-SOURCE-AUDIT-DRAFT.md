# B6 — Remaining Behavior, Regression Gaps and Phase-4 Dependency: Source Audit (DRAFT)

**Date:** 2026-10-22
**Status:** SOURCE AUDIT DRAFT — NOT FROZEN; NO IMPLEMENTATION AUTHORIZED
**Frontend:** `carbroz26/CB-Partner` / `architecture/dynamic-runtime-v2-discussion-freeze`
**Reviewed source SHA:** `c28e4732fea50daf5bf036eaa84d3c055a1594e6`
**Scope:** Review remaining L-01–L-15 behavior and verification, distinguish B6 candidates from Phase-4 ListSection runtime. No Kotlin/JSON/Gradle/backend modifications.
**Related source of truth:** V2 Doc 112 §§7, 8, 16–19, 21–23; frozen B4 Doc 113; reviewed/frozen B5 Docs 114–115; canonical target JSON `partner-login-country-picker-current-target.json`. Historical passages in Doc 112 do not override later explicit B3b/B4/B5 decisions.

## A. Verified baseline (do not re-implement)

- B1 gesture package move, B2 DividerElement, B3 ImageContent/typed remote assets, B3b Template-owned image, B4 generic pending Button and B5 `emptySectionId` are source-committed and owner-reported automated build PASS. Their Android/Web visual acceptance is not established by Gradle alone.
- B5 maps `ListSectionDto` -> domain `ListSection.emptySectionId` and checks a unique sibling StackSection inside the same StackComponent. It does **not** register or render ListSection, filter items, own selection, or conditionally display fallback.
- Approved canonical Country Picker JSON has `country_list.properties.emptySectionId = "country_list_empty_state"`; `picker_content.sections[]` includes that sibling StackSection with the original `empty_content` Group and three original Elements unchanged.

## B. Source completeness and pending gaps

| ID | Source evidence / observed gap | B6 disposition |
|---|---|---|
| B6-V1 | `DynamicFormValidator.kt` resolves named validator arguments and calls `DynamicValidatorRegistry.requireValidator`. `DynamicModule.kt` builds registry from Koin `getAll<DynamicValidator>()`. Current inspected Dynamic implementation has no concrete `phone_number` validator. Registry/sample validator unit tests exist, but they do not establish production phone validation for region-dependent input. | **B6 candidate.** Confirm product-specific owner, country metadata, argument error behavior, and externally registered implementation before code. Never hardcode country phone rules inside generic Dynamic. |
| B6-V2 | `DynamicReferenceResolver.kt` accepts only `$binding`, `$context`, `$state`, `$literal`. Canonical JSON has two `$config` legal URI references (`legal.termsUrl`, `legal.privacyUrl`). No `$config` resolver behavior is implemented. | **B6 contract/design gate.** Determine authoritative app-config source, typed reference scope and security before extending the generic resolver; no substitute `$context` or hardcoded legal URL. |
| B6-V3 | `NetworkException.kt` distinguishes RateLimit/Validation/Server/Connection/Timeout and other errors. `DynamicRepositoryImpl.kt` forwards exception failure; `DynamicStore.kt` currently sets generic `Unable to complete action` on Request failure; `DynamicContainer.kt` displays generic `Surface`/`Text`/`Dismiss`. This is not the backend-authored inline-vs-red-alert design. | **B6 candidate.** Freeze typed safe error projection and backend-owned presentation/style rules first. Do not print raw server text/secrets or invent a frontend style fallback. |
| P4-L1 | `domain/.../section/ListSection.kt`, `data/.../section/ListSectionDto.kt`, `SectionMapper.kt`, `ComponentMapper.kt` support the type/ref, but no `feature/dynamic/.../sdui/section/ListSection.kt` renderer and no `.section(ListSectionModel::class, ...)` in `DynamicRendererRegistration.kt`. | **Phase 4 only. Missing production renderer is a known dependency, not a B5 regression or B6 quick fix.** |
| P4-L2 | `feature/dynamic/.../sdui/component/StackComponent.kt` renders all sibling Sections unconditionally. A valid B5 target therefore cannot be shown correctly without future logic to reserve the referenced fallback Section from normal siblings, rendering it solely when its owning list has no visible rows. | **Phase 4 only.** Do not try to hide sibling through ad hoc `visibleWhen` or list-specific behavior in B6. |
| P4-L3 | `DynamicReferenceResolver` does not support `$item`; canonical list uses it for `flagAsset`, `name`, `dialCode`. Source/catalog resolution, filtering, `selectedStyle`, `visibleWhen.selected`, result selection, itemSeparator and lazy list body lack complete ListSection runtime implementation. | **Phase 4 only, separately planned/frozen.** ListSection owns item context/filter/selection, StackGroup remains generic row renderer. |
| INT-1 | Canonical target's historical `theme.properties.backgroundImage` resource field is superseded by B3b's Template-owned URL-image contract (Doc 112 §18). B5 intentionally preserved this unrelated original JSON value. | **Integration-contract reconciliation needed before full-screen smoke.** Do not silently edit B5's canonical L-10-only migration or move image into Theme. |
| VERIFY-1 | The recent owner-reported `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack` completed successfully for B5. That result verifies mapping/compilation, **not** end-to-end Country Picker or physical Button/image display. | **Verification backlog.** Distinguish automation, Android physical device, Web smoke, and Phase-4-blocked flows. |

## C. Recommended B6 audit-to-freeze workflow (no source changes yet)

1. Reconcile each remaining L-01–L-15 acceptance item against current source and exact owner-approved plan; retain `B6-V1–V3` as *candidate* labels until that reconciliation is signed off. Other L-12–L-14 visual/regression acceptance items must be tracked separately from feature creation.
2. For phone validation: review `DynamicValidator`, `DynamicValidatorRegistry`, `DynamicFormValidator`, `DynamicModule`, app composition/Koin registration, country metadata and validator tests. Identify the owning feature/module outside the generic Dynamic runtime. Produce exact test matrix for region/blank/malformed/unavailable metadata.
3. For `$config`: review typed reference resolver, `DynamicRenderContext`, `RequestActionHandler` and legal span/URI actions, the app-config source and action URL safety contract. Specify missing configuration behavior, snapshot lifecycle, ownership and tests before implementation approval.
4. For error UI: trace typed `NetworkResult`/`NetworkException` -> `DynamicRepositoryImpl` -> `RequestActionHandler` -> `DynamicStore` -> `DynamicContainer`; identify an allowed structured, sanitized error contract and backend-driven alert-vs-inline properties without changing the current B4 lifecycle.
5. Add source/test regressions only after a **new exact file-by-file B6 plan is reviewed, frozen and separately authorized**. Source-only audit does not authorize changes.
6. Maintain separate Phase-4 audit/plan for `ListSection` renderer+registration, catalog and `$item`, filtering, stable row identity, selection, separator, virtualized rendering and `emptySectionId` fallback consumption. No unapproved ListElement, new seventh category, second Store, custom list repository or hardcoded country UI.

## D. Verification and decision gates

- B6 review should document concrete files, contract decisions, tests and unchanged modules; distinguish source verified, unit test verified, UI manual pending, Phase-4 blocked. No false full-screen or renderer readiness claim.
- Before changing B6 production source, freeze a separate scope with exact implementation owners and expected backend/app-config dependencies. Backend repository remains read-only until independently authorized.
- Preserve the six-category architecture, KMP shared types, Compose renderer registry discipline, MVI/UDF Pure Store, explicit DI, current safe diagnostics and backend-driven visual styling.

**Current status:** B6 source audit recorded; exact implementation plan and freeze pending owner review. Phase 4 ListSection remains explicitly separate and not authorized. No production code edits made here.
