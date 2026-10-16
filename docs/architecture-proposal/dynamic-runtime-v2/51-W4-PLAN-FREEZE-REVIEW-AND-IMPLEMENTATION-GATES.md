# 51 — W4 Plan Freeze Review and Implementation Gates

**Status: PLAN_FROZEN — OWNER APPROVED D1–D6 AND EXACT FILE SCOPE; KOTLIN IMPLEMENTATION NOT AUTHORIZED.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Source authority:** frozen baseline 20, source audit 48, file plan 49, freeze candidate 50, canonical fixtures summary 12, current DynamicActionMapper / NavigateActionHandler / DynamicActionExecutor / DynamicStore / NavigationState / Nav3 Host and existing tests.

## Final source check on disputed semantics
- The canonical owner fixture *summary* in doc12 shows Navigate destination but does not demonstrate explicit Navigate mode, destinationless Pop, or top-level `back` action. Therefore claiming any of these wire usages are proven by fixtures would be inaccurate.
- `DynamicActionMapper` simply passes the action `type`, payload and optional four-field destination to Domain; it does **not** manufacture or normalize a `back` action.
- `NavigateActionHandler` currently requires destination on every navigation action and defaults unknown mode to Push, which violates doc20's strict unknown-operation requirement.
- `DynamicActionExecutor` contains an explicit top-level `back` alias to `dismiss` that is separate from `navigate.payload.mode=back`. No observed fixture usage proving that top-level alias was found in the reviewed source/tests. Its removal is **not** authorized by this plan freeze until targeted evidence and sign-off because it may be compatibility-sensitive.
- `DynamicStoreTest` only tests live-collector ordered effects; there is no entry-removal sequence regression. The source lifetime mismatch remains unproven in a live platform test.
- `NavDisplay` state retention and saveable-state behavior is not established by JVM NavigationState tests; keep integration proof as implementation gate.

## Proposed decisions ready for owner acceptance
**D1 — Authority/ownership.** One `DynamicNavigationState` owns stack and modal transitions. Executor computes ordered action results only. Entry-scoped Store owns intents, screen state, bindings and validation; commits shared state once and publishes an immutable whole effect batch to one app-session-owned effect sink; `DynamicContent` owns single consumer and platform URI execution. Container only renders entry. RuntimeState remains session-retained. No second Nav authority or global manager.

**D2 — Wire modes.** Missing `navigate.mode` => Push (backward compatibility); explicit Push/Replace/Pop/Back accepted; explicitly unknown/null/wrong-typed mode => typed failure, **never Push**. Push/Replace require destination; Pop/Back do not; Present requires destination, Dismiss does not. No popTo/refresh. Because doc20 specifies `pop/back`, support the mode alias; top-level `back` remains compatibility-held pending evidence, not a newly introduced action.

**D3 — Effect transaction boundary.** Compute successful entire action/Sequence, commit full state patch once, then submit ordered effects as one batch to session-owned consumer. Failed/cancelled action => no new state/effects. Effect *application* rejection => safe visible error outcome and continue later effects in already-committed batch. Already performed effects are not rolled back. Session disposal cancels pending work; source entry disposal cannot cancel accepted work. Delivery is sequential and at most once within one session, with explicit handling of failures.

**D4 — Entry lifetime.** Unique visit `entryId`, including repeated same destination. Entry-specific bindings persist for as long as NavigationState contains the entry even if Compose temporarily removes its content. Pop restores existing entry and bindings, Replace discards replaced entry, Dismiss discards modal. Session retained state persists until session reset. No process-recreation persistence promised. Minimal holder chosen after red lifecycle test rather than generic SaveStateManager.

**D5 — Rejected transitions and errors.** Modal already showing => Present rejected without replacement. Pop at root / Dismiss without active modal => observable no-op/rejection, not crash; source error not leaked. Missing required Push destination fails validation. Failure representation is typed/controlled where needed, keeps usable screen data, avoids raw network/message exposure.

**D6 — Strict scope.** W4 excludes new backend fields, navigation refresh/popTo, universal managers, ViewModels, repeated queue stacks, new Gradle modules, and unrelated W5 rendering/W6 business-action rewrites. Retain established `back` handling until usage evidence is established; avoid silent compatibility removal.

## File-by-file scope
| Source | Agreed target |
|---|---|
| `feature/dynamic/action/DynamicActionExecutor.kt` | Keep sequence ordering/fail-fast; modify only if tests reveal actual gap; distinguish top-level Back compatibility |
| `feature/dynamic/action/NavigateActionHandler.kt` | Strict typed mode/default and destination by operation |
| `feature/dynamic/action/DynamicActionResult.kt` | Preserve result/effect vocabulary, narrow typing only if proven necessary |
| `feature/dynamic/store/DynamicStore.kt` | Session effect batch handoff replacing entry Channel; commit-before-handoff; safe in-place errors |
| `feature/dynamic/DynamicContainer.kt` | Remove entry-scoped effects collection; preserve per-entry UI/lifecycle |
| `app/ApplicationContent.kt` | Session-owned queue and sole consumer; existing NavigationState/URI adapter |
| `navigation/DynamicNavigationState.kt` | Unique entry creation, checked transitions, membership and state lifetime |
| `navigation/DynamicNavigationEntry.kt` | Explicit visit ID and minimal entry binding state |
| `navigation/DynamicNavigationHost.kt` | Keep Nav3+Dialog; only evidence-driven lifecycle changes |
| `feature/dynamic/runtime/DynamicRuntimeState.kt` | Keep session-shared state |
| `navigation/.../DynamicNavigationStateTest.kt` | Extend entry identity and transition tests |
| `feature/dynamic/.../DynamicStoreTest.kt` | Add state-before-effects, entry disposal, cancellation and retention tests |
| `feature/dynamic/.../DynamicActionExecutorTest.kt` | Add navigate-first ordered sequence and failure tests |
| Navigate handler tests | Test absent, accepted, rejected modes and destinationless Pop |
| Session sink | At most one narrow session-owned helper if needed by type/dependency ownership |

## Freeze gates / implementation sequence
- **Gate 0 (completed):** owner explicitly approved D1–D6 and the exact file-by-file scope on 2026-10-16. Top-level `back` compatibility must be preserved pending usage evidence. **This approval freezes planning only; do not edit Kotlin without a separate implementation authorization.**
- **W4-A tests first:** failing regression for same-destination distinct entry identities, Nav3 entry disposal/binding persistence, unknown mode Push bug, navigate-first sequence effect loss. Keep reproducibility evidence.
- **W4-B:** implement only minimal session handoff plus single consumer and test state-before-effects, ordered follow-up after Navigate/Replace, effect failure, cancellation.
- **W4-C:** entryId + entry lifetime/retention on push/pop/replace/present/dismiss; test restoration.
- **W4-D:** strict mode validation, safe failures, integration and docs reconciliation.
- **Build gate:** `./gradlew :navigation:jvmTest :feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` on Windows; `git diff --check`. Android/iOS runtime remains explicitly deferred. No claim W4 tests run.

## Final sign-off status
**PLAN_FROZEN (2026-10-16).** Owner explicitly accepted D1–D6, preservation of the top-level `back` compatibility alias pending evidence, and this document's exact file scope. **Kotlin implementation remains NOT AUTHORIZED**, explicitly per owner instruction. The frozen W4 plan may be implemented only after separate owner authorization; tests and platform verification remain future gates.
