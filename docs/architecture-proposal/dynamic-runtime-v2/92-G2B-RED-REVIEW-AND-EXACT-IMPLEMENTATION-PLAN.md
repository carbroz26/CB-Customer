# 92 — G2-B RED Review and Exact Implementation Plan

**Status: IMPLEMENTATION_PLAN_PREPARED — REVIEW ONLY; NOT PRODUCTION IMPLEMENTATION AUTHORIZATION.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Source baseline:** `0ce6115ba6d16b121976e6fb2aab65196f60887a`
**Authorities:** 51, 85, 89 (owner-approved D1–D4), 90, 91 (exact API).

## Owner-observed RED evidence

Owner ran `./gradlew :app:jvmTest` on Windows after pulling baseline; compilation and test discovery succeeded; all eight `DynamicSessionEffectsTest` cases failed with assertions; Gradle task FAILED as anticipated. Native iOS simulator test not runnable on Windows. App host Android tests not enabled; not relevant to JVM red gate.

**Important evidence limit:** The current `DynamicSession.commit` stub always returns `false`, and each of the eight tests begins with `assertTrue(session.commit(...))`. Consequently all eight fail at that initial guard. This verifies test wiring and unimplemented commit, NOT separately lease, cursor, session close, or partial batch semantics. Do not overclaim individual red paths. Additional tests are required for workRevision, simultaneous successful commits, stale consumer replacement without explicit detach, and state patch + effects cancellation/serialization. Existing G2-A tests remain separately passing.

## Actual source boundaries reconciled
- `app/.../DynamicSession.kt` has only frozen API scaffolding; navigation/runtime existing owners, StateFlow revision, method stubs and nested claim/outcome types.
- `app/.../ApplicationContent.kt` uses composition `Channel.UNLIMITED`, suspension-based delivery callback, and a `LaunchedEffect` consumer; Koin is active-composition scoped. No session use yet.
- `feature/dynamic/.../DynamicStore.kt` calls `runtimeState.setAll(result.stateChanges)` then suspending effect delivery. This is a D1 gap.
- `DynamicActionResult.kt` exposes a list contract, but current concrete results produce zero or one effect. Test session batch semantics directly using existing effect variants.
- Session must never import Koin, platform URI handler or Store, and feature/dynamic must never depend on app. Existing `DynamicNavigationState` remains sole navigation authority.

## Minimal internal algorithm, no extra class/manager
Implement the exact document-91 API in existing `DynamicSession.kt`:
- Private closed flag, active lease ID, strictly increasing lease sequence, monotonic batch ID, FIFO batches with immutable effect copies, each batch's current effect index, and one active reservation with `dispatchStarted`.
- `commit(changes, effects, trace)`: reject closed without state changes, copy effects, serialize successful commits, update existing runtime state and append batch in one **non-suspending serialized critical section**. Empty list changes state only; truly empty commit is still a successful accepted no-op. All later application-side callbacks to this session share this commit boundary.
- Revision `StateFlow<Long>` only signals work availability, NOT the work queue; increments when pending work becomes drainable or lease replacement may resume it. No second channel.
- `attachConsumer()`: always grants a distinct lease, invalidating previous lease. If previous claim is not dispatch-started, release for reclaim; if dispatch-started but unacknowledged, mark terminal uncertain and advance. No automatic replay. No extra external status persistence.
- `claimNext(lease)`: lease-validated reservation of current head pending effect only; never hand out the next effect while one claim is unresolved. Same-lease duplicate claim cannot create parallel reservations.
- `markDispatchStarted(claim)`: accept once iff exact active claim and lease match, and session alive; must be invoked immediately before external action. Any later unacknowledged detach becomes uncertain, not success.
- `acknowledge(claim, Applied|Rejected)`: once only after dispatch start; on either outcome advance cursor, track UI-safe generic failure on rejected outcome via existing application UI (not raw exception/URI), trigger next-claim wakeup. Acknowledgements from older consumers are rejected. No arbitrary "uncertain success".
- `detachConsumer`: only effective for current lease. Unstarted claims released; started claims marked uncertain and cursor advanced, including ability to claim later effects on reattachment.
- `close()`: idempotently prevent all future operations and discard unconsumed effects/leases. It is final retirement, not transient host removal.
- Linearization: all mutable session queue/lease/close/commit operations must use one proven cross-platform synchronization boundary. Kotlin/Native, Wasm and JVM compatibility must be validated; do NOT assume an unreviewed Java JVM-only lock works. `MutableStateFlow.update` alone cannot protect the separate pending batch. Do not add another state manager.
- Effect ordering = successful commit order, not action start order. Queue may hold multiple batches. For same-batch multiple effects, progress index is strictly increasing.
- Consumer never holds the synchronization boundary while invoking UriHandler or modifying navigation. External effect uncertainty remains explicitly unresolved in reality; no exactly-once completion guarantee.

## Review-only file-by-file intended diff

### Slice S1 — Session behavioral GREEN (requires owner implementation authorization)
1. **Modify only** `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/DynamicSession.kt`: replace stubs with minimal queue/lease/progress/close behavior and cross-platform safe synchronization; no DI, platform URI handlers or navigation invocation.
2. **Modify only if evidence requires** `app/src/commonTest/kotlin/com/carbroz/cbpartner/app/DynamicSessionEffectsTest.kt`: add focused cases for revision wakeup, no-op/empty-effects commit, second consumer takeover, queue multiple batches, stale claim, close and simultaneous commit ordering. Preserve existing 8 assertions. No sleeps/network/business screens.
3. **No App/Store wiring in S1.** This stage tests isolated logical session behavior; even if `:app:jvmTest` becomes GREEN, D1 end-to-end cancellation boundary and G1 host lifetime remain outstanding.

### Slice S2 — Non-suspending Store boundary (separate review/authorization)
4. `feature/dynamic/src/commonMain/.../store/DynamicStore.kt`: replace existing split state mutation + suspending delivery with a **single injected non-suspending commit callback** receiving computed `changes`, `effects`, `trace`. It must invoke only after successful action and after checking cancellation while active. Preserve existing action handling, diagnostics, validation, load and entry scope. No dependency on app or session. Preserve/adjust existing G2-A tests as needed rather than simply removing their meaningful assertions.
5. `app/src/commonMain/.../ApplicationContent.kt`: wire current Store to the session callback; remove the composition-owned channel only when new consumer delivery safely applies navigation/URI and acks via frozen API. One consumer lease, ordered drain, safe error; consumer detach on composition lifecycle; session kept alive for temporary detach. Do not put Koin/UriHandler in session.
6. `app/src/commonMain/.../App.kt`: G1 retained bootstrap handoff/Koin separation once G1-B tests support it, **not automatically part of S2**.
7. `feature/dynamic/.../DynamicContainer.kt`: unchanged unless independent Store reconstruction test proves required change.

### G1 separate gate (not part of S1)
Test successful Bootstrap handoff without duplicate GET, fresh Koin graph for each active host, detach vs final retirement, new session isolation. Review actual `retain` host and Android/Web/iOS lifecycle evidence first. G3 entry response snapshot also remains a separate gate.

## Before calling S1 GREEN
- Run `./gradlew :app:jvmTest` and `:feature:dynamic:jvmTest` after changing S1; record pass/fail separately.
- Green G2-B isolated tests are insufficient to claim cancellation-atomic state + effects for production action flow.
- Add deterministic thread/dispatcher interleaving proof of concurrent `commit` before claiming D1 serialized success. `runTest` virtual time by itself does not establish actual multi-thread safety.
- Validate Kotlin/Wasm compilation and Android build later before platform-wide claims; iOS on macOS.

## Change control
This document is a narrow production **plan**, not approval to implement it. Existing approved D1–D4 and document 91 API signatures must be preserved. Backend, action wire contract, nav authority, unrelated Gradle warnings, user local untracked files and branches are out of scope.

**Next authorization:** Owner explicitly authorizes S1 (`DynamicSession.kt` logical queue/lease implementation + focused G2-B tests only), reviews results, then separately decides S2/G1. No other production edits allowed.
