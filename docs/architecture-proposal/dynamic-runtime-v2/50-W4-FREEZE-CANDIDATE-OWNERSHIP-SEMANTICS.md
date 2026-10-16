# 50 — W4 Ownership and Semantics Freeze Candidate

**Status: FREEZE CANDIDATE / OWNER SIGN-OFF REQUIRED / NO KOTLIN IMPLEMENTATION AUTHORIZED.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Authority:** frozen V2 doc20 §§1,3,5,6; source audit 48 and file plan 49; owner canonical wire doc12. This document decides *proposed* defaults and records remaining confirmation gates. It does **not** override doc20 by unilateral inference.

## A. Exact responsibilities
1. **ActionExecutor** executes and orders actions, returns one result containing state delta + ordered effect list. It has no Nav3/Compose/platform reference.
2. **DynamicStore** remains entry-specific: intents, validation, local bindings, local screen state and action job. It commits session state once before handing a **whole immutable ordered effect batch** to session scope. A producer cancellation before successful commit/handoff publishes no effects; no entry-owned cross-screen Channel.
3. **One app-session effect owner** is retained by `DynamicContent`, with one consumer living independently of any entry UI composition. Navigation effects call **only** `DynamicNavigationState`; ExternalUri calls app/UI platform adapter (`LocalUriHandler`). It owns delivery until session is disposed. No global singleton/event manager.
4. **DynamicNavigationState** exclusively creates/removes entries, manages stack/modal, applies checked push/replace/pop/present/dismiss. It does not interpret JSON/action payload. A rejected operation is observable, not silently discarded.
5. **DynamicNavigationEntry** has unique visit identity; it retains only the state necessary to restore that specific entry across push/pop. Session `DynamicRuntimeState` is not moved into entry state.
6. **DynamicContainer** only renders the entry and forwards its intents; it must not be the session effect collector.
7. **NavigationHost** stays Nav3 + Dialog. No second Nav state, ViewModel, popTo or refresh.

## B. Proposed exact behavioral semantics
**B1 — Wire mode:** absent `navigate.payload.mode` means `push` for backward compatibility; explicit `push`, `replace`, `pop`, `back` are the only supported spellings (doc20 explicitly calls out `pop/back`). Provided null/non-string/unknown values fail validation; no fallback to Push. Do not confuse `navigate(mode=back)` with undocumented top-level `action.type=back`; keep the latter only if canonical backend fixture/test proves it is a supported compatibility requirement. Canonical doc12 shows navigate destination but does not settle `mode`, so this is a proposed default, not direct fixture evidence.

**B2 — Destination:** `push/replace` require the existing four-field destination. `pop/back` require no destination; a legacy redundant destination on a pop may be ignored without rewriting the wire object. `present` requires destination; `dismiss` does not. Do not construct dummy destinations.

**B3 — Ordered effects:** after successfully computed Sequence, commit complete state delta then enqueue complete effect batch atomically to session owner. Consumer executes each effect sequentially, including effects after navigate/replace/dismiss that remove source entry. No guaranteed rollback of already performed effects or of state commits. On an *effect application* failure, record a safe failure and **continue later effects of the same committed batch**; this is different from fail-fast *action computation*. Order applies across batches on one consumer too.

**B4 — Rejected transitions:** pop at root and dismiss with no presentation are explicit no-ops with an observable rejected result, not crashes. Present while another modal is active is rejected and does not replace existing modal. A rejected transition in a batch reports safe error but does not swallow later effects. Push/replace with missing destination fail validation earlier, not silently ignored. Never display raw exception strings, URLs, request diagnostics, or tokens.

**B5 — Identity and retention:** new visit (including same destination) receives a distinct `entryId`; pop restores previous Entry and bindings, replace abandons replaced Entry and its bindings, dismiss discards modal Entry state. Session state persists until end/reset. Entry lifetime is determined by **navigation membership**, not temporary Nav3 composition absence. Pick the minimal entry-state holder only after a targeted lifecycle/regression test; no speculative process persistence.

**B6 — Dispatch/cancellation:** effect batch is handed over exactly once after state commit, consumer is app-session owned, entry disposal does not cancel already accepted batches; app session disposal cancels consumer and discards pending batches. No replay after application disposal. Cancellation of action before commit yields no batch and no new state.

## C. Source-specific conflicts clarified
- Existing `NavigateActionHandler` presently *requires destination before reading mode*, even for Pop. Proposed B2 fixes this against doc20 operation semantics.
- Existing `NavigateActionHandler` maps unknown mode to Push; B1 changes it to error.
- `DynamicActionExecutor.dispatch` recognizes undocumented top-level `back` alias. Distinguish from the documented `navigate.mode=back`; verify fixtures/test/actual backend usage before removing alias.
- Existing `DynamicStore` uses entry Channel and `DynamicContainer` collects it; this cannot *prove* session-safe delivery on disposal. Regression required.
- Existing `DynamicNavigationEntry` has identity by reference only; frozen explicit entryId absent.
- Existing `DynamicStoreTest.sequenceEffectsAreDeliveredInOrder` verifies entry Channel delivery under a live collector but not disposal mid-batch; do not count it as P0 acceptance.
- Existing `NavDisplay` composition lifetime may differ from back-stack membership. Test state retention through push/pop, then choose minimal solution.
- Existing `DynamicStore` puts raw `error.message` in failure UI. Do not expand error classification system; sanitize at UI-safe Store boundary.

## D. Exact implementation files after authorization
| File | Narrow edit |
|---|---|
| `feature/dynamic/action/NavigateActionHandler.kt` | strict supported mode parsing, no default for invalid supplied values, no mandatory destination for Pop |
| `feature/dynamic/action/DynamicActionExecutor.kt` | preserve ordered Sequence computation; remove undocumented alias only after evidence |
| `feature/dynamic/action/DynamicActionResult.kt` | preserve current existing effect models unless one test demands explicit outcome |
| `feature/dynamic/store/DynamicStore.kt` | state-first atomic batch handoff; remove per-entry Channel; safe errors; entry lifetime |
| `feature/dynamic/DynamicContainer.kt` | remove per-entry effect collector, keep UI/store intents |
| `app/ApplicationContent.kt` | one session-scoped sink and consumer, wire to existing navigation and URI adapter |
| `navigation/DynamicNavigationState.kt` | checked transitions, unique visit creation and lifetime handling |
| `navigation/DynamicNavigationEntry.kt` | explicit entryId and minimal entry-local state, based on Nav3 test |
| `navigation/DynamicNavigationHost.kt` | only adjust actual lifecycle gaps |
| `feature/dynamic/runtime/DynamicRuntimeState.kt` | unchanged shared session state |
| `navigation/src/commonTest/.../DynamicNavigationStateTest.kt` | transitions, identity, modal priority/rejection, entry restoration |
| `feature/dynamic/src/commonTest/.../store/DynamicStoreTest.kt` | disposal-safe effect handoff; state-before-effects; cancellation; binding retention |
| `feature/dynamic/src/commonTest/.../action/DynamicActionExecutorTest.kt` | navigate-first multi-effect sequence, failure/cancellation ordering |
| Focused NavigateActionHandler test (new or existing file) | default/allowed/rejected modes and Pop without destination |
| Session sink implementation | at most one small session-owned helper if genuinely needed for clean app/feature dependency direction; no generic manager |

## E. Gates
1. Source-level fixture/consumer inventory: verify any actual top-level `back` action and behavior on no-mode navigate. If evidence conflicts, stop for owner decision; never silently delete compatibility.
2. First write red tests for disposal of origin entry mid-effects, mode handling, entry binding restoration. Distinguish pure JVM test from Nav3 Compose lifecycle integration.
3. Implement a minimal W4-A/W4-B/W4-C/W4-D slice, compile and test `:navigation:jvmTest :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack`.
4. No platform success claims without runtime evidence. Android/iOS runtime deferred by owner.

## F. Freeze approval checklist
Proposed to freeze **only** after the owner accepts:
- common ownership A1–A7;
- semantics B1–B6, including continuing subsequent committed effects after application failure;
- distinguishing documented mode `back` from top-level `back` alias and preserving compatibility until evidence;
- entry membership lifetime rather than Compose visibility;
- exact source list/test gates in D/E.

**This file is ready for review, not a declaration of PLAN_FROZEN or permission to edit Kotlin.**
