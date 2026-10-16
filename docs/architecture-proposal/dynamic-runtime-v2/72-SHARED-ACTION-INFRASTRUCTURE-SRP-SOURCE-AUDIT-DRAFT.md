# 72 — Shared Action Infrastructure: Source-Level SRP / Duplication Audit

**Status:** READ-ONLY AUDIT / DRAFT — NOT PLAN_FROZEN; NO KOTLIN IMPLEMENTATION AUTHORIZED.
Frontend branch `architecture/dynamic-runtime-v2-discussion-freeze`. Backend unchanged. State target/visibility contract postponed by owner; Sequence remains a standalone inert registered placeholder.

## Scope and source reviewed
Main: `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicActionValueResolver.kt`, `DynamicReferenceResolver.kt`; direct boundaries `DynamicActionHandler.kt`, `DynamicStore.kt`, `DynamicRuntimeState.kt`, `DynamicRenderContext.kt`, `DynamicModule.kt`, `RequestActionHandler.kt`, `SequenceActionHandler.kt`, and navigation/other action handlers. Tests: `DynamicActionExecutorTest.kt`, `DynamicActionValueResolverTest.kt`, `DynamicReferenceResolverTest.kt`, `DynamicStoreTest.kt`; app effect consumer reviewed in `ApplicationContent.kt`. No Gradle build executed in this read-only audit.

## Findings by responsibility
### A. DynamicActionExecutor — KEEP
- Single responsibility: normalize action type, dispatch to explicitly registered handler, propagate typed Result with cancellation preserved.
- Duplicate handler check prevents accidentally shadowing handler registration; Koin `DynamicModule` registers seven handler types explicitly.
- No nested Sequence logic, no network/navigation/UI mutation, no second registry. Top-level `back` aliases to `dismiss`; owner froze retention for now in Navigation/Presentation plan 69. Do not remove in this audit.
- Minor concern: `trim().lowercase()` occurs at registration and lookup (legitimate symmetry, not harmful duplication). No justified refactor.
- `Result` wrapper + Store success/failure handling is a deliberate error boundary. Keep pending exact test evidence.

### B. DynamicActionResult + DynamicActionEffect — KEEP structure; potential redundancy deferred
- `Completed` and `StateChanged` yield 0 effects; `Navigate`, `Present`, `Dismiss`, `ExternalUri` each materialize 1 effect. Existing Sequence cannot produce multiple child effects.
- An action result may carry BOTH retained state changes and navigation effect (Request); Store commits `runtimeState.setAll` BEFORE delivering effects. This is a required contract and a tested behavior.
- Current `effects: List<DynamicActionEffect>` → `toList()` → `Channel<List<DynamicActionEffect>>` → double for-loop in `ApplicationContent` is heavier than current 0/1 semantics. However those types cross feature/app boundaries and may be retained intentionally for effect handoff. No removal without caller + lifecycle + failure/cancellation audit.
- Separate result and effect types are NOT obviously duplicates: result contains changes; effect represents app-owned navigation/presentation/URI side effect. Avoid collapsing them prematurely.

### C. DynamicActionValueResolver vs DynamicReferenceResolver — actual forwarding duplication
- `DynamicActionValueResolver` forwards `resolve`, `string`, `boolean`, `resolvePath` verbatim to `DynamicReferenceResolver`, plus `asString` convenience. It has no action-specific state, validation, or resolution policy.
- `DynamicReferenceResolver` already resolves typed trees across `$binding`, `$context`, `$state`, `$literal`, rejects unsupported `$response` and mixed references, and exposes string/boolean/path helpers.
- `DynamicRenderContext.resolve` and Store validation use `DynamicReferenceResolver` directly while action handlers call the forwarding object: two names for the same logic, extra abstraction/cognitive load. This is a genuine low-risk consolidation *candidate*, but must inventory all usages including Request retain and non-Request handlers before edit.
- Path helpers are NOT duplicates of identical behavior: `resolvePath` collapses missing to `DynamicValue.NullValue`, while `resolveExistingPath` returns Kotlin null for missing but preserves present `DynamicValue.NullValue` for Request retention. Keep both semantic contracts while simplifying naming, and add coverage before moving anything.
- `asString` converts unknown/missing to empty string and may mask wrong types; no identified production consumer in examined files. Verify whole project before deleting.

### D. Store and consumer — legitimate separation with local cleanup opportunities
- Store owns pre-request form validation, serialized action acceptance, result state commit, safe action error. Executor owns dispatch only. Session app owns effect application. KEEP boundaries.
- `DynamicStore` contains request-only validation behavior; it is specific, but frozen Request contract deliberately put form-validation ownership in Store. Do NOT extract another manager or move form validation into executor.
- `DynamicIntent.EffectFailed` exists and safely reduces in-place, while `ApplicationContent` uses a separate session-level `effectError` instead of dispatching this intent. Potential duplicate error-display paths; defer to session effect audit, not automatic removal.
- `RequestActionHandler` `resolveString/resolveBoolean/resolve` are local convenience wrappers around resolver, with missing/default checks; distinguish necessary contract handling from pure wrapper duplication.
- `SequenceActionHandler` inert placeholder must not execute payload.actions or be removed.

## Risk and priority
**P1 (source-proven):** redundant no-policy `DynamicActionValueResolver` forwarding object. Proposed candidate remove ONLY after complete use-site inventory and parity tests; exact file list needed.
**P2 (observed complexity, no change yet):** result effects list + channel batches currently 0/1, but simplifying requires joint feature/app lifecycle testing and owner approval.
**P3 (document only):** Store vs app error signaling duplication, Request local convenience wrappers, normalization location and path-missing vs explicit null behavior. Neither warrants immediate architecture redesign.
**KEEP:** Executor registry, Result/Effect separation, Runtime state store, Store orchestration, single session consumer, cancellation propagation, Sequence inert.

## Proposed next exact cleanup plan (DRAFT, NOT AUTHORIZED)
1. `feature/dynamic/action/DynamicActionValueResolver.kt` — REMOVE only if verified every consumer can use existing `DynamicReferenceResolver` without semantics change and visibility permits.
2. `feature/dynamic/action/RequestActionHandler.kt`, `NavigateActionHandler.kt`, `ExternalUriActionHandler.kt`, `StateActionHandler.kt` — narrow import/call-site replacements only; explicit State contract stays deferred; no behavior changes.
3. `feature/dynamic/action/DynamicActionValueResolverTest.kt` — replace forwarding-only coverage with equivalent resolver tests in `runtime/DynamicReferenceResolverTest.kt` or maintain parity tests; do not silently delete assertions.
4. `feature/dynamic/runtime/DynamicReferenceResolver.kt` — KEEP all typed/null/path semantics unchanged, adjust only if visibility/compiler requirements proven.
5. `DynamicActionExecutor.kt`, `DynamicActionResult.kt`, `DynamicStore.kt`, `DynamicModule.kt`, `ApplicationContent.kt`, `SequenceActionHandler.kt` — KEEP in first cleanup slice.
6. Batches/lifecycle and session error semantics — separate draft and decision after tests/caller inventory; no invented future multi-effect use case.

## Pending before PLAN_FROZEN
- Search **all frontend Kotlin references** to each resolver method and action result/effect factory (including generated/other modules) and confirm helper visibility and `asString` use.
- Confirm no behavior drift: nested object refs, unsupported `$response`, missing-vs-null retains, Request mode errors before HTTP, type coercion, cancellation, state-before-effect.
- Freeze *only* the narrow resolver consolidation if approved, then distinct explicit Kotlin authorization; write regression tests first and run `:feature:dynamic:jvmTest :domain:jvmTest :data:jvmTest :navigation:jvmTest :webApp:wasmJsBrowserDevelopmentWebpack` and `git diff --check`.
