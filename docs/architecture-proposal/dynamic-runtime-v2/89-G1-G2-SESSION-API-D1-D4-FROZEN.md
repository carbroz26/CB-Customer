# 89 — G1/G2 DynamicSession API Behavioral Contract Freeze

**Status: PLAN_FROZEN — OWNER APPROVED D1–D4 ON 2026-10-17. PRODUCTION KOTLIN IMPLEMENTATION NOT AUTHORIZED.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`.
**Source baseline:** `695f16a33ce3e0920c134196a10f93686543882d`.
**Review authority:** 88-G1-G2-EFFECT-ACCEPT-CLAIM-ACK-API-REVIEW.md, 87-G1-G2-RED-TEST-SOURCE-PREPARATION.md, frozen documents 85 and 51.

## Approved decisions (binding)

**D1 — Logical commit boundary.** Once a Dynamic action result succeeds, commit the full state patch and accept its immutable ordered effects as one serialized, non-suspending logical transaction. No suspension/cancellation point may expose an applied state patch without its accepted effect batch. The session coordinates acceptance only; the existing DynamicActionExecutor computes action results and does not move into session. Multiple action origins serialize at successful commit, not request start. Exact threading synchronization and Kotlin method signatures remain test-driven implementation details.

**D2 — Detach/reattach continuation.** Pending effects survive temporary consumer loss. Claims not yet dispatch-started can be returned to pending and claimed by the replacement consumer. Once a platform/UI dispatch attempt is marked started, no automatic retry when completion is ambiguous; mark outcome uncertain/skipped for continuity and proceed to remaining effects according to ordered contract. Successfully completed effects are never replayed; rejected effects yield UI-safe failure and do not block later effects. At-most-one dispatch attempt is a best-effort in-memory guarantee, not exactly-once external completion. No arbitrary dynamic data persistence.

**D3 — Consumer ownership.** Exactly one active application consumer may claim/apply effects at a time. Minimal private claim/progress and consumer-generation state reject stale consumers. Only the existing application consumer applies navigation operations to existing DynamicNavigationState or uses the active platform URI handler. No second controller, effect manager, or navigation state. Avoid gratuitous new public models/classes.

**D4 — Koin and session isolation.** After successful bootstrap handoff, one retained commonMain DynamicSession owns the initial destination/diagnostic identity and existing DynamicNavigationState/DynamicRuntimeState plus accepted effects/progress and idempotent retirement. Koin, SplashStore, DynamicStore, platform URI handler, activity, rendering and DI-owned dependencies are not retained. Active composition creates its own Koin graph; entry composition constructs and closes its own Store. Reattach to a retained session must not repeat completed bootstrap; new Stores bind current graph. Session retirement is not triggered by temporary host detach.

## Conceptual API (not frozen literal Kotlin signatures)
```kotlin
internal class DynamicSession(
    val diagnosticSessionId: String,
    initialDestination: DynamicDestination,
) {
    val navigationState: DynamicNavigationState
    val runtimeState: DynamicRuntimeState
    // Atomic successful-result commit: state patch + copied effects.
    fun commit(result: DynamicActionResult, trace: TraceContext?)
    // Single-consumer lease, claim, dispatch-start marker, terminal ack, detach.
    fun claimNext(consumerId: Long): ClaimedEffect?
    fun markDispatchStarted(claim: ClaimedEffect)
    fun acknowledge(claim: ClaimedEffect, outcome: EffectOutcome)
    fun detachConsumer(consumerId: Long)
    fun close()
}
```
Names, result types, identity representation and synchronization primitives remain implementation-review details and MUST NOT independently justify new abstraction layers. Result-to-session dependency boundary must be verified in proposed test harness.

## Required focused red regressions before implementation authorization
1. G1: retained session no repeated bootstrap; original diagnostic ID/initial destination/navigation/runtime preserved.
2. G1: active Koin graph recreation and new Store dependencies; old graph never retained or used.
3. G1: temporary detach never closes session; final retirement closes exactly once; new logical session isolated.
4. G2: pending/claimed-but-not-started effect handling across detach and reattach, including stale consumer rejection.
5. G2: partially applied batch resumes remaining effects in order with no completed-effect replay.
6. G2: started-but-unacknowledged external URI is not automatically repeated; no false exactly-once guarantee.
7. G2: failed navigation emits UI-safe failure and remaining batch continues.
8. G2: cancellation at the commit boundary never leaves a committed state patch without accepted effects; closed session refuses further accepts/acks.
9. Generic app and host tests remain required. Android isolated retention behavior previously observed; Koin integration and Web actual viewport teardown/iOS lifecycle are unproven.

## Scope and authorization
- **Allowed next work:** focused G1-B/G2-B regression test source preparation and execution; exact API signature review; evidence-driven, file-by-file implementation diff proposal.
- **Not authorized:** production Kotlin changes, any new production abstraction, retroactive modification of action/navigation wire contracts, backend edits, new branch, or broad refactors.
- Existing G2-A tests in `DynamicStoreTest.kt` passed `:feature:dynamic:jvmTest` on owner's Windows host. They do not prove multi-effect partial continuation or Koin retention.
- Honor the user workflow DISCUSS -> RESEARCH -> DECIDE -> PLAN -> FREEZE -> IMPLEMENT (separately approved) -> TEST -> REVIEW -> FREEZE.
