# 57 — Action / Sequence Intent Correction (Owner Clarification)

**Status:** OWNER CORRECTION RECORDED; ACTION CONTRACT IN REVIEW — NOT FROZEN. **No Kotlin edits.** Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## Owner's intent takes precedence over previous agent interpretation

The owner explicitly disputes the claim that `sequence` was originally intended as a general-purpose action that executes arbitrary multiple child actions (e.g. Navigate then ExternalUri) and says this design was **not planned by the owner**. Do not attribute that assertion to the owner or repeat it as settled architecture. Owner recalls that a mechanism was wanted for **cross-element state changes**, such as tapping Resend OTP to start/reset a countdown and control Resend enabled state, or tapping one element to show/hide another. The owner is uncertain whether `sequence` was the intended name; **do not assert that Sequence definitively means visibility/timer either** until the contract is explicitly decided.

Earlier owner requirements (2026-10-03) included visibility/enabled, countdown/timer, parent-child and cross-element changes through shared Dynamic State. A prior assistant had proposed multiple child actions as the meaning of Sequence. Those are different sources of authority. The present owner's clarification overrides the agent's interpretation as a design requirement.

## What current source actually does (not necessarily desired)

- `DynamicActionsDto` maps each of five UI events to one `DynamicActionDto`.
- `DynamicActionExecutor.executeInternal` special-cases `type == "sequence"` and executes all `payload.actions` recursively, including Navigate/Present/ExternalUri/Request; it aggregates state changes and effects in order.
- `DynamicStore` commits resulting state and session effect batches.
- `StateActionHandler` changes a path in session state.
- No source evidence in the inspected action package establishes a dedicated countdown/timer or show/hide control contract. Having `state` does not itself implement a timer.
- Frozen V2 doc20, W4 docs51–55, and Actions Draft56 assumed multi-child Sequence semantics. They must now be marked **under contract review** wherever this assumption matters; W4 infrastructure and verified compilation remain factual, but Sequence acceptance tests do not establish agreement with owner intent.

## Correct architectural principles for redesign
1. An event hook refers to one top-level action; unrelated Navigate/Dismiss/ExternalUri should not be automatically chained by a click.
2. A cross-element change is performed through Store-owned dynamic state; Element renderers observe it and never directly mutate sibling elements.
3. A timer requires explicit lifecycle and time semantics; it must not be simulated through a naked Sequence of unrelated actions. Define starting, countdown, completion, screen disposal and subsequent tap behavior.
4. Resend OTP request and countdown should have a separately specified relationship (API outcome and retries); do not hardcode authentication/OTP business logic into generic dynamic runtime.
5. Decide whether `sequence` should be **removed**, **redefined** (with contract-approved meaning), or **kept for an evidenced separate need**. None of these is approved by this note. Do not invent a `SequenceStateAction`, Timer manager, or extra Action hierarchy yet.
6. Keep core invariants: one navigation authority; typed reference namespaces; explicit UI state and binding ownership; strict SRP/Clean Architecture; no unused duplicate modules.

## Exact impact inventory for contract revision planning
- `docs/architecture-proposal/dynamic-runtime-v2/20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md`: Sequence multiple-child claims must be reopened **in this scope only**; no silent rewrite of other frozen decisions.
- `docs/architecture-proposal/dynamic-runtime-v2/51-W4-PLAN-FREEZE-REVIEW-AND-IMPLEMENTATION-GATES.md`: Sequence Navigate-first acceptance scenario is now a **technical regression of existing code, not owner-approved intended user behavior**.
- `docs/architecture-proposal/dynamic-runtime-v2/56-ACTIONS-SOURCE-AUDIT-AND-REFACTOR-PLAN-DRAFT.md`: `sequence` intent is disputed; the sentence portraying it as settled must not be treated as valid plan authority. Superseded in this subject by doc57.
- `domain/.../action/DynamicAction.kt`, `data/.../model/action/DynamicActionDto.kt`, `data/.../mapper/DynamicActionMapper.kt`: audit required wire and Domain representations once action semantics are decided.
- `feature/dynamic/action/DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `StateActionHandler.kt`, `RequestActionHandler.kt` and `DynamicActionValueResolver.kt`: review and then remove only obsolete Sequence recursion/state delta/effect list machinery after contract decision, with all callers/tests updated together.
- `feature/dynamic/store/DynamicStore.kt`, `runtime/DynamicRuntimeState.kt`, `runtime/DynamicReferenceResolver.kt`, `runtime/DynamicFormValidator.kt`, `app/ApplicationContent.kt`: clarify generic UI state visibility/enabled and timing ownership; preserve existing session navigation/effect capability only if needed for real action results.
- `feature/dynamic/.../action/DynamicActionExecutorTest.kt`, `store/DynamicStoreTest.kt`: existing nested-Sequence and Navigate-first tests prove prior implementation only, not owner authorization. Update/remove mismatched tests together with frozen implementation changes.

## Decision-led plan — no implementation yet
A. Source/fixture inventory: find any actual backend payloads using `sequence`, `state`, `visible`, `enabled`, `timer`, `countdown`; distinguish frontend-only tests from real API contract.
B. Specify generic cross-element dynamic state: backend-defined identifiers/paths, conditional visibility, enabled status, timer state and lifecycle. Keep presentation reactive; no element-to-element references.
C. Decide exact action taxonomy and JSON schema with owner: one event/action, whether `sequence` is needed at all and what it means, and how a request outcome affects timer state.
D. Revise frozen docs by explicit owner sign-off, including any W4 acceptance tests dependent on Sequence semantics.
E. Produce minimal exact file-by-file remove/edit/keep manifest and tests; avoid retaining unused generic Sequence frameworks.
F. Only after PLAN_FROZEN + separate implementation authorization: update production code and regression fixtures in coherent slices, run JVM/Web and supported platform UI tests.

## Stop rule
**No Kotlin actions/Sequence refactoring until revised contract is approved.** No backend changes. This document corrects provenance and defines audit scope, not a final substitute definition of Sequence.
