# 90 — G1-B/G2-B Executable Red-Test Gate and Exact Production Diff Review

**Status: TEST_CASES_PREPARED / DIFF_REVIEW_CANDIDATE — NOT EXECUTED, PRODUCTION IMPLEMENTATION NOT AUTHORIZED.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `ece37d12e6ddd9acf11cde2101d9be5cf0adae57`
**Authority:** 51, 85, 87, 88 and owner-frozen 89 (D1–D4).

## Feasibility check from actual project source

- No `DynamicSession.kt` yet; no session claim/dispatch/ack API exists. Production-coupled G1-B/G2-B Kotlin tests referring to nonexistent methods would **fail compilation**, not serve as meaningful behavioral red tests. Do not commit unresolved Kotlin test placeholders into the build.
- `app/build.gradle.kts` declares `commonMain` dependencies but no explicit `commonTest` block; inspect existing convention test setup before adding tests or test dependencies.
- `DynamicStore.applyActionResult` presently calls non-suspending `runtimeState.setAll` and then suspending `deliverEffects` or `deliverCorrelatedEffects`; the cancellation-atomicity rule D1 is NOT established.
- Existing `DynamicActionResult` concrete outputs provide at most one effect today, although the `effects: List<DynamicActionEffect>` contract is generic. Multi-effect fixture must exercise the queue at session API level, not invent an unsupported backend action or fake actual production sequencing.
- Koin graph in `App.kt` is `remember` + `DisposableEffect(koin).onDispose { koin.close() }`. `ApplicationContent` remembers `SplashStore`, session/navigation are not yet retained.
- G2-A three new tests passed via owner's `:feature:dynamic:jvmTest` run. They do NOT test session consumer loss, partial batches, or Koin.

## Frozen behavior-to-test matrix

| ID | Deterministic setup / action | Required assertion |
|---|---|---|
| G1B-01 | Fake bootstrap result -> handoff; detach/reattach same retained session | bootstrap invoked once; same destination, diagnostic ID, nav/runtime identities |
| G1B-02 | Host graph A closed, host graph B started; new Store created for retained entry | dependencies only from B; no reference/call to closed A; old Store canceled/closed |
| G1B-03 | Attach -> detach -> attach -> final retire -> close again | no early close; final close once; post-close delivery rejected |
| G1B-04 | Session A stores runtime nested values and has pushed nav; create unrelated session B | B begins fresh; no retained bindings/state shared |
| G2B-01 | Commit synthetic ordered batch [A,B,C] -> detach before claim -> new consumer claims | A,B,C in order, once |
| G2B-02 | Consumer 1 claims A but has not marked dispatch-started -> detach -> consumer 2 | consumer 2 may reclaim A; stale consumer 1 cannot mark or acknowledge |
| G2B-03 | Apply/ack A in [A,B,C], detach -> consumer 2 | B,C only, in order |
| G2B-04 | Mark external URI B dispatch-started -> detach before ack -> reattach | B is not automatically dispatched again; later C continues; uncertainty recorded safely |
| G2B-05 | Consumer 1 detached and consumer 2 active, then consumer 1 attempts ack | stale acknowledgement rejected, cursor not moved |
| G2B-06 | First effect fails transition; acknowledge safe failure | next ordered effect proceeds; error is UI-safe |
| G2B-07 | Pending work exists -> final session close -> try claiming/accepting | no application after close, pending discarded |
| G2B-08 | Controlled coroutine cancellation before success and between state-patch/queue stages | no committed state without accepted batch; no accidental effects from canceled action |

**Batch test fixture:** Use existing `DynamicActionEffect.Navigate`, `Dismiss`, or `ExternalUri` instances in manually submitted test batches; avoid triggering actual `openUri`. Assert mutation count/order via fake consumer. Do not log raw URIs.

**Concurrency:** Deterministic scheduler/explicit barriers; no sleeps. State+batch logical commit must serialize simultaneous successful results. Invocation marker precedes external side effect, but that cannot guarantee it completes.

## Minimal API signature constraints to review BEFORE compiling tests

1. Constructor takes bootstrap `DynamicDestination` and diagnostic session ID; exposes existing navigation and runtime states.
2. A non-suspending logical commit accepts **computed state patch + ordered effect batch**, optionally a trace, without running action handlers. Exact argument type is subject to cross-module dependency review.
3. Explicit active-consumer ownership/lease, claim/release or detach, dispatch-start marker, terminal ack, and close; stale consumer cannot mutate progress.
4. Close is idempotent; producer actions run before commit outside session; newly created entry Stores take only active Koin dependencies.
5. Keep any claim/ack/progress types internal/private and minimal. No new module, queue manager or navigation authority.
6. Batch cursor must preserve order across consumer loss; past attempted external effect must not automatically redispatch.
7. Decide response for acceptance after close and stale claim (return controlled result or fail safely) in concrete Kotlin API review; do not scatter exceptions in UI.
8. **Important**: D1 does not imply that unrelated StateFlow subscribers have a transactional multi-object snapshot. Tests must assert observable accepted effects for each committed patch and absence of coroutine suspension/race in the commit region; prove serialization on actual execution model.

## File-by-file proposed implementation diff (REVIEW ONLY)

| Path | Proposed necessity | Limit |
|---|---|---|
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/DynamicSession.kt` NEW | Retained logical owner, existing nav/runtime state, commit/effect cursor, idempotent close | No DI, action execution, URI, new controller, business IDs |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/App.kt` | Stable session retention boundary distinct from current Koin graph; proof of cleanup | Koin remains active composition-scoped, no global singleton |
| `app/src/commonMain/kotlin/com/carbroz/cbpartner/app/ApplicationContent.kt` | Bootstrap-once handoff, fresh DI graph, one active effect consumer | No second orchestration abstraction |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/store/DynamicStore.kt` | Replace suspending callback split commit with approved non-suspending session commit handoff; keep pure Store/Executor | No request/navigation wire changes |
| `feature/dynamic/src/commonMain/kotlin/com/carbroz/cbpartner/feature/dynamic/DynamicContainer.kt` | Touch only if Store recreation test demonstrates stale captured factory | No retaining closed Store |
| `app/src/commonTest/kotlin/com/carbroz/cbpartner/app/DynamicSessionLifecycleTest.kt` NEW | G1-B tests once API exists | Match actual available test support |
| `app/src/commonTest/kotlin/com/carbroz/cbpartner/app/DynamicSessionEffectsTest.kt` NEW | G2-B tests once API exists | No fake production action mechanics |
| `feature/dynamic/src/commonTest/.../DynamicStoreTest.kt` | Extend cancellation race boundary test only if new Store signature demands | Preserve passing G2-A tests |
| Platform host files | Deferred, only if actual host failure proven | iOS requires macOS build/run |

## Blocking review before separate implementation approval
- Decide exact `commit` type and cancellation synchronization boundary across app ↔ feature/dynamic: currently both app and Store depend on each other only at caller-wiring level. A session-held `DynamicActionResult` is available in app (app depends on feature/dynamic); Store can use an injected callback without importing app classes.
- Decide single-consumer lease representation, and how detach/reattach notifications are delivered without dropping accepted work.
- Decide how a new active Koin graph is created on reattach when a logical session already exists; successful bootstrap store must not survive.
- Confirm `app/commonTest` convention and whether new test dependencies are necessary (do not add proactively).
- Explicitly review one tricky condition: if an effect cursor skips ambiguous dispatch, consumer must still move past it without marking success; no replay.
- Reassess whether G3 entry snapshots need to be integrated to meet full session recreation acceptance later. G3 is a separate slice; do not falsely claim real screen continuity from this G1/G2 slice.

## Execution status
- Prepared: matrix, proposed source diff, API constraints.
- Not implemented or run: G1-B/G2-B actual Kotlin tests, because session API is absent; no claim of red JVM failures.
- No production Kotlin edits, no new branch, no backend edits, no local untracked file changes.

**Next approval boundary:** owner reviews exact API signatures + test placement, then authorizes a narrow scaffold/test slice if desired; actual production implementation requires separate explicit approval under frozen doc 89.
