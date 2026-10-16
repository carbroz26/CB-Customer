# 56 — Dynamic Actions: Source-Level Audit and Exact Refactor Plan (Draft)

**State:** AUDIT + PLAN DRAFT; NOT FROZEN. No Kotlin modifications authorized by this record. Branch `architecture/dynamic-runtime-v2-discussion-freeze`.

## 1. Owner clarification and semantics

A normal user event (`onClick`, `onSubmit`, etc.) maps to **one top-level action**. `request`, `navigate`, `present`, `dismiss`, `external_uri`, `state` and `sequence` are different action types. **Sequence is not implicit**: only an explicitly declared `type:"sequence"` contains `payload.actions[]`, and then multiple child actions run in order. A Request may itself return a destination from backend; that is a result of one Request, not an implicit separate Navigate action. Dismiss closes presentation (current implementation falls back to pop; semantic review below), external_uri opens a link such as Terms; plain navigate performs its specified nav operation.

This reconciles the owner's clarification with frozen doc20 §3 (seven types, sequence semantics). **Do not delete sequence or conflate one UI event with multiple implicit actions.** Any change to this is a contract change requiring new approval.

## 2. Sources inspected
- `data/dynamic/model/action/DynamicActionDto.kt`: five event hooks + typed single Action DTO, raw JsonObject payload.
- `data/dynamic/mapper/DynamicActionMapper.kt`: maps hook to `DynamicAction(type,payload,destination)`.
- `domain/model/dynamic/action/DynamicAction.kt`: one action with type, dynamic payload, optional destination.
- `feature/dynamic/action/`: `DynamicActionExecutor`, `DynamicActionHandler`, `DynamicActionResult`, `DynamicActionValueResolver`, `RequestActionHandler`, `NavigateActionHandler`, `PresentActionHandler`, `DismissActionHandler`, `ExternalUriActionHandler`, `StateActionHandler`.
- Related callers: `DynamicStore`, `DynamicContainer`, `DynamicRenderContext`, `DynamicReferenceResolver`, `DynamicFormValidator`, `DynamicRuntimeState`, `DynamicModule`, Request/Executor/Store/Resolver tests. W4 session consumer is in `app/ApplicationContent.kt`.
- Contract authority: `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md`, `12-CANONICAL-JSON-FIXED-CONTRACT.md`, `51-W4-PLAN-FREEZE-REVIEW-AND-IMPLEMENTATION-GATES.md`.

## 3. Current accurate behavior
1. Event DTO exposes a **single action per event hook**; `DynamicActionMapper` preserves that one action.
2. `DynamicActionExecutor` dispatches non-sequence types through one handler per normalized type. Only explicit `sequence` is decoded recursively from `payload.actions`.
3. Sequence applies child state changes to an in-memory sequence state, aggregates effects in order, and publishes no aggregate result if a child throws. Cancellation rethrows. It computes recursive state delta to preserve nested/dotted updates.
4. `DynamicStore` validates top-level action **only when** `payload["validate"]` is literal Boolean true; nested Request validation does not receive its own precondition.
5. `RequestActionHandler` sends a repository request, maps `responseMode:none` to no navigation and `responseMode:destination` to `Navigate` result with optional retain changes. Unknown Request mode silently defaults to Push; `NavigateActionHandler` itself has already been made strict in W4-A.
6. `DynamicReferenceResolver.resolvePath` returns `NullValue` for both missing path and explicit null. Retain extraction in Request handler uses `mapNotNull` for invalid mapping path definitions and does not reject absent paths.
7. `DynamicActionValueResolver` is mostly a thin forwarding wrapper over `DynamicReferenceResolver` with `asString` (empty fallback). Its utility as a second owner must be audited with all call sites before removal.
8. `DynamicFormValidator` walks body components/sections/groups and validates input required/pattern; header/footer input handling, validation scopes, empty/unrecognized bindings need deliberate contract review.
9. App-level effect delivery was introduced in W4; action executor must not reimplement navigation and app consumer must not infer wire payloads. Current `Dismiss` effect handler falls back to `pop` if no modal, which may not match owner's meaning *dismiss modal only*; needs decision before editing.
10. `DynamicStore` action errors currently render full-screen Failure with exception text, losing usable screen UI in presentation though keeping screen data. This violates frozen recoverable in-place behavior.

## 4. SRP / unnecessary code assessment
- **KEEP** `DynamicAction` domain, action DTO/event mapping, `DynamicActionHandler` registration, concrete handler per actual action behavior, `DynamicActionExecutor` and `DynamicActionResult`. Distinct handlers have real responsibilities and are not merely wrappers.
- **KEEP** Sequence as executor-owned execution semantics, not `SequenceActionHandler` plus duplicate logic. No Sequence-specific navigation system.
- **REVIEW TO REMOVE** `DynamicActionValueResolver` if replacing forwarding calls by the existing reference resolver can preserve module visibility, tests and typed semantics. Do not delete before complete call-site/test reconciliation; `DynamicReferenceResolver` is internal and in same feature module.
- **REVIEW** `DynamicActionResult` subclasses versus `stateChanges + effects`; keep expressiveness, avoid maintaining two unrelated representations of same operation. Replace only when evidence demonstrates simpler complete model without lost semantics.
- **NO NEW** `ActionManager`, `SequenceManager`, `NavigationManager`, `RequestManager`, second Store or ViewModel.
- **DO NOT SIMPLIFY BY LOSING** typed $binding/$context/$state/$literal, nested state path patches, cancellation, seven supported action types or all five event hooks.

## 5. Exact proposed source-file plan
| File | Proposed operation |
|---|---|
| `data/.../model/action/DynamicActionDto.kt` | KEEP five hooks and one action per event; strict tests for map fidelity and explicit sequence payload |
| `data/.../mapper/DynamicActionMapper.kt` | KEEP single mapper; add tests preserving type/destination and nested payload |
| `domain/.../action/DynamicAction.kt` | KEEP; no action hierarchy explosion without concrete demonstrated type safety gain |
| `feature/dynamic/action/DynamicActionHandler.kt` | KEEP single handler contract, narrow precondition context only if needed |
| `feature/dynamic/action/DynamicActionExecutor.kt` | EDIT narrowly: ensure per-child request validation and fail-fast/cancellation with no repeated state/effects; preserve one sequence execution owner; simplify state delta only if regressions prove equivalence |
| `feature/dynamic/action/RequestActionHandler.kt` | EDIT: strict mode/responseMode/missing retain semantics and validate before repository.fetch; avoid duplicating precondition logic in Store |
| `feature/dynamic/action/NavigateActionHandler.kt` | KEEP W4-A strict mode, reuse behavior only where same semantics proven |
| `feature/dynamic/action/PresentActionHandler.kt` | KEEP |
| `feature/dynamic/action/DismissActionHandler.kt` | KEEP handler; investigate app effect semantics (modal only vs pop fallback) |
| `feature/dynamic/action/ExternalUriActionHandler.kt` | KEEP; validate nonblank URI and scheme policy at proper host boundary, avoid token leak |
| `feature/dynamic/action/StateActionHandler.kt` | KEEP; consider strict key/path validation and references only if contract-backed |
| `feature/dynamic/action/DynamicActionResult.kt` | KEEP typed effects; optimize only if duplicate representations verified |
| `feature/dynamic/action/DynamicActionValueResolver.kt` | REMOVE candidate (thin forwarding) only after reconciling all references/tests |
| `feature/dynamic/runtime/DynamicReferenceResolver.kt` | EDIT only if explicit missing/null retain path resolution needs typed presence result; no second resolver |
| `feature/dynamic/runtime/DynamicFormValidator.kt` | KEEP one validator; verify one traversal includes all relevant input paths including new slot if needed |
| `feature/dynamic/store/DynamicStore.kt` | EDIT remove top-level-only request prevalidation in favor of every-Request precondition, preserve in-place screen on recoverable action error and session state-first effect delivery |
| `feature/dynamic/runtime/DynamicRuntimeState.kt` | KEEP session state; no duplicate accumulation |
| `app/ApplicationContent.kt` | EDIT only for confirmed modal dismiss semantics and effect failure presentation; preserve W4 session consumer |
| `feature/dynamic/DynamicModule.kt` | KEEP one DI registry, update constructor wiring without shadow handlers |
| `feature/dynamic/.../action/*Test.kt`, `store/DynamicStoreTest.kt`, `data/.../mapper/DynamicResponseMapperTest.kt` | EXTEND targeted regression cases; preserve existing tests |

## 6. Decision questions / freeze gates (not silently fixed)
A. **Dismiss:** Owner says dismiss closes dialog. Current `DynamicActionEffect.Dismiss` does `dismissPresentation() || pop()`. Proposed for owner: `dismiss` must target only an active presentation; back-stack pop remains `navigate(mode:pop/back)` or system Back. This is a behavioral contract change requiring explicit sign-off.
B. **Request validation:** Proposed: `validate:true` belongs to each Request, including nested sequence; for forms, use current screen & current bindings; action's resolved or literal Boolean? Doc20 requires validation before every Request (regardless placement); clarify `validate:false` as deliberate opt-out vs always validating Request. Existing canonical Login/OTP says Request validate available.
C. **Sequence state vs bindings:** Sequence state patches are visible to later children, while entry-local bindings are unchanged until explicit input change. Confirm no implicit copying from $state to $binding.
D. **Retain:** Missing required mapping path should fail while explicit JSON null is allowed; define whether entire `retain` mappings are required or whether optional mapping syntax exists (none currently evidenced).
E. **Response:** Preserve `responseMode: none/destination` behavior as confirmed by tests; unsupported provided values must fail, and no dummy destination.
F. **Top-level `back` alias:** Frozen W4 preserves compatibility pending evidence. Keep pending owner-approved removal after source/backend contract proof.

## 7. Suggested implementation/testing waves AFTER freeze
- **Actions-A:** freeze decisions A-F after backend-fixture and all-consumer reconciliation; add failing mapper/request/sequence/store tests first.
- **Actions-B:** request validation ownership and nested sequence validation before side effects. Regression: invalid form must prevent repository.fetch even if nested.
- **Actions-C:** missing vs explicit null retain paths, responseMode/mode validation, request failure preservation.
- **Actions-D:** precise modal dismiss/present/back semantics without breaking W4; sequence fail-fast and ordered state/effects after navigation.
- **Actions-E:** remove only proven duplicate resolver/wrapper, update every caller and tests in one coherent diff; full module/package reconciliation.
- **Actions-F:** JVM `:data:jvmTest :domain:jvmTest :navigation:jvmTest :feature:dynamic:jvmTest`, Web webpack, `git diff --check`, then Android/Web smoke and deferred iOS tests. No current new tests claimed passed.

## 8. Stop rules
No Kotlin edits until decisions are accepted and separate implementation authorization. If frozen W4 or V2 behavior conflicts with owner's action semantics (notably dismiss), document explicit contract revision before changing code. No unused abstractions, broad rewrites, backend modifications, temporary branch, or silent compatibility deletion.
