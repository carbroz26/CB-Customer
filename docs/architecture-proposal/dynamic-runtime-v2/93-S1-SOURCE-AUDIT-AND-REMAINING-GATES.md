# 93 — S1 DynamicSession Source Audit and Remaining Gates

**Status: AUDIT_COMPLETE — S1 NOT YET FINAL-FROZEN; NO PRODUCTION EDIT AUTHORIZED BY THIS AUDIT.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Source baseline:** `05b4214d53b131a66dc7385a9f390912c056090c`
**Authorities:** frozen 89 D1–D4, 91 exact API, 92 S1-only plan.

## Evidence
Owner ran `./gradlew :app:jvmTest :feature:dynamic:jvmTest` against `05b4214d`, result `BUILD SUCCESSFUL`. App G2-B 12 regression cases pass. This is JVM sequential/deterministic proof only; Android host, Wasm, iOS and concurrent commit races are not established. No S2/app consumer integration is implemented.

## Line-level findings in DynamicSession.kt
1. **Responsibilities/SRP:** Session does NOT dispatch navigation, execute actions, open URI, hold Koin/Store. It owns existing navigation/runtime instances and effect progress. Aligned with 85/89.
2. **Serialization:** Private `AtomicBoolean busy` and `while (!busy.compareAndSet(false,true)) {}` gate session mutable queue/lease/close and commits; no suspension, but **busy-wait** can monopolize a caller thread, including a UI thread. Prove platform suitability/fairness or replace within a narrowly reviewed S1 fix. No network or platform calls occur under lock.
3. **Commit consistency:** `runtimeState.setAll(changes)` occurs before `batches.addLast(...)`. The lock prevents *other session API calls* from observing intermediate queue state, but StateFlow observers can see the new values independently. A throwable/allocation failure after `setAll` but before successful enqueue can leave state without accepted batch. Current tests do not inject this failure. D1's full guarantee is not proven. Avoid claiming independent Flow observers see one atomic snapshot.
4. **External mutation:** `runtimeState` is public and `setAll` callable from outside the session, so the session lock does not serialize arbitrary external writes. S1 cannot guarantee all runtime state writes are transactional until Store/App wiring is replaced in S2. Delimit the S1 guarantee to commits made through session API.
5. **Queue progression:** `ArrayDeque`, batch current index, reservation, and `advance()` support ordered partial batch reattach. `detachConsumer` releases an unstarted claim, or treats a started unacknowledged claim as uncertain and moves forward. This matches D2, including deliberate possibility an effect was not actually completed.
6. **Lease takeover:** `attachConsumer()` revokes prior claim/lease by `releaseReservation()`; stale `markDispatchStarted/acknowledge/detach` fail or have no effect. Single active lease invariant appears coherent.
7. **Claim token:** `ClaimedEffect` is public-internal nested data class, caller constructible, and validation uses value equality. Internally untrusted forged-value token could match a reservation. This is an ownership integrity/testability concern, not evidence of a remote security exploit. Prefer session-created opaque identity or stricter reference identity if warranted without extra manager/model.
8. **Outcome:** `acknowledge(claim, outcome)` ignores `outcome` other than advancing. `Rejected` continuation is correct but a generic error indication is left entirely to future active UI consumer; S1 has no retained safe failure status on detach. Check against W4 UI-safe error contract in S2 without introducing a manager.
9. **Wakeup:** `workRevision` increments for commit of nonempty effects, attach, detach, ack/advance, close. `StateFlow` conflates; consumer must drain pending work and check latest revision, not rely on counting every emission. Wrap-around `Long` is academic; do not overengineer.
10. **Exceptions/retirement:** closed rejects commit, claim, mark and ack; `attachConsumer()` returns sentinel 0, which should be tested. `close()` repeat is idempotent. Commit queue may hold the effect values until retirement; no disk persistence. `effects.toList()` copies list structure; it does not deeply copy nested mutable object structures inside effect destinations. Avoid claiming deep immutability without contract evidence.
11. **Concurrency tests absent:** the four additional tests cover wakeup, takeover, empty batch, and sequential FIFO; none is a true multi-threaded barrier test. `runTest` alone cannot prove thread contention safety. Need JVM parallel-thread test and cross-platform compilation evidence before freezing synchronization.

## Recommended narrow S1 audit closure (NOT implementation authorization)

- Add/execute deterministic JVM simultaneous commit/lease/close interleaving tests with explicit barriers, no sleeps; ensure test does not hang even under busy-wait.
- Test rejection of fabricated/copy tokens, duplicate claimed effects, inactive lease 0, stale detach after replacement, close during reserved/started claim and no post-retirement acceptance.
- Test `workRevision` wakeup with an actual simulated consumer-drain loop rather than only comparing counters.
- Decide whether spin-lock can be retained based on Android UI/Wasm/Native characteristics and contention tests; if not, choose smallest KMP-safe critical section change, not a new manager.
- Strengthen D1 fail-before-state safety where possible (prebuild immutable batch/queue work before state update, review throwable points) and explicitly delimit non-atomic independent StateFlow observers.
- Audit effect claims against immutability and decision D3, avoid business-specific tests.
- Review UI-safe rejected outcome handling during S2; no S2 source edits in S1.
- Run `:app:jvmTest :feature:dynamic:jvmTest`, `:app:compileKotlinWasmJs` or exact discovered Wasm task, and Android compilation/build; Native iOS runtime/compile on macOS later.
- Do not treat passing JVM tests as full D1 end-to-end proof; split state/effect callback in Store remains until separately approved S2.

## Verdict
**S1 JVM behavioral GREEN, audit identifies non-blocking design alignment plus blocking final-freeze proof gaps** (contention, commit failure window, actual concurrency/consumer wakeup). No production Kotlin changed in this review. No G1-B, G3, G4 or S2 authorization inferred.
