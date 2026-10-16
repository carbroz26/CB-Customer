# 77 — Shared Action Effects and Error Ownership: Read-Only Source Audit

**Status: AUDIT DRAFT — NO PLAN_FROZEN — NO KOTLIN AUTHORIZATION.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`. Source after resolver consolidation and N1. Backend development READ ONLY. State Action deferred; Sequence inert.

## Source examined
`feature/dynamic/action/DynamicActionResult.kt`, `DynamicActionExecutor.kt`, `SequenceActionHandler.kt`; `feature/dynamic/store/DynamicStore.kt`, `feature/dynamic/DynamicContainer.kt`, `app/ApplicationContent.kt`, `navigation/DynamicNavigationHost.kt`; `feature/dynamic/commonTest/DynamicStoreTest.kt`, `DynamicActionExecutorTest.kt`.
This was remote source inspection; no local build, runtime smoke, full repository symbol index or test execution performed.

## Current effects flow
Handler -> `DynamicActionResult` with `stateChanges` and `effects: List<DynamicActionEffect>` -> `DynamicStore.applyActionResult`: `runtimeState.setAll(result.stateChanges)` **before** `deliverEffects(result.effects.toList())` -> app `Channel<List<DynamicActionEffect>>(Channel.UNLIMITED)` remembered per navigation state -> `LaunchedEffect` sequentially iterates batches and their effects -> URI handler / NavigationState.
The current `Completed` and `StateChanged` return zero effects; `Navigate`, `Present`, `Dismiss`, `ExternalUri` each return a singleton effect. Inert Sequence does not aggregate actions. **Current in-repo result variants do not generate multi-effect batches**, although Store/app contract supports them.

## Current error flows
1. **Action computation error:** Executor `runCatching`, rethrows `CancellationException`; Store receives Result.failure, preserves current screen and bindings, sets sanitized `validationMessage = "Unable to complete action"`; DynamicContainer renders dismissible bottom message.
2. **Request validation error:** Store validates request flag and current form before action executor; validation messages stay in `DynamicScreenState.validationMessage`.
3. **Effect application error:** App `LaunchedEffect` catches non-cancellation exceptions per effect, sets batch `failed=true`, **continues with later effects** in same batch, then writes `effectError = "Unable to complete this action"` to app-level dismissible message. Thus a batch may partially succeed even when one effect fails; if batches ever contain 2+ effects, later effects execute. No handler currently emits such batches.
4. **Store intent `DynamicIntent.EffectFailed(message)`:** exists and tests call it. Its `reportEffectFailure(message)` ignores argument, reduces current screen with sanitized `validationMessage`. Current `ApplicationContent` effect consumer does **not** send `EffectFailed` to the originating Store. Therefore a second, currently unused-by-app error delivery mechanism exists; this is a candidate for removal only after full caller check.
5. **Error-message distinction:** App reports `"Unable to complete this action"` and Store reports `"Unable to complete action"`; both sanitized, but distinct UI surfaces and recovery clearing mechanics. Do not blindly consolidate because lifecycle scopes differ.

## Lifecycle and cancellation
- `DynamicContent`: `navigationState = remember(initialDestination)`; channel `remember(navigationState)`; `DisposableEffect(effectBatches)` closes channel; effect loop `LaunchedEffect(effectBatches,navigationState,uriHandler)`; `effectError` remembered per navigationState.
- `DynamicContainer`: `store = remember(entry)`, `DisposableEffect(store)` invokes `close()`, store load/action jobs and its scope are canceled; dynamic renderer sends `DynamicIntent.Action` to entry Store.
- Store sends via suspending callback into unbounded app channel. Queued effects can outlive individual initiating entry while the navigation session persists; precise staleness policy on entry disposal/rapid successive navigation not specified/tested.
- `Channel.UNLIMITED` has no backpressure. This is a property, not proof of a real production overflow.
- Executor and app effect loop rethrow cancellation. Store action failure path likewise rethrows cancellation; cannot claim all cancellation paths covered without execution-oriented testing.

## Existing test coverage / gaps
- `DynamicStoreTest.retainedStateIsCommittedBeforeRequestNavigationEffect` asserts retain committed when effects delivered.
- Store tests cover `DynamicIntent.EffectFailed`, action failures preserving current screen/bindings, Request validation.
- `DynamicActionExecutorTest` covers registered handler dispatch, failure, cancellation propagation, inert Sequence.
- No direct app-level `LaunchedEffect` effect consumer test found in examined suites. No explicit test of effect-batch partial failure, sequential continuation, channel-close/queued effects, or app error-surface dismissal found in examined test inventory.

## SRP judgment
- **KEEP boundaries:** Handler computes result; Store commits typed state; app applies platform/navigation effects; container displays per-screen action errors. No new manager/router, and do not move navigation or URI handling into Store.
- **P1 Potential redundancy:** List-of-effects/batch channel for current singleton/empty effects. Simplification appears possible but would cross 3 layers, require ordering and lifecycle decision, and has no immediate feature requirement. **DEFER.**
- **P2 Potential dead path:** Store's `DynamicIntent.EffectFailed` and `reportEffectFailure` have test callers but no identified production sender in examined current app consumer. **Candidate to remove after exhaustive usage scan, not now.**
- **P3 Actual semantics to clarify if changes proposed:** Which UI owns effect failure after entry disappears; whether to continue batch after an effect fails; whether queued stale effects execute; safe error content and clearing. No future Sequence semantics invented.

## Specific proposed next verification (read-only)
1. Whole repository search for `EffectFailed`, `reportEffectFailure`, `deliverEffects`, `DynamicActionEffect`, `Channel<List<DynamicActionEffect>>`, `effectError`.
2. Correlate production vs tests; enumerate lifecycle holders and each effect sender.
3. If clean, prepare separate explicit owner decisions for (a) retaining/removing dead Store effect-failure intent and (b) deferring or narrowing singleton effect batch contract.
4. No Kotlin changes until plan frozen and separate explicit implementation authorization. Android/Web interactive smoke remains pending independently.
