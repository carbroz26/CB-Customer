# 60 — Action Contract Revision: Remove Multi-Action Sequence (Frozen Scope)

**Status: SEQUENCE-REMOVAL CONTRACT FROZEN — separate action-refactor implementation stages pending source/test gates.**
**Owner basis:** explicit owner instruction that `sequence` never represented arbitrary chained child actions and that the implementation and its unused infrastructure must be removed completely. This document supersedes only the listed clauses of earlier freezes. Frontend target branch `architecture/dynamic-runtime-v2-discussion-freeze`. Backend `carbroz26/CB-Backend` `development` is read-only during frontend work.

## Authoritative action semantics
1. One event hook (`onClick`, `onLongClick`, `onValueChange`, `onFocus`, `onSubmit`) specifies one action. There is **no** implicit action list, recursive action dispatcher, or multi-action sequence.
2. Supported existing independently handled types remain **request, navigate, present, dismiss, external_uri, state**. No `sequence` runtime type. Unknown type including `sequence` must fail safely; never run nested actions.
3. Request owns HTTP request construction/execution and parsing its API response. A destination returned by the backend can be represented as one navigation effect of the same Request result, not a second invoked Navigate handler.
4. Navigation actions declare navigation intent; only the single session-level navigation authority mutates stack/presentation. External URI action requests a platform effect, not direct browser navigation in handler.
5. State action remains a distinct handler. The backend `targetId/operation/property` vs frontend `key/value` wire mismatch is **NOT RESOLVED** here. No cross-element/timer/visibility semantics designed or implemented under this freeze.
6. Preserve typed reference resolution, entry/session boundaries, cancellation propagation, fail-safe errors and one session-owned effect consumer. If action result cardinality is at most one effect, simplify list/batch representation without compromising navigation-origin entry disposal safety.

## Supersession record
- `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md`: supersede only requirements to retain **seven** action types, Sequence child execution/order/fail-fast, nested Sequence Request validation, Sequence-specific tests and SequenceActionHandler. Replace with six types and ordinary one-action dispatch. All unrelated frozen requirements remain.
- `51-W4-PLAN-FREEZE-REVIEW-AND-IMPLEMENTATION-GATES.md`: supersede D3's Sequence-specific multi-effect transaction and its navigate-first Sequence regression. Retain single-session effect delivery, state-before-effect, visible safe error on rejected navigation, entry identity and lifetime. The legitimate state+effect combination for a Request still commits state first.
- `56-ACTIONS-SOURCE-AUDIT-AND-REFACTOR-PLAN-DRAFT.md`: incorrect assumption that Sequence is intended functionality is rejected.
- `57-ACTIONS-SEQUENCE-INTENT-CORRECTION-AND-CONTRACT-REVIEW.md`, `58-ACTIONS-ARCHITECTURE-CLEANUP-AND-SEQUENCE-REMOVAL-PLAN-DRAFT.md`, `59-ACTIONS-SEQUENCE-REMOVAL-CROSS-REPO-RECONCILIATION.md`: context and findings; this document controls **sequence removal only**. No historical rewrite.

## Source-based backend migration constraint
Backend `development:sdui/engine/src/core/Action.ts` still exports `sequenceAction` and `action.sequence`; `sdui/engine/tests/Action.spec.ts` asserts them; backend auth SDUI contract lists sequence. The reviewed PartnerLoginScreen and PartnerOtpScreen do **not** call `action.sequence` directly; this does not prove persisted registry payloads lack it. Backend replacement/removal requires a separate authorized coordinated backend migration, and deployment compatibility verification. This frontend freeze alone does not authorize backend edits.

## Minimal file and test plan (do not redesign)
A. `feature/dynamic/action/DynamicActionExecutor.kt`: delete special-cased recursive Sequence, depth guard, decode/conversion helpers, sequence state delta; retain simple registered handler dispatch and cancellation.
B. `feature/dynamic/action/DynamicActionResult.kt`: remove `Sequence`; verify single-action results before replacing `effects: List` and `Channel<List<...>>` with zero-or-one effect.
C. `feature/dynamic/action/DynamicActionExecutorTest.kt`: replace all obsolete Sequence expectations by ordinary dispatch, unsupported action, duplicate handler and cancellation cases. Tests must pass at the same commit as corresponding runtime edits.
D. `feature/dynamic/store/DynamicStoreTest.kt`: replace three Sequence-effect tests with accepted standalone effect, state-commit ordering, source entry disposal and failure/cancellation regressions. Preserve valid Request and entry binding tests.
E. `feature/dynamic/store/DynamicStore.kt` and `app/ApplicationContent.kt`: simplify single-effect handoff only if tests demonstrate one-effect cardinality and retain session-safe delivery.
F. `domain/.../DynamicAction.kt`, `data/.../DynamicActionDto.kt`, `data/.../DynamicActionMapper.kt`: retain generic one-action schema; no Sequence-specific wire adapter. Add focused tests only as required.
G. `RequestActionHandler.kt`, `StateActionHandler.kt`, `DismissActionHandler.kt`, `DynamicActionValueResolver.kt`: additional validation, modal-only dismissal, backend state-wire reconciliation, or removal of forwarding wrapper are **separate unapproved subcontracts** until precisely scoped; do not bundle into initial Sequence removal.
H. Backend `Action.ts`, `Action.spec.ts` and docs migration: scoped follow-up in `CB-Backend/development` only with coordination and approval.

## Testing and review gates
For **each coherent source commit**: inspect diff, source-consumer reconciliation, relevant `:feature:dynamic:jvmTest` and `:navigation:jvmTest`. Final frontend gates: `:domain:jvmTest :data:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`, then Android/Web smoke; no iOS simulator tests on Windows.
Stop if backend production/registry fixtures contain active sequence, if the source requires unplanned action types or wire contract migrations, or if result simplification would discard valid Request state+navigation output. Report conflict; do not silently retain dead compatibility, invent successor abstractions, or break source.

**Scope clarification:** This is a contract freeze for sequence removal, not permission to silently implement the other proposals in drafts 58/59. Remaining actions require separate decisions/freeze. No Kotlin changes are made in this freeze document.


## 2026-10-16 owner revision — backend migration deferred; inert Sequence retained
**SUPERSEDES earlier removal-of-action-type/unsupported-sequence instructions in this document.** The owner now explicitly directs: do **not migrate or modify the backend**; keep `sequence` as an individually registered action type, but it performs **no child actions and no application logic**. On click the current frontend returns `DynamicActionResult.Completed` from `SequenceActionHandler`; it does not parse or execute `payload.actions`, navigate, change state, call APIs, or open links. A toast was illustrative only; no new notification UI is authorized or necessary. Do not invent timer, visibility or cross-element behavior. A future jointly approved backend/frontend contract will define that independently.

Backend `CB-Backend/development` and existing backend `sequenceAction` authoring are **unchanged**; migration removed from the current implementation scope and explicitly marked **DEFERRED**. Previously frozen doc20/doc51 child-execution semantics remain superseded; the action type itself is not removed. Existing generic DTO/domain payload continues to deserialize legacy JSON, but child payload has no effect. Subsequent action SRP changes beyond this limited cleanup require their own freeze.
