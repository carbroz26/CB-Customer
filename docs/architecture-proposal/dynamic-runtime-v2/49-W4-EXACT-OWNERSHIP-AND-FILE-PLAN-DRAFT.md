# 49 — W4 Exact Ownership and File Plan (Draft for Freeze Review)

**State:** PLAN DRAFT / NOT FROZEN / NO KOTLIN EDITS. Branch `architecture/dynamic-runtime-v2-discussion-freeze`. Source inspected against frozen `20-PLAN-FROZEN-V2-IMPLEMENTATION-CONTRACT.md` §§1,3,5,6 and audit 48. English documentation; Hindi chat explanation.

## 1. Strict ownership
| Existing owner | Responsibility | Prohibited |
|---|---|---|
| `DynamicActionExecutor` | Compute ordered results of actions/Sequence; fail-fast and cancellation; no navigation/UI effects executed | Router, Compose, source-entry lifecycle handling |
| `DynamicStore` (entry-scoped) | Process intents, validation, entry bindings, action execution, commit session state **once** and hand off **whole ordered batch** after successful execution | Own lifetime of cross-entry effect queue; native URI opening |
| `DynamicNavigationState` (session-scoped) | Sole back stack, distinct modal slot, entry creation/identity, mutation of push/replace/pop/present/dismiss | Reparse action payloads, own business-level action semantics |
| `ApplicationContent.DynamicContent` (session composition) | Create and retain one session boundary and one long-lived effect consumer; arrange app host and URI adapter | Second Nav authority, global singleton, per-screen collector |
| `DynamicContainer` (entry UI) | Compose per-entry screen and route intents, close screen-specific work on actual entry removal | Session-scoped effect consumption/queue disposal |
| `DynamicRuntimeState` | Session-retained data independent of back-stack entry | Per-entry input bindings |
| `DynamicNavigationEntry` / small entry state | Unique visit identity and storage lifetime for bindings (plus only necessary restored screen data), freed on final removal | Persistent/global process manager |

## 2. Proposed effect handoff: ordered, session-owned
1. Executor returns `DynamicActionResult` with ordered `effects`; failures/cancellation produce no committed new effects.
2. Store applies `result.stateChanges` exactly once *before* publishing any effect.
3. Store hands off the **complete list as one immutable batch** to a session-owned sink (not per-entry Channel). Session scope owns its queue and one consumer; feature-layer API should avoid importing Compose or platform dependencies.
4. Session consumer handles ordered Navigate/Present/Dismiss/ExternalUri using exactly one `DynamicNavigationState` and existing `LocalUriHandler` from the app/UI boundary. Navigation mutation cannot cancel this consumer when source entry is removed.
5. On rejected transition, report a typed/controlled failure or explicit operation outcome; no silent ignored `Boolean`, no raw server exception text. Define deterministic semantics for later effects after one application failure before implementation.
6. Session cancellation closes queue/consumer. Do not replay a batch after disposal, duplicate effects, or create two collectors.
7. **Source-verified risk only**: Current `DynamicStore` owns `Channel(BUFFERED)`, while `DynamicContainer` consumes in `LaunchedEffect(store)` and disposes per entry. This must be proven by a regression with navigate-first multi-effect sequence.

## 3. Exact file-level plan — proposed operations
| File | Operation | Reason |
|---|---|---|
| `feature/dynamic/action/DynamicActionExecutor.kt` | KEEP; add/adjust tests only unless regression demands narrow fix | Preserve sequence accumulation/state delta/cancellation; W6 later handles deeper action changes |
| `feature/dynamic/action/DynamicActionResult.kt` | KEEP types; narrowly revise error/outcome signatures only if demanded by tests | Already represents ordered effects |
| `feature/dynamic/store/DynamicStore.kt` | EDIT: replace per-entry Channel/send loop with one session-bound effect batch sink; preserve entry intent/validation and state-first commit; sanitize failures | Remove lifetime mismatch and stale collector ownership |
| `feature/dynamic/DynamicContainer.kt` | EDIT: remove entry-scoped effect collector and `applyNavigation`; keep per-entry loading/render/actions | No longer consumes effects after entry disposal |
| `app/ApplicationContent.kt` | EDIT: create session effect owner and single collector in `DynamicContent`, wire it to Store factory and existing nav+URI boundary; dispose on session end | Stable ownership above individual screens |
| `navigation/DynamicNavigationState.kt` | EDIT minimal: retain one stack/modal; expose checked transitions and explicit entry lifetime/removal hooks only as evidence demands | Single navigation authority; reject invalid transitions |
| `navigation/DynamicNavigationEntry.kt` | EDIT minimal: add distinct `entryId` and entry-bound persistence holder as approved after Nav3 lifecycle confirmation | Frozen identity and input retention |
| `navigation/DynamicNavigationHost.kt` | KEEP or small edit only for correct entry lifecycle/Back/Dialog semantics | Preserve Nav3+Dialog |
| `feature/dynamic/runtime/DynamicRuntimeState.kt` | KEEP | Existing session-retained Map |
| `feature/dynamic/action/NavigateActionHandler.kt` | EDIT: parse known modes; absent mode only if canonical default Push; reject unknown; audit legacy `back` support | No unknown->Push |
| `feature/dynamic/action/PresentActionHandler.kt`, `DismissActionHandler.kt` | KEEP unless concrete error test fails | Distinct modal actions already implemented |
| `navigation/src/commonTest/.../DynamicNavigationStateTest.kt` | EXTEND | Unique identity, rejected transitions, modal-back/root, replace and removal |
| `feature/dynamic/src/commonTest/.../store/DynamicStoreTest.kt` | EXTEND | State-before-batch, failed/cancelled batch absent, source disposal and binding persistence |
| `feature/dynamic/src/commonTest/.../action/DynamicActionExecutorTest.kt` | EXTEND | Sequence navigate-first multi-effect order and fail fast |
| `feature/dynamic/src/commonTest/.../action/` Navigate tests | ADD focused test only if no adequate suite | Unknown/missing/known mode contract |
| Session effect implementation | **Prefer small app-/feature-session-scoped file only if constructor/wiring cannot stay clear in existing sources** | No singleton `NavigationManager`, second router, or effect transport ecosystem |

## 4. Two contract questions to settle *from fixtures and source* before implementation
- **Alias/default:** Frozen doc20 writes `pop/back` as current supported wire operation, while also forbidding invented aliases. Existing `NavigateActionHandler` handles `back` and executor maps a top-level `back` type to `dismiss`. Do **not** delete `back` reflexively; first inventory canonical Login/OTP fixture and backend actual action shapes. Absent `mode` may default to Push, but unrecognized supplied value must fail. `pop` may not need destination; current handler requires it universally; settle exact wire behavior.
- **Entry lifetime:** Nav3 may retain state for entries through saveable-state mechanisms even when composition leaves; `remember(entry)` by itself is not proof of preserved bindings. Reproduce with tests, then choose smallest stable entry-owned holder, not premature retained Store globally.

## 5. Sequence error outcome (to freeze)
Current sink receives effects and `navigation.present` returns Boolean but UI consumer ignores it. Proposed policy: rejected effect yields a safe, explicit failure surfaced to the currently relevant entry **without aborting unrelated batches**, preserving previous successful navigation/effects. Must decide whether effects later in the same batch continue on an application failure; executor fail-fast applies to *action execution*, not necessarily effect delivery. Avoid silently claiming transactional rollback.

## 6. Incremental test gates
**W4-A (no behavior changes):** regression tests for mode decoding, same-destination identity, push/pop/replace/modal, and source-entry disposal+effect sequence reproduction.
**W4-B:** batch handoff + one session consumer; proof nav-first sequence preserves later effects, state commit order, cancellation, no duplicate handling, and rejected transition semantics.
**W4-C:** explicit entryId and entry-bound binding persistence via existing Nav3 lifecycle; test revisits and removal.
**W4-D:** strict operations and safe error handling; docs reconcile; `:navigation:jvmTest :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`, `git diff --check`. Native runtime Android/iOS deferred.

## 7. No-go / release gate
Do not implement `popTo`/refresh wire keys, generic NavigationManager, parallel Nav stack, per-effect global bus, ViewModel, new Gradle module, unverified process restoration or platform app-store logic. Do not edit Kotlin until the owner accepts/freezes this exact W4 plan. This document is a planning artifact, not passing tests or implementation authorization.
