# 88 — G1/G2 Minimal Session Effect API Review

**Status: REVIEW_CANDIDATE — NOT FROZEN. NO PRODUCTION KOTLIN AUTHORIZATION.**
**Baseline:** `695f16a33ce3e0920c134196a10f93686543882d`.
**Authority:** 51 W4, 85 session freeze, 87 regression design. G2-A JVM tests passed on owner's Windows machine.

## Source-proven constraints
- `App.kt` scopes Koin to `remember` and closes via `DisposableEffect`.
- `ApplicationContent.kt` scopes `Channel.UNLIMITED` and consumer to composition.
- `DynamicStore.applyActionResult` mutates `DynamicRuntimeState` then calls a **suspending** effect callback. State/effect handoff is therefore not established as a cancellation-atomic transaction.
- `DynamicRuntimeState.setAll` performs non-suspending `StateFlow.update`, but that alone does not atomically combine session queue acceptance.
- Current concrete `DynamicActionResult` types expose 0 or 1 effect. G2 continuation must still be designed for lists (future-compatible contract), without fabricating current multi-effect production usage.
- Android retain lifetime & final retirement were observed with opt-in probe. Koin-safe logical session lifetime is not yet verified.

## Candidate API (conceptual, not exact types or signatures)
```kotlin
internal class DynamicSession(
    val diagnosticSessionId: String,
    initialDestination: DynamicDestination,
) {
    val navigationState: DynamicNavigationState
    val runtimeState: DynamicRuntimeState

    // Prefer one non-suspending commit operation accepting the
    // computed state patch + immutable effect batch together.
    fun commit(result: DynamicActionResult, trace: TraceContext?)

    fun claimNext(consumerId: Long): ClaimedEffect?
    fun markDispatchStarted(claim: ClaimedEffect)
    fun acknowledge(claim: ClaimedEffect, outcome: EffectOutcome)
    fun detachConsumer(consumerId: Long)
    fun close()
}
```
The above is conceptual. `commit` must not move action execution into session, and its exact placement against module boundaries and existing `DynamicRuntimeState` owner must be signed off. The session should not manufacture or mutate `DynamicActionResult` semantics. `consumerId`, `ClaimedEffect` and `EffectOutcome` are illustrative, NOT authorization for separate public models/managers. Prefer scoped private internal types/fields when proven adequate.

## Candidate transition semantics
1. **Accept/commit:** After action result success, perform state patch and append copied ordered batch in a cancellation-safe non-suspending critical region. A suspending `send` after `setAll` does *not* suffice. Review concurrency because action stores can act concurrently; ordering follows serialized successful commits, not unrelated job start time.
2. **Pending:** Not claimed, no dispatch. Remains across consumer detach.
3. **Claimed but NOT dispatch-started:** Consumer owns a reservation; on explicit detach it can return to pending for a new consumer. A single consumer owns work at any instant.
4. **Dispatch-started:** Persist attempt position *before* platform invocation. If consumer detaches or callback completion becomes ambiguous, do not automatically re-execute this effect (especially external URI). Record uncertain/skipped state and proceed to later effects only after explicit policy. This is at-most-one *attempt*, not guaranteed external completion.
5. **Acknowledged success/failure:** Effect is terminal for continuation. Failed navigation produces safe error, with next effect still attempted. Acknowledgement must not expose URI or exception text.
6. **Close:** Mark session retired, discard pending batches, revoke claims, ignore later callbacks. Idempotent. Temporary detach does not close session.

## Races and limits needing red tests
- Consumer loss **before** `markDispatchStarted`: released claim must be retryable.
- Consumer loss **after** start but before ack: must not redispatch; later effects remain processable under chosen skip policy. A synchronous navigation effect may be applied just before a lifecycle cancellation; a claimed-but-unacknowledged navigation must not be blindly replayed.
- A stale consumer must not apply or acknowledge after a replacement consumer attaches; consumer generation/identity checks need real testing.
- `markDispatchStarted` and actual UI/platform call must be adjacent in the consumer; platform call itself cannot be atomic with the in-memory marker. Session teardown or process death can still prevent completion.
- `commit` must be tested against cancellation before successful result and the race between state mutation and queue acceptance.
- `EffectOutcome` is internal UI-safe status. Do not log or persist URI, phone, token, form values or raw exception details.

## Regression sequence before production edit
- Extend `app/commonTest` (once API signature approved) with accept/claim/ack/detach/reattach, stale consumer, partial batch, failed navigation, external-uri ambiguous attempt, idempotent close.
- Add action-store commit boundary test with deterministic scheduler and injected suspension/cancellation; do not treat the three passing G2-A tests as proof of atomic commit.
- Prove G1 bootstrap handoff and fresh Koin-graph Store reconstruction in separate integration tests.
- Run JVM tests then physical Android retention/integration, Wasm build and Web host testing. iOS must be validated on macOS.

## Decision requests (NOT automatically frozen)
D1. Agree to non-suspending, serialized state patch + batch acceptance as one logical commit boundary, without shifting action execution into session.
D2. On detach before dispatch-start: retry reservation. After dispatch-start but before acknowledgement: no automatic replay; mark uncertain and continue later effects. Accept at-most-one attempt rather than exactly-once completion.
D3. Single consumer lease, no second controller, private minimal progress state; stale consumer cannot mutate session state.
D4. Minimal G1 handoff: retained session has bootstrap outcome identity/destination and state only; active composition owns Koin, URI and newly-created entry Store.

No production source changed, no API implementation exists, no G1-B/G2-B red tests executed.
