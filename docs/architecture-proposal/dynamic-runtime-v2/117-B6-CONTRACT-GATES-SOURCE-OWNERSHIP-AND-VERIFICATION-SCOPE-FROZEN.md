# 117 — B6 Source-Reconciled Decisions, Exact Ownership, Verification Scope: PLAN FROZEN

**Date:** 2026-10-22
**Owner direction:** Review B6 remaining contract decisions and the exact file-by-file scope; preserve Phase 4 ListSection as a separate mandatory dependency.
**Status:** B6 **REVIEW / CLASSIFICATION / VERIFICATION SCOPE FROZEN**. Remaining behavioral design gates explicitly OPEN. **No B6 Kotlin, backend, asset, target-JSON or UI implementation is authorized or claimed complete.**
**Frontend:** `carbroz26/CB-Partner`, branch `architecture/dynamic-runtime-v2-discussion-freeze`
**Reviewed source HEAD:** `fb8f99e1473b4d1c8556f2de81ce925f981b7e8c` (Doc116 added after owner-verified B5 build at `c28e4732`)
**Authoritative evidence:** V2 Doc112 (especially §§7–8,16–19,21–23), B4 Doc113, B5 Docs114–115, audit Doc116, current canonical `partner-login-country-picker-current-target.json`, actual source/tests. The older historical L-01–L-15 gap checklist must be reclassified against *today's* source rather than treated as an implementation backlog verbatim.
**Backend:** `carbroz26/carwash-backend`, `feature/backend-migration`: READ ONLY, no B6 or Phase-4 backend edits.

## 1. Freeze interpretation and separation of concerns

- **Frozen now:** exact current-state classifications, responsibility boundaries, evidence/verification gates, and the explicitly listed existing source files to inspect/test before any later production change.
- **Not silently frozen now:** phone validation rules and dependency/owner; `$config` provider/source/lifecycle; safe backend-error-to-UI schema; actual asset inventory; backend OTP response and device-dependent behavior. Their *existence as open gates* is frozen, but their *solutions* require separate owner discussion/contract freeze.
- **Separate Phase 4:** `ListSection` renderer/registration, catalog data, `$item` scoping, filtering, row selection and virtualized UI, separators, and conditional `emptySectionId` fallback. Neither B6 nor a test-only B6 authorization may implement those.
- Maintain six-category typed DTO/Domain/Mapper/Registry/Renderer architecture, Pure Store (no ViewModel), strict SRP, no duplicate network/navigation/binding resolver, no hardcoded country-specific generic renderer, no UI defaults for backend-authored styling, and no raw data logging.

## 2. Corrected L-01 through L-15 status (verified source vs future acceptance)

| ID | Actual current source interpretation | Decision / owner of next step |
|---|---|---|
| L-01 | `DividerElement` UI renderer and registry registration exist. | B2 DONE; retain Android/Web visual acceptance separately. No B6 rewrite. |
| L-02 | `ImageContent` renderer and content registration exist. | B3 DONE; retain real asset visual acceptance separately. |
| L-03 | `DynamicValidator`, `DynamicValidatorRegistry`, `DynamicFormValidator` and Koin `getAll<DynamicValidator>()` exist; `App.kt` does not register a concrete `phone_number` provider. | **B6 CONTRACT GATE OPEN:** product/domain-owned validation policy, region metadata/source, safe invalid/unavailable behavior and provider DI. Generic Dynamic must not contain country-specific phone validation. |
| L-04 | Typed Resource/Url source and origin-trusted remote asset renderer exist. | B3 DONE for typed asset runtime; exercise source/fallback tests and visual acceptance. |
| L-05 | `DynamicPackagedAssets.kt` explicitly has no packaged Login/logo/flag/icon entries, so resource-source images from target are not displayed. | **B6 ASSET INPUT GATE OPEN:** obtain approved actual assets/licensing/platform packaging; no fake drawable or arbitrary mapping. |
| L-06 | B3b implemented URL-sourced image on `template.properties.backgroundImage` behind entire Template; canonical B5 target still contains obsolete `theme.properties.backgroundImage` with Resource source. | **INTEGRATION GATE OPEN:** separate owner-approved canonical target/back-end JSON reconciliation, not an L-10 edit. Do not silently rewrite historical target. |
| L-07 | B4 scoped Button Request loading and cleanup implemented/tested. | B4 DONE automated; physical/Web spinner/interaction/accessibility smoke pending. No B6 rewrite. |
| L-08 | `ListSection` is DTO/domain/mapper only; no `sdui/section/ListSection.kt` or registration. | **PHASE 4 REQUIRED** (not B6). |
| L-09 | Catalog/row `$item`, filter/search, selection, stable identity, selected visuals and separator rendering not implemented as complete list runtime. | **PHASE 4 REQUIRED** (not B6). |
| L-10 | B5 `properties.emptySectionId` with same-Component sibling StackSection reference mapping verified; fallback UI consumption absent. | B5 DONE contract; **PHASE 4 REQUIRED** for conditional display and ordinary-sibling suppression. |
| L-11 | `DynamicReferenceResolver` supports `$binding`, `$context`, `$state`, `$literal`, not `$config`; canonical JSON contains two `$config` legal links. | **B6 CONTRACT GATE OPEN:** authoritative app-config provider, safe URI policy and missing-key behavior; no `$context` workaround. |
| L-12 | Generic `RequestActionHandler`, request validation, typed binding resolution, retained-state extraction and navigation-effect commit already tested. | **B6 END-TO-END VERIFICATION OPEN:** concrete Send OTP backend behavior, named phone validator, server destination/retained response and Android/Web flows not proven. Do not alter Request contract speculatively. |
| L-13 | `NetworkException` distinguishes rate-limit, validation, server, connection, timeout etc; `DynamicStore` exposes generic safe failure messages; `DynamicContainer` shows generic Surface/Text, not frozen backend-authored red alert. | **B6 CONTRACT GATE OPEN:** sanitized category-to-UI data and authored alert/inline styling semantics, before implementing. Existing B4 lifecycle must remain unchanged. |
| L-14 | Automated JVM/Android compilation/Webpack B1–B5 PASS as owner reported; no complete Android/Web UI smoke for target Login/Country Picker. | **B6 VERIFICATION OPEN**, split supported states from Phase-4-blocked states. iOS device validation unavailable on Windows. |
| L-15 | Shared `DynamicGestureModifier.kt` is already located directly in `sdui/`; Element and Content callers import shared `sdui.dynamicGestures` / `reportDynamicOrigin`. | **B1 DONE**; do not move or duplicate. |

## 3. Exact file-by-file B6 ownership manifest

These are **existing, source-verified file paths** and their B6 disposition. `READ/KEEP` is not permission to edit; `TEST AFTER GATE` indicates the precise test owner for a separately approved behavior.

| Existing file | B6 purpose and disposition |
|---|---|
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicValidator.kt` | READ/KEEP; contract for externally registered validator. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicValidatorRegistry.kt` | READ/KEEP; fail-closed unknown/duplicate keys already supported. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicFormValidator.kt` | READ/KEEP until L-03 validation semantics/provider frozen. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicModule.kt` | READ/KEEP Koin `getAll<DynamicValidator>()`; app/product provider registration after L-03 owner freeze, not generic hardcoded registration. |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt` | READ/KEEP current assembled module list. A **new** product-validator DI module/file is NOT yet authorized/named; ownership pending L-03 contract. |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicFormValidatorTest.kt` | TEST AFTER L-03 freeze: named args, valid/invalid, unsupported/unknown region, unavailable metadata, required/blank/length and cancellation/failure behavior. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolver.kt` | READ/KEEP; `$config` is currently unsupported and fails closed. Changes require L-11 typed provider/URI contract freeze. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicRenderContext.kt` | READ/KEEP for consumer/reference propagation; no new generic property bag. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/element/TextElement.kt` | READ/KEEP spans/legal link consumer. Any `$config` resolution requires separately frozen source, not local hardcoded URI. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/ExternalUriActionHandler.kt` | READ/KEEP action URI resolution. Apply only separately frozen URI rules. |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/runtime/DynamicReferenceResolverTest.kt` | TEST AFTER L-11 freeze: typed/missing config, privacy, unsupported namespace and URI edge cases. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicPackagedAssets.kt` | READ/KEEP explicit genuine-asset registry; no approved Login drawables exist in current map. Require actual originals before change. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicAssetImage.kt` | READ/KEEP trusted-URL + resource lookup; no speculative fallback or CDN relaxation. |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/asset/DynamicAssetImageTest.kt` | VERIFY existing URL/rejection tests; expand only against approved genuine asset set after L-05 gate. |
| `network/src/commonMain/kotlin/com/carbroz/cbpartner/network/NetworkException.kt` | READ/KEEP typed categories; do not leak raw messages to UI. |
| `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicRepositoryImpl.kt` | READ/KEEP exception propagation and safe trace; error projection contract not frozen. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandler.kt` | READ/KEEP generic request body/retain/destination semantics. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt` | READ/KEEP validation lifecycle, B4 actionJob/pending, safe error state; only bounded change after L-13 contract/owner approval. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicScreenState.kt` | READ/KEEP transient state; any error-presentation additions need explicit owner decision. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt` | READ/KEEP generic current error surface. Any authored inline/alert UI contract must first be frozen; no new login-specific widget. |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/RequestActionHandlerTest.kt` | L-12 verification: retain/navigation and body resolution happy/rejection paths, no raw secrets in logs. |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStoreTest.kt` | L-12/L-13 verification: blocking invalid input, navigation-effect atomic handoff, failure/cancellation, B4 spinner cleanup and scoped Present/Dismiss. |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt` | READ/KEEP sole app-level navigation/effect-consumer owner; no second navigation system. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRendererRegistration.kt` | READ/KEEP. ListSection registration is **Phase 4**, not B6. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/component/StackComponent.kt` | READ/KEEP. Future fallback exclusion/consumption is **Phase 4**, not B6. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/StackSection.kt` | READ/KEEP. No temporary list/fallback behavior. |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/DynamicGestureModifier.kt` | KEEP; package placement corrected in B1. |

**What is not yet an exact, approved implementation file list:** new concrete product `phone_number` validator implementation, app-config source/provider, sanitized backend error DTO/model, canonical hero URL and real image asset files. Neither actual owners nor payload semantics are frozen. Do not invent a path to make the plan appear complete.

## 4. Frozen B6 verification matrix and readiness gates

1. **Source/regression baseline:** re-run existing `:domain:jvmTest :data:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack`. This is a verification request, **not** an implementation approval. No Gradle cleanup based on non-blocking Windows/iOS/Web warnings.
2. **L-03:** before validation integration testing, freeze expected phone format/region rules, metadata source, unknown-region behavior, and product module DI. Existing `DynamicFormValidatorTest` proves registry mechanics, not production phone validation.
3. **L-05:** before visual acceptance, supply genuine supported platform asset set and decide resource-vs-trusted-backend-URL representation. Document which actual keys exist; no native glyph map, fake flag or untrusted CDN fallback.
4. **L-06:** reconcile canonical backend target's old `theme.properties.backgroundImage` Resource field with B3b `template.properties.backgroundImage` URL source in a **separately approved JSON contract revision**; do not modify B5 canonical migration silently.
5. **L-11:** before link acceptance, freeze typed app-config source and snapshot/refresh ownership, URI security (scheme/host policy), missing-key fallback and the provider-to-resolver contract. Test that `$response` remains forbidden and `$item` remains Phase 4.
6. **L-12:** with an approved validator and reachable backend, check invalid input suppresses request; valid phone and country bindings produce exact approved Send OTP body; successful response retains only declared paths and hands off destination to the existing navigation authority; cancellation and failure do not commit or navigate.
7. **L-13:** before UI implementation, freeze safe network-result classification (validation/rate-limit/server/connection/timeout), allowed backend-authored title/message/styles/fields, red alert vs inline mapping, accessibility, dismissal/retry and no sensitive logging. Generic Button pending must clear in every terminal/cancellation path.
8. **L-14 / visual:** Android physical device and Web acceptance for supported screen states: hero actual URL, logo/flag/icon genuine assets, keyboard/focus/validation/loading, modal open/dismiss/return, navigation and screen retention. Full Country Picker list/search/select/empty fallback cases remain explicitly **Phase 4 blocked**, not B6 PASS.
9. **Platform:** iOS device/simulator cannot be validated on Windows; keep this pending rather than asserting parity.

**B6 must not claim the current canonical target is executable:** it still has `$config` and `$item` refs, non-packaged Resource assets and old Theme-image placement. `ListSection` has no registered renderer; current StackComponent attempts to render ordinary siblings, so simply mapping the document cannot show the intended picker/fallback.

## 5. Phase-4 hard dependency contract — SEPARATE PLAN REQUIRED

The separately planned Phase 4 MUST include all of:

- **CREATE** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/section/ListSection.kt` as a concrete generic Section renderer (name/path is candidate until Phase-4 file plan freeze).
- **MODIFY** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/registry/DynamicRendererRegistration.kt` to register `ListSectionModel` to the new concrete renderer.
- **MODIFY/REVIEW** `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/sdui/component/StackComponent.kt` so a StackSection referenced through `ListSection.emptySectionId` is **not rendered as an ordinary sibling**; render it only as the owning ListSection empty fallback. No duplicated rendering and no feature-specific `visibleWhen` workaround.
- Implement generic list source/catalog ownership, row-local read-only `$item`, filter/search, stable row keys and selection with atomic result mapping; selectedStyle/selected-visible Content semantics, separators only between visible rows, and scrolling/virtualization as contract requires.
- Validate interactions for Screen vs Presentation scope, missing/duplicate item keys, zero/nonzero filtered items, navigation-entry/presentation lifecycle and app-config independence. Preserve generic six categories; do not create `ListElement`, a second Store, a List-only networking layer, country-specific renderer, or a seventh category.
- **NOT AUTHORIZED:** This Phase-4 scope is mandatory for future planning but is **not** an approved Phase-4 implementation or exact final file manifest. Discussion → architecture review → exact files/test plan → owner freeze → separate implementation authorization required.

## 6. Stop/go rule

**B6 PLAN FROZEN =** this issue classification, verification matrix and ownership/keep boundaries. It is not a claim that L-03/L-05/L-06/L-11/L-12/L-13/L-14 are implemented or that their missing contract choices have been made. B6 source implementation may proceed **only** after the corresponding contract gate is separately frozen and the owner explicitly authorizes that bounded batch. At every review/implementation step compare actual HEAD/source first. Retain current branch; do not create temporary branches, edit backend, revise frozen UI tokens or broaden Phase 4 under B6.

**Current stage:** B6 source audit and scope/decision-gate plan FROZEN; implementation NOT AUTHORIZED; separate product/provider/schema contract decisions OPEN; Phase 4 renderer and conditional fallback REQUIREMENTS RECORDED but PHASE-4 PLAN NOT FROZEN.
