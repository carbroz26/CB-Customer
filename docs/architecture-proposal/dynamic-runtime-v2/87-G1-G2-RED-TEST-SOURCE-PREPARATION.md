# 87 — G1/G2 Focused Red-Test Preparation (review only)

**Status: TEST_DESIGN_PREPARED — NO RED TEST EXECUTION, NO PRODUCTION IMPLEMENTATION AUTHORIZATION.**
**Branch:** `architecture/dynamic-runtime-v2-discussion-freeze`
**Baseline:** `5bcdf868e88206939fbde0b9edc1b85ba0444b8b`
**Authority:** frozen 51 W4 and 85 DYNAMIC-SESSION-LIFECYCLE-001. Android retention evidence from owner: same identity across detach/attach, one onRetire per explicitly removed content. This is isolated probe evidence, not Koin integration evidence.

## Source verified before defining tests

| Source | Present behavior | Missing coverage |
|---|---|---|
| `app/.../App.kt` | `koinApplication` in `remember(runtimeMetadataProvider, apiBaseUrl)`; `DisposableEffect(koin)` closes graph | active graph and retained session isolation |
| `app/.../ApplicationContent.kt` | `SplashStore` remembered; Success builds `DynamicContent`; `DynamicNavigationState`, `DynamicRuntimeState`, unlimited `Channel` remembered | retained handoff; pending effects after detach; partially-consumed batches |
| `feature/dynamic/.../DynamicStore.kt` | `applyActionResult` runs `runtimeState.setAll` then suspending effect callback; store close cancels scope | cancellation between state patch and accepted effect handoff |
| `feature/dynamic/.../DynamicContainer.kt` | store `remember(entry)` and close on `DisposableEffect` | rebuilt store must use fresh DI graph |
| `feature/splash/.../SplashStore.kt` | bootstrap success holds `BootstrapOutput`, diagnostic ID; store close cancels scope | bootstrap success handoff without retaining store |
| `DynamicStoreTest.kt` | existing `retainedStateIsCommittedBeforeRequestNavigationEffect`, `recreatedStoreRestoresBindingsFromSameNavigationEntry` | do not duplicate either test |
| `DynamicNavigationStateTest.kt` | existing identity/entry/pop/presentation tests | do not duplicate navigation authority tests |

## Test ordering and genuinely testable expectations

**Group G1-A — isolated host proof: already manually observed; no duplicate runtime probe.**
- Existing `RetainCompatibilityProbe.kt`: retained identity survives Android detach/reattach; removal invokes onRetire once. Web button/resize/refresh behaviors observed. No claim regarding Koin graph, iOS, actual Web ComposeViewport recreation, process death.

**Group G1-B — integration regressions, blocked until narrow `DynamicSession` public contract reviewed.**
1. `successfulBootstrapHandoffSurvivesHostReattach`: first bootstrap success creates session once; on host attach with retained session, bootstrap repository call count unchanged and diagnosticSessionId/destination preserved. Separate test for failure/retry; no success created prematurely.
2. `newEntryStoreUsesCurrentGraphAfterReattach`: use fake dependency instances tagged `graph-1`/`graph-2`, close first fake, recreate host, construct a new Store with `graph-2`; assert no old dependency invocation or retained graph reference. A fake closed-graph test cannot prove Compose integration; pair with host test.
3. `retirementClosesSessionOnceNotOnTemporaryDetach`: detach + reattach produces no session close; final removal closes exactly once; repeated close idempotent; graph closes via its own composition owner.
4. `separateSessionsDoNotShareState`: fresh session has fresh navigation/runtime IDs and no carried values. No persisted dynamic values.

**Group G2-A — existing code tests that can be added before production API (test-only).**
5. `completedActionDeliversOrderedWholeBatch`: leverage existing DynamicStore callback and action executor fixtures; ensure one callback containing complete immutable-order result, never one callback per effect; do not duplicate existing state-before-effect check.
6. `cancelledInFlightActionDeliversNoEffects`: a deferred repository completes only after test-controlled suspension; close store first; assert zero callback and zero state patch. Distinguish cancellation *before* commit from *after* commit.
7. `acceptedBatchSurvivesSourceStoreClose`: callback copies accepted batch into a test-owned sink; close Store; assert sink still has it. This tests ownership handoff only, not consumer detach.

**Group G2-B — currently impossible to express against real session API (prepare assertion plans, do not add compile-breaking placeholders).**
8. `acceptedBatchSurvivesConsumerDetach`: submit batch A,B,C, detach consumer before dequeue, reattach; applied order A,B,C once.
9. `partialBatchResumesWithoutReplayingCompleted`: apply A, detach, reattach; apply B,C only; record cursor by effect position, not a new navigation stack.
10. `failedNavigationEffectContinuesRemainingBatch`: reject first transition with UI-safe failure, still apply next effect; do not leak exception or URI.
11. `ambiguousExternalUriDoesNotAutomaticallyRetry`: invocation-started then detach before confirmation; on reattach *do not dispatch again*; document potentially skipped outcome. This is not exactly-once.
12. `sessionRetirementDiscardsUnappliedBatches`: close session; no later consumer execution.
13. `stateCommitAndBatchAcceptanceHaveOneCancellationBoundary`: test scheduler cancels at each suspension point; either no successful commit or patch with accepted batch; never committed patch without accepted batch. Specify exact non-suspending critical section or other proven atomicity *after* reviewing code; do not assume existing callback passes this.

## API/design decisions that tests must constrain, not pre-decide

- Session owns existing `DynamicNavigationState`, `DynamicRuntimeState`, bootstrap destination/diagnostic ID, minimal ordered effect acceptance/continuation, and idempotent retirement only.
- No Koin, SplashStore, DynamicStore, UI UriHandler, navigation executor or activity reference retained in session.
- Active composition owns Koin and effect consumer. Entry composition owns its Store. No second navigation authority or manager layer.
- Consumer detach during an external side effect has an ambiguous completion boundary; prefer no automatic retry, never claim exactly-once.
- No new production class or public method signature approved by this test-design record.
- Existing `Channel.UNLIMITED` and `LaunchedEffect` are composition-owned; moving channel into session alone does not establish atomicity or partial-batch continuation.

## Narrow proposed test file targets and review gate

- Existing `feature/dynamic/src/commonTest/.../store/DynamicStoreTest.kt`: only G2-A tests 5–7 after fixtures are checked.
- New `app/src/commonTest/.../DynamicSessionLifecycleTest.kt` and `DynamicSessionEffectsTest.kt`: G1-B/G2-B **only after minimal API signature approval**; code referencing unimplemented API would fail compilation rather than provide meaningful failing behavioral regressions.
- Host/manual Android/Web/iOS tests as independent acceptance, not substituted by fake unit tests.

**Review sequence:** approve target assertions -> implement test-only G2-A -> capture JVM results -> agree minimal session API and G1-B/G2-B test harness -> add focused red tests -> inspect exact production diff -> obtain explicit production implementation authorization. Do not edit production Kotlin while reviewing. Do not touch backend or untracked local trace files.
