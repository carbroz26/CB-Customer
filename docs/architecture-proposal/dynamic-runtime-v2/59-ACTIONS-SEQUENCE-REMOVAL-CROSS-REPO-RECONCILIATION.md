# 59 — Actions Sequence Removal: Frontend ↔ Backend Source Reconciliation

**Status:** READ-ONLY CROSS-REPO AUDIT / PLAN RECONCILIATION; NOT PLAN_FROZEN. No Kotlin or backend source modifications. Action cleanup draft58 remains draft.

## Explicit owner decision
The arbitrary chained multi-child `sequence` implementation is **wrong and is to be completely removed**, including obsolete tests and supporting helpers. Do not retain it as compatibility dead code. Cross-element communication/timers/visibility are a future separate backend/frontend contract, not an excuse to preserve recursive sequence. Each ordinary event triggers one action; six intended independent action types currently are `request`, `navigate`, `present`, `dismiss`, `external_uri`, `state`. A Request can return a destination and one navigation effect; it is not invocation of a Navigate handler. No Kotlin implementation authorized until conflict resolution and frozen change plan.

## Source facts on frontend branch `architecture/dynamic-runtime-v2-discussion-freeze`
- `feature/dynamic/action/DynamicActionExecutor.kt`: `type=sequence` special-case; nested decoding, recursive `executeInternal`, depth 32, mutable state projection, ordered effect list, nested `stateDelta` and local `toDestination`. Remove these after freeze, keep ordinary handler lookup and cancellation.
- `feature/dynamic/action/DynamicActionResult.kt`: `Sequence` subclass plus list-valued effects; decide whether to simplify to 0..1 effect after all result producers/consumers reviewed.
- `feature/dynamic/.../DynamicActionExecutorTest.kt`: numerous tests asserting nested/recursive sequence, cancellation within sequence, state merging, effect ordering. Replace with single-action dispatch, duplicate registration, unsupported type and cancellation tests; preserve legitimate non-sequence coverage.
- `feature/dynamic/store/DynamicStoreTest.kt`: tests `sequenceEffectsAreDeliveredInOrder`, `acceptedEffectBatchOutlivesSourceStoreDisposal`, `navigateFirstBatchKeepsLaterEffectAfterSourceStoreCloses` are tied to invalid semantics; replace with Request->destination, state-before-effect, source-disposal accepted single effect and failure tests. Preserve `recreatedStoreRestoresBindingsFromSameNavigationEntry` and typed Request coverage.
- `feature/dynamic/store/DynamicStore.kt` + `app/ApplicationContent.kt`: W4 session effect batch lists now exist largely for sequence. Preserve single-session delivery and source-disposal independence, simplify cardinality/queue ONLY after verified 0..1 result mapping. Preserve safe navigation/URI and error reporting.
- `data/.../DynamicActionDto.kt` and mapper / `domain/.../DynamicAction.kt`: generic one-action-per-event schema does not itself require sequence-specific DTO/domain subclasses. Preserve five event hooks.
- Existing frontend `StateActionHandler.kt` implements `payload.key` + `payload.value` state path; backend `stateAction` currently emits `targetId`, `operation`, `property`, `value`, which is **a separate wire mismatch**, not something to solve via sequence removal.

## Backend source audit — `carbroz26/CB-Backend` branch `development` (READ ONLY)
- `sdui/engine/src/core/Action.ts` has `sequenceAction(actions)` validating nonempty array and returning `{type:'sequence',payload:{actions}}`; `action.sequence` is exported in `action` helper.
- `sdui/engine/tests/Action.spec.ts` explicitly asserts sequence authoring for two `state` actions, and rejects empty sequence. Both must be revised in **separately authorized backend migration**.
- `sdui/engine/src/screens/partner/PartnerLoginScreen.ts` directly authors Request and External URI actions; no direct sequence call observed in this file.
- `sdui/engine/src/screens/partner/PartnerOtpScreen.ts` directly authors Navigate, Request and External URI actions; no direct sequence call observed in this file.
- `sdui/PARTNER-AUTH-SDUI-CONTRACT.md` lists `sequence` as one of generic actions. Its documented contract must be explicitly superseded; do not silently rewrite past freeze.
- Backend `Action.ts` state authoring is target-relative; frontend State handler is session path-based. This is a **verified mismatch requiring separate precise scope/decision** rather than guessing a replacement contract.
- The inspected two production screen source files lack sequence authoring, but **this is not proof that no registry/draft/database API response can contain it**. Before removal, search current screen registry, persisted content and actual API output/fixtures for sequence; backend DB runtime not inspected.

## Frozen-contract collision
- Frontend doc20 §1, §3 and §6 explicitly mandate all seven types, nested sequence execution and seven-action testing.
- Frontend doc51 D3 defines ordered multi-effect batches and failure behavior for sequence.
- Earlier draft56 erroneously treated sequence behavior as owner-approved. Doc57 corrected the authority problem; doc58 records removal decision.
- Removal requires **new explicit supersession for sequence-only sections**, not retroactive claim that doc20 or doc51 never existed. Other W4 decisions (entry identity, URI/nav single authority) remain valid.

## Precise coordinated work plan, subject to owner freeze
1. Frontend audit references in remaining source/fixtures and backend registry/production JSON. Distinguish actual emitted payloads from merely supported builder API.
2. Author a focused **sequence-only contract supersession** (Frontend doc20/doc51; Backend `Action.ts`/doc/test migration agreement), with no cross-element contract invented.
3. Introduce/adjust non-sequence regressions: one UI event maps to one action; Request destination result; unsupported `sequence` not executed; state update and optional single effect; no effects on failed/cancelled actions; present/dismiss/nav errors safe.
4. Remove frontend special sequence code, related state projection and obsolete tests. Remove `Sequence` result and simplify effects only when full producer/consumer cardinality proven.
5. In separately authorized backend step, remove `sequenceAction`, `action.sequence`, obsolete authoring tests and documentation assertions, and verify emitted screens. No backend code edit during frontend planning.
6. Reconcile separately discovered **state action wire mismatch** (`targetId/operation/property` versus `key/value`) before claiming generic action compatibility; owner wants future cross-element planning deferred, so do not invent fix now.
7. Validate full JVM (`:domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest`), Web webpack, `git diff --check` and targeted Android/Web smoke. Keep commits narrow, maintain SRP and no ViewModel/second router.

## Gate
**NO IMPLEMENTATION / NOT FROZEN.** Return focused owner review for sequence-only frozen-contract override and backend contract coordination before changing Kotlin. No build verification run here.


## 2026-10-16 owner revision — backend migration deferred; inert Sequence retained
**SUPERSEDES earlier removal-of-action-type/unsupported-sequence instructions in this document.** The owner now explicitly directs: do **not migrate or modify the backend**; keep `sequence` as an individually registered action type, but it performs **no child actions and no application logic**. On click the current frontend returns `DynamicActionResult.Completed` from `SequenceActionHandler`; it does not parse or execute `payload.actions`, navigate, change state, call APIs, or open links. A toast was illustrative only; no new notification UI is authorized or necessary. Do not invent timer, visibility or cross-element behavior. A future jointly approved backend/frontend contract will define that independently.

Backend `CB-Backend/development` and existing backend `sequenceAction` authoring are **unchanged**; migration removed from the current implementation scope and explicitly marked **DEFERRED**. Previously frozen doc20/doc51 child-execution semantics remain superseded; the action type itself is not removed. Existing generic DTO/domain payload continues to deserialize legacy JSON, but child payload has no effect. Subsequent action SRP changes beyond this limited cleanup require their own freeze.
