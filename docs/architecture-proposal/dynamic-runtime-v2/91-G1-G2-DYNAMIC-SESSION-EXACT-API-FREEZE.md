# 91 — G1/G2 DynamicSession Exact Kotlin API Freeze

**Status: API_SIGNATURES_FROZEN — OWNER ACCEPTED EXACT-API REVIEW AND FREEZE WORKFLOW ON 2026-10-17.**
**NO SCAFFOLD, RED-TEST CODE, OR PRODUCTION KOTLIN IMPLEMENTATION AUTHORIZED BY THIS DOCUMENT.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `5753e882e1171e3e59ba238ecd609b4bc3208c28`
**Authority:** 51 W4, 85 session freeze, 89 approved D1–D4, 90 test/diff review.

## Exact proposed Kotlin-visible contract for approved test scaffold

These are the selected signatures to implement/test in a separately authorized narrow slice, not existing source. Nested types intentionally avoid managers/public abstraction expansion.

```kotlin
internal class DynamicSession(
    val diagnosticSessionId: String,
    initialDestination: DynamicDestination,
) {
    val navigationState: DynamicNavigationState
    val runtimeState: DynamicRuntimeState

    // Monotonic in-memory wakeup signal; NEVER a replacement effect queue.
    val workRevision: StateFlow<Long>

    // One serialized, non-suspending commit of state patch and copied batch.
    // false if the session is already retired; empty effects are valid.
    fun commit(
        changes: Map<String, DynamicValue>,
        effects: List<DynamicActionEffect>,
        trace: TraceContext? = null,
    ): Boolean

    // One active consumer, a new lease each time. Previous lease invalidated.
    fun attachConsumer(): Long
    fun claimNext(leaseId: Long): ClaimedEffect?
    fun markDispatchStarted(claim: ClaimedEffect): Boolean
    fun acknowledge(claim: ClaimedEffect, outcome: EffectOutcome): Boolean
    fun detachConsumer(leaseId: Long)
    fun close()

    // Nested immutable claim token; no side effects executed by session.
    data class ClaimedEffect(
        val leaseId: Long,
        val batchId: Long,
        val effectIndex: Int,
        val effect: DynamicActionEffect,
        val trace: TraceContext?,
    )
    enum class EffectOutcome { Applied, Rejected }
}
```

**Visibility:** `internal` within the app module; Store does **not** import this class. Instead the app injects a non-suspending callback that forwards `result.stateChanges`, `result.effects`, and optional trace to `commit`. Exact Store callback compatibility change must be reviewed in its own authorized diff. Trace is correlation only; never log contents of effects, URI, personal dynamic values.

## Lease / cursor semantics

1. `attachConsumer()` creates a strictly increasing in-memory lease and invalidates an old lease. Exactly one active lease; a valid claimant must own it. On attach, revision publication wakes listener for pending work.
2. `commit(...)` validates that session is open, copies the ordered effect list and commits changes with append in one serialized non-suspending logical critical region. A batch with no effects still applies state changes. A closed session returns `false` without updating either state or queue. Commit linearization order governs competing origins. Do not assume unrelated StateFlow readers get a combined atomic snapshot.
3. `claimNext(leaseId)` returns the **head** uncompleted item only, and reserves it for that lease. No second item may be claimed while the current one remains unresolved. Empty returns `null`; invalid or closed lease must fail safely without effect.
4. `markDispatchStarted(claim)` is accepted exactly once for the currently reserved token and active lease, **before** the app consumer calls navigation/URI; returns `false` for stale, closed, already-started or invalid claims. Marker and platform invocation cannot form a truly atomic external side effect.
5. `acknowledge(claim, Applied|Rejected)` succeeds once for a started claim from current lease; advances cursor, permits next effect and records generic safe failure for rejection (in app UI, no exception or URI echo). Duplicate/stale ack returns `false`, changes nothing.
6. `detachConsumer(leaseId)`: if it matches active lease, revoke it. A reserved-but-not-started effect returns to pending; a started-but-not-acked effect becomes terminal **uncertain** (no replay) and cursor advances so later effects continue. Stale detach does nothing. Uncertainty is not represented as `Applied`.
7. When the final effect of a batch reaches terminal state, remove the batch; completed effects must never replay. `close()` idempotently closes, invalidates lease, discards pending work and rejects later commits/claims/acks. Temporary host detach NEVER calls `close`.
8. `workRevision` increments when work may have become claimable (commit, reattach, ack, detach/uncertain continuation). It is only a wakeup signal; consumer must drain `claimNext` until null, and re-check after subscribe to prevent missed wakeups. No extra app-level queue/channel.
9. No sleep, network or business-screen requirements for G2-B tests. Manual lists of existing DynamicActionEffect variants suffice; supported production results may currently have a single effect.

## G1 host/DI contract linked to API
- Session construction occurs only after successful Bootstrap handoff. Existing retained session takes precedence over constructing another SplashStore on active composition reattach.
- Session stores no Koin, SplashStore, DynamicStore, runtime metadata provider, Activity, UriHandler, renderer or DI-created instances.
- `DynamicNavigationState` is the sole navigation authority. `DynamicRuntimeState` owns values; session coordinates commit ownership, but does not execute actions.
- Compose `retain` + `RetainedEffect` lifetime integration and Koin lifecycle need live proof. Android isolated removal callback observed; Web actual host teardown and iOS remain pending.
- No process-death, browser-refresh or persistence guarantee.

## Red-test requirement and exact review scope
G1-B: Bootstrap once, Koin graph A closed/B used for new Store, detach != retirement and close once, new session isolation.
G2-B: ordered claim/ack with detach, unstarted reclaim, started-but-unacked uncertainty, stale lease rejection, rejection continues, close discards, concurrent commit ordering, cancellation boundary. Use deterministic scheduler/explicit barriers and no real external URI invocation.

**Candidate source files once separately authorized:** NEW `app/src/commonMain/.../DynamicSession.kt`; `app/src/commonMain/.../App.kt`; `ApplicationContent.kt`; minimal `feature/dynamic/.../DynamicStore.kt` callback adjustment; focused `app/src/commonTest/.../DynamicSessionLifecycleTest.kt` and `DynamicSessionEffectsTest.kt` after verifying app test dependencies. `DynamicContainer.kt` only on demonstrated failure. No new Gradle module or branch; backend untouched.

**Critical implementation caveat:** `DynamicRuntimeState.setAll` updates its own StateFlow independently from queue mutation. Exact thread/concurrency mechanism for linearizing `commit` and UI observation is still subject to evidence-driven tests; this contract freezes externally observable API and rules, not an unproven atomicity primitive.

**Next:** user explicitly authorizes a narrow test-scaffold slice (which necessarily introduces the minimal API production scaffold), or reviews alternative API changes. Do not begin that slice without explicit authorization.
