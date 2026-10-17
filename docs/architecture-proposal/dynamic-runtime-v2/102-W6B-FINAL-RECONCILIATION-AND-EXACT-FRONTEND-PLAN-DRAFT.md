# 102 — W6-B Final Source Reconciliation and Exact Frontend Plan (DRAFT)

**State:** OWNER REVIEW REQUIRED; NOT FROZEN; production implementation NOT AUTHORIZED.
**Frontend read baseline:** `86a783cabb32992faefdf6e454efd9e1ccd73dcc`, branch `architecture/dynamic-runtime-v2-discussion-freeze`.
**Backend read baseline:** `carbroz26/carwash-backend` `development` at `f51f8918c834b13731acde2982c6c20647675ec4`.
**Cross-repository matrix:** `101-W6B-ACTION-CROSS-REPO-GAP-MATRIX-DRAFT.md`.
**Policy:** No backend source edits, new backend branch, or PR. Future backend migration is documentation-only now.

## Owner-approved boundaries
- W6-B-STATE-001: Retained session runtime state and target-based element UI state are different responsibilities. Preserve existing `DynamicRuntimeState` and `StateActionHandler`; no target-based adapter/manager until separately jointly frozen.
- W6-B-PRESENT-001: Preserve existing destination-based presentation semantics, but defer new/changed Present wire contract until a real product use case.
- W6-B-DISMISS-001: Preserve presentation-only Dismiss; do not conflate with navigation Pop.
- Bootstrap update/maintenance message is a **separate bootstrap configuration use case**, not a reason to redesign Dynamic Present. Its response contract is not yet frozen.
- Sequence remains one registered inert placeholder (docs 60 final revision and 61).
- W6-A outside-click focus/keyboard dismiss explicitly deferred; working IME Done-dismiss preserved.

## Source/test reconciliation
`DynamicActionExecutorTest` covers registration uniqueness, trace forwarding, independent dispatch, inert sequence, unknown failure, cancellation. `NavigateActionHandlerTest` covers default push, explicit modes, invalid modes and missing destination. `RequestActionHandlerTest` covers resolved references/retain/default response mode, invalid modes and paths, request trace. `DynamicStoreTest` has passed latest-binding (ordered ValueChanged -> Request), state-before-effect and entry binding retention gates. Backend `Action.ts`, `SduiModel.ts` and `Action.spec.ts` contain differences enumerated in doc101; no backend tests executed in this audit. Existing `DynamicSession` has explicit effect leases, state commit before effect and navigation consumer lifecycle handling; preserve without refactor.

## Exact file-by-file frontend implementation proposal
| File | Decision | Rationale |
|---|---|---|
| `feature/dynamic/.../action/DynamicActionExecutor.kt` | **NO EDIT** | One-handler dispatch with cancellation; no recursive sequence |
| `RequestActionHandler.kt` | **NO EDIT** | Current Request/retention supported; backend retain strict-schema inconsistency documented |
| `NavigateActionHandler.kt` | **NO EDIT** | Push/Replace/Pop semantics already covered |
| `PresentActionHandler.kt` | **NO EDIT** | Future wire semantics deferred |
| `DismissActionHandler.kt` | **NO EDIT** | Presentation-only dismiss approved |
| `StateActionHandler.kt` | **NO EDIT** | Current key/value session behavior preserved; target UI state deferred |
| `SequenceActionHandler.kt` | **NO EDIT** | Frozen inert Sequence |
| `ExternalUriActionHandler.kt` | **NO EDIT** | Reference resolution and platform effect established |
| `DynamicActionResult.kt` | **NO EDIT** | Effect list to scalar would touch session safety; no defect proven |
| `DynamicStore.kt` | **NO EDIT** | W6-A latest binding regression PASS |
| `app/.../DynamicSession.kt`, `ApplicationContent.kt` | **NO EDIT** | W4 effect lease/consumer safety |
| `navigation/.../DynamicNavigationState.kt` | **NO EDIT** | Single navigation authority; Present/Pop distinct |
| `data/.../DynamicActionDto.kt`, `DynamicActionMapper.kt` | **NO EDIT** | No backend-unsupported wire adaptation |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/PresentDismissActionHandlerTest.kt` | **PROPOSED NEW TEST ONLY** | Test existing: Present requires destination; Dismiss emits only Dismiss effect, no navigation pop. No new action semantics |
| `feature/dynamic/src/commonTest/kotlin/com/carbroz/cbpartner/feature/dynamic/action/StateActionHandlerTest.kt` | **PROPOSED NEW TEST ONLY** | Assert existing key/value to StateChanged and typed reference resolution; do not claim backend targetId compatibility |
| `navigation/src/commonTest/... existing DynamicNavigationState tests` | **REVIEW EXISTING TEST FIRST** | Add no tests unless exact missing present/dismiss rejection/stack invariants demonstrated |

**Do not create tests for unapproved targetId/presentation UI behavior.** Do not modify source solely to make a proposed test pass without a separately reviewed defect, amended plan, and owner authorization.

## Exact backend migration documentation backlog — NO implementation
1. `src/modules/sdui/engine/src/core/Action.ts`: Request builder has `retain` but strict `SduiModel.ts` request schema omits it; investigate validation paths before proposing schema migration.
2. `src/modules/sdui/engine/tests/Action.spec.ts`: Navigate expectations reference `payload` despite builder returning `destination`; verify test execution and fix through future backend PR only.
3. State targetId/operation/property/value vs session key/value: defer coordinated UI state contract.
4. Present targetId/presentation vs destination-based frontend: defer semantics and lifecycle.
5. Dismiss optional targetId vs active-presentation-only frontend: defer target matching contract.
6. `$response` remains backend-authored but forbidden by frontend V2; inventory actual usage before a proposed backend removal.
7. Sequence remains inert frontend; no backend authoring change now.

## Approval gate and validation
- Ask owner to **approve tests-only W6-B hardening** or leave W6-B as documentation only. This draft itself grants no code authorization.
- If approved: add minimal tests in test files only, then owner runs `./gradlew :feature:dynamic:jvmTest :navigation:jvmTest` (Windows `.\\gradlew.bat`); inspect results and exact diff. No need to rerun Android/Web for test-only change unless dependency/build scope demands.
- If no missing behavior is found, close W6-B as an audit with deferred backend migration notes. Do not invent a production change to justify a workstream.
- All future backend work would occur in fork `carbroz26/carwash-backend` branch `feature/backend-migration`, with upstream development as PR base, only after owner authorization.
