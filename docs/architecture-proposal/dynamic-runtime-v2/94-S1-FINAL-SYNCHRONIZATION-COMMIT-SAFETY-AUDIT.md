# 94 — S1 Final Audit: Synchronization and Commit Safety

**Status: SOURCE_AUDIT_COMPLETED; MINIMAL FIX PLAN FOR OWNER REVIEW. S1 NOT FINAL-FROZEN. NO PRODUCTION EDIT AUTHORIZED.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Source baseline:** `584c7e79aaeea147c24c56f389c26e8f1cc8a2bd`.
**Authority:** frozen D1–D4 in 89, exact API 91, S1 scope 92 and audit 93.

## Evidence actually observed
Owner: `:app:jvmTest :feature:dynamic:jvmTest` **BUILD SUCCESSFUL** after the S1 extra test commit (16 common G2-B cases + 1 JVM concurrent test). Owner: `:app:compileKotlinWasmJs :androidApp:assembleDebug` **BUILD SUCCESSFUL** (compile/build only, not runtime contention). iOS compilation or runtime not validated on Windows.

## Source-level verdict by point

### A — AtomicBoolean lock: performance/liveness risk, not demonstrated correctness failure
`DynamicSession.locked` uses `while (!busy.compareAndSet(false,true)) {}` then `busy.store(false)` in `finally`. It serializes all session operations under observed JVM load, and Wasm/Android compilation succeeds. A non-yielding spinlock can burn CPU under contention; if a holder is paused/preempted and a UI thread spins waiting, responsiveness suffers. No proof in current tests that the lock itself has deadlocked or corrupted state. Do **not** introduce a separate manager or claim guaranteed platform liveness. Before changing synchronization primitive, compare narrow KMP-compatible options against the frozen non-suspending API, then run a bounded contention/stress test. A suspending `Mutex.withLock` would require changing the frozen signatures and is not silently authorized.

### B — Commit ordering: real failure window in current source
Current commit logic:
```kotlin
val copied = effects.toList()
runtimeState.setAll(changes)
if (copied.isNotEmpty()) {
    batches.addLast(Batch(++nextBatch, copied, trace))
    changed()
}
```
If the `Batch` allocation or `ArrayDeque.addLast` throws after `setAll` succeeds, the runtime patch remains visible without an accepted batch. The private lock protects only session operations: external `runtimeState.values` observers can see the patch immediately; they do not share the session lock. This violates the strongest interpretation of D1's failure-atomicity and **requires correction or explicitly evidenced feasible guarantee** before final freeze.

**Smallest candidate correction:** Preconstruct/copy the batch before publishing state, append it to the protected queue first, then apply the state patch within the same critical section; on a synchronous `setAll` exception, remove the just-appended batch *while still locked*, then return/throw without advertising successful acceptance. Publish `workRevision` only after successful patch+queue work. Recheck cancellation before entry to the non-suspending commit, in S2 Store wiring. This minimizes a state-patch-without-effect window for ordinary exceptions, but **does not grant a transactional snapshot to independent StateFlow collectors**, does not guarantee crash/power-loss atomicity, and assumes `setAll` either publishes the full map or throws before publication. Confirm the actual `MutableStateFlow.update` behavior and test adverse cases rather than overpromise rollback of every possible failure.

### C — RuntimeState write bypass: S2 integration blocker
`DynamicSession.runtimeState` exposes the existing `DynamicRuntimeState` and `setAll` is public. The current `DynamicStore` still writes directly then makes a suspending callback, outside the session lock. Thus S1 tests prove only `session.commit` operations; **full end-to-end D1 cannot be declared satisfied** until separately authorized S2 callback migration, even with reordered S1 internals. Do not modify `DynamicStore` or `ApplicationContent` under this S1 audit.

### D — Consumer lease/claim identity: edge-case hardening candidate, not proven externally exploitable
`ClaimedEffect` is a constructible data class; `active?.claim != claim` checks structural equality. Code inside the app module could recreate identical values and get accepted in place of the reserved object. Consider reference identity `active.claim !== claim` (or other minimal opaque internal identity) without changing frozen method signatures, then test that `claim.copy()` cannot dispatch/ack. Claiming this to be a remote security vulnerability would be ungrounded.

### E — Outcome and wakeup: defer consumer UI semantics to S2
`acknowledge(..., outcome)` does not inspect `outcome`; rejection advances cursor correctly but no retained error is recorded. Since S1 deliberately has no effect consumer or UI error, defer generic error display to S2 consumer review. `workRevision` is a conflated StateFlow signal, not a per-event count or queue; caller must subscribe, drain until `null`, and recheck revision. Existing tests only verify local counter increases and synthetic drain, not real Compose lifecycle. Do not add a second channel in S1.

## Minimal tests proposed before correction authorization
1. A **specific claim-copy test**: current value-equality validation accepts copied matching claim, so an assertion of rejection should RED until a narrow fix. Also verify original reservation still succeeds.
2. Closed session/consumer takeover/unfinished reservation tests already exist; avoid duplicating them.
3. State/queue exception-order test: direct deterministic failure injection is not supported by existing public API. Decide whether an injectable test hook would violate SRP before adding one; prefer source-guided minimal reorder with existing invariant tests over a new production dependency purely for fault injection.
4. JVM bounded contention test with concurrent commit/attach/close, deterministic latch start and bounded waits. The existing 4-thread/200-batch test verifies successful concurrent commits, not a scheduling/liveness guarantee.
5. Cross-platform compile/build gate already passed; Web runtime stress and Android main-thread lifecycle observation still absent. iOS compiler verification on macOS later.

## Recommended narrow correction authorization, NOT presently granted
**Only** `app/src/commonMain/.../DynamicSession.kt` plus `app/src/commonTest/.../DynamicSessionEffectsTest.kt` (and JVM contention test only if it actually adds a distinct assertion). Fix verified claim identity and reduce failure ordering window without introducing a new class, a second state owner or changed public API. Propose any replacement for `AtomicBoolean` after a scoped performance/liveness demonstration; do not automatically refactor a compiling working lock based on hypothetical contention. Keep `App.kt`, `ApplicationContent.kt`, `DynamicStore.kt`, `DynamicRuntimeState.kt`, Gradle and backend unchanged.

## Closure criteria
- Focused claim-copy RED -> corrected GREEN if accepted by owner.
- Commit queue+state consistency plan reviewed and implemented under separate explicit authorization.
- JVM suite remains GREEN, Android/Wasm compile gates stay GREEN after fixes.
- Explicitly scope final S1 claim to isolated session logic; no end-to-end D1 assertion before S2.
- Source audit + regression results + owner approval before S1 final freeze.

No Kotlin production source was changed for this audit.
