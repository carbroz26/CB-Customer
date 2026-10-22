# B5 / L-10 — Generic ListSection Empty-Section Reference: PLAN FROZEN

**Owner decision:** APPROVED — B5 PLAN FROZEN
**Date:** 2026-10-22
**Status:** PLAN FROZEN; implementation NOT STARTED and NOT AUTHORIZED by this freeze
**Repository:** `carbroz26/CB-Partner`
**Only authorized frontend branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Plan reviewed against production source at:** `edcfbfe821aa164c3fbf4936f6f42e914c9b90af`
**Draft adopted without scope expansion:** [114 — B5 L-10 Source Audit & Exact Plan Draft](114-B5-L10-EMPTY-SECTION-REFERENCE-SOURCE-AUDIT-AND-EXACT-PLAN-DRAFT.md). The exact file manifest, mapping matrix and regressions in Document 114 are incorporated into this freeze. For B5, this file is the owner's freeze approval; the `DRAFT` filename in Document 114 denotes its review history, not an invitation to change its frozen scope.

## 1. Frozen contract

- `ListSection.properties.emptySectionId` is an optional typed string reference to a **sibling StackSection** in the same parent **StackComponent.sections** collection.
- The reference, when present, must be an explicitly supplied JSON string and nonblank; it must match exactly one `StackSection` sibling by ID. Missing, ambiguous, cross-component/cross-scope, self-references and references to any `ListSection` must reject deterministically.
- Legacy `ListSection.emptyGroup` is obsolete. Its prior embedded Group must not be mapped or silently ignored, including under `Json { ignoreUnknownKeys = true }`; reject the old top-level field. Reject misplaced top-level `emptySectionId`. Only the new `properties.emptySectionId` location is valid.
- The referenced StackSection remains an ordinary defined Section in the typed component tree **for B5 mapping only**. It is reserved conceptually for fallback and must not render as a normal sibling *when the Phase-4 ListSection renderer and fallback-consumption logic are implemented*. **B5 does not render ListSection or make fallback conditional.**
- The reference target may be declared before or after the ListSection in the same sibling list. Never resolve references globally across Templates, Components, Headers, Footers or Presentation boundaries.
- No new `ListElement`, additional UI category, `emptyState`, duplicate visibility conditions, special country-picker logic or frontend visual defaults.
- Existing `items xor source`, row Group, filter, selection, separator and typed layout contracts stay unchanged. Multiple ListSections referencing one sibling are not prohibited by this B5 contract; no unapproved exclusivity rule is introduced.

## 2. Frozen exact file manifest

Only the following B5 changes may be made after a *separate implementation authorization*:

1. **MODIFY** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/model/section/ListSectionDto.kt` — remove `emptyGroup` DTO field.
2. **MODIFY** `domain/src/commonMain/kotlin/com/carbroz/cbpartner/domain/model/dynamic/section/ListSection.kt` — replace `emptyGroup` with `emptySectionId: String?`.
3. **MODIFY** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/SectionMapper.kt` — strict new property decoding and explicit obsolete-field rejection.
4. **MODIFY** `data/src/commonMain/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/ComponentMapper.kt` — validate references against mapped siblings exactly once.
5. **MODIFY** `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/DynamicContractFixtures.kt` — preserve old fallback Group contents under a new sibling StackSection.
6. **MODIFY** `data/src/commonTest/kotlin/com/carbroz/cbpartner/data/dynamic/mapper/DynamicResponseMapperTest.kt` — update mapping assertions and add valid/invalid/legacy/scope regression cases.
7. **MODIFY** `docs/architecture-proposal/dynamic-runtime-v2/112-LOGIN-COUNTRY-PICKER-PHASE1-3-CONSOLIDATED-FREEZE-AND-AUDIT-HANDOFF.md` — reference revised target JSON; keep historical Appendix for audit.
8. **ADD** `docs/architecture-proposal/dynamic-runtime-v2/partner-login-country-picker-current-target.json` — derive **only** the approved L-10 transformation from the exact 2026-10-21 frozen JSON artifact, retaining every unrelated value and visual detail. Verify the original artifact is available and identical to the approved source before generating this file; do not synthesize missing payload fields.

**KEEP:** `feature/dynamic` Section/Component renderers and registry, `DynamicRenderContext`, DynamicStore, actions, validators, navigation, Theme, backend, network, templates and all other batches. Do not introduce a ListSection renderer in B5.

## 3. Verification and acceptance gates

- Negative tests must reject malformed `emptySectionId` including explicit `null`, wrong JSON primitive/type, blank string; reject legacy `emptyGroup` even with ignore-unknown decoding; and reject invalid/ambiguous sibling graphs.
- Positive tests must cover optional reference, before/after sibling ordering, correct typed `StackSection`/Group contents, and isolated validation in Screen Components and Presentation Components.
- Existing catalog, item/source, filter, selection, row and separator mapping tests must keep passing.
- Verify with `:data:jvmTest :domain:jvmTest :feature:dynamic:jvmTest :app:jvmTest :feature:dynamic:compileAndroidMain :webApp:wasmJsBrowserDevelopmentWebpack`, followed by `git diff <pre-B5-HEAD> HEAD --check` and a scoped changed-file review.
- **B5 VERIFIED** means mapping/model compatibility tests and compilations pass. It does **not** mean the ListSection or its referenced fallback has been displayed. End-to-end conditional rendering remains a Phase-4 workstream, separately discussed, frozen and authorized.

## 4. Change-control gate

**Plan freeze is not code authorization.** No Kotlin source, test fixtures, target JSON, existing frozen contract or backend file is changed by this freeze event. Next transition must be the owner's explicit B5 implementation authorization. If the source baseline, target frozen JSON, or a required contract detail conflicts with this plan, stop and surface the exact conflict for owner reconciliation; do not silently extend scope.

**Owner-approved phase boundary:** B5 = generic L-10 reference contract; Phase 4 = collection/catalog resolution, `$item`, filtering, selection, list rows, lazy presentation and conditional empty-section rendering.
