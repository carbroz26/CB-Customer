# CB-Partner — Project Tracker

**Status:** ACTIVE
**Scope:** Frontend repository only
**Last Updated:** 2026-10-04
**Purpose:** Single execution ledger for cross-session recovery and work status.

## Current Project State

| Field | Current value |
|---|---|
| Phase | Dynamic UI vertical slice implementation |
| Current module | `:feature:dynamic` + supporting `:app/:navigation/:data/:network` boundaries |
| Current work item | DYNAMIC-UI-001 — Dynamic UI vertical slice |
| Workflow | IMPLEMENTATION IN PROGRESS |
| Branch | `feature/dynamic-ui` |
| Discussion | DYNAMIC-01 → DYNAMIC-07 FROZEN |
| Plan | PLAN FROZEN / IMPLEMENTATION AUTHORIZED |
| Latest JVM verification | BUILD SUCCESSFUL — 2026-10-04 |
| Blocker | No test/build blocker currently; platform verification and remaining Dynamic implementation are pending |
| Next action | Continue Dynamic implementation/testing; then Login → OTP integration verification |

## Base Architecture Register

| ID | State | Notes |
|---|---|---|
| BASE-ARCH-001 | DECISION_FROZEN | Project/Gradle foundation |
| BASE-ARCH-002 | DECISION_FROZEN | Responsibilities/dependency direction |
| BASE-ARCH-003 | DECISION_FROZEN | Module granularity |
| BASE-ARCH-004 | DECISION_FROZEN | Core technical foundation |
| BASE-ARCH-005 | DECISION_FROZEN | Navigation/Application composition |
| BASE-ARCH-006 | DECISION_FROZEN | DI/Application composition |
| BASE-ARCH-007 | DECISION_FROZEN | Feature/Pure Store architecture |
| BASE-ARCH-008 | DECISION_FROZEN | Bootstrap/startup flow |
| BASE-ARCH-009 | DECISION_FROZEN | Application/platform boundary |
| BASE-ARCH-010 | DECISION_FROZEN | Startup lifecycle |
| BASE-ARCH-011 | DECISION_FROZEN | Startup failure/recovery |
| BASE-ARCH-012 | DECISION_FROZEN | Startup state/bootstrap contract |
| BASE-ARCH-013 | DECISION_FROZEN | Bootstrap data flow/layer ownership |
| BASE-ARCH-014 | CODE_FROZEN | Foundation implementation verified/accepted |
| BASE-ARCH-015 | DECISION_FROZEN | Runtime dependency/technical contract |
| BASE-ARCH-016 | DECISION_FROZEN | Koin/DI implementation contract |
| BASE-ARCH-017 | CODE_FROZEN | Runtime infrastructure verified/review accepted |

Detailed frozen record: `docs/architecture/BASE-ARCHITECTURE.md`

## Dynamic Discussion Register

| ID | State | Durable record |
|---|---|---|
| DYNAMIC-01 | FROZEN | Dynamic container/destination lifecycle |
| DYNAMIC-02 | FROZEN | Screen/template/component/section/group/element hierarchy |
| DYNAMIC-03 | FROZEN | Registry architecture |
| DYNAMIC-04 | FROZEN | Layout/visual/rendering direction |
| DYNAMIC-05 | FROZEN | Definition/registry/recursive renderer |
| DYNAMIC-06 | FROZEN | Actions/state/references/request/errors/destination |
| DYNAMIC-07 | FROZEN | Runtime validation/compatibility/fallback/recovery/observability |

Canonical record: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`

## Dynamic Implementation Register

| Area | State | Current evidence / source |
|---|---|---|
| `DynamicDestination` | IMPLEMENTED | `domain/model/dynamic/DynamicModels.kt` |
| Navigation state | IMPLEMENTED | `navigation/DynamicNavigationState.kt` |
| Dynamic container | IMPLEMENTED | `feature/dynamic/DynamicContainer.kt` |
| Dynamic Store | IMPLEMENTED | `feature/dynamic/store/DynamicStore.kt` |
| Load screen use case | IMPLEMENTED | `LoadDynamicScreenUseCase.kt` |
| Execute action use case | IMPLEMENTED | `ExecuteDynamicActionUseCase.kt` |
| Registry | IMPLEMENTED | `DynamicRegistry.kt` |
| Renderer | IMPLEMENTED | `DynamicRenderer.kt` |
| Initial definitions | IMPLEMENTED | `StackTemplate`, `StackComponent`, `StackSection`, `StackGroup`, `TextElement`, `ImageElement` |
| Dynamic repository | IMPLEMENTED | `DynamicRepositoryImpl.kt` |
| Dynamic remote source | IMPLEMENTED | `DynamicRemoteDataSource.kt` |
| DTO / mapper | IMPLEMENTED | `DynamicResponseDto.kt`, `DynamicResponseMapper.kt` |
| Shared network boundary | IMPLEMENTED / JVM VERIFIED | `RequestHandler`, `HttpClientBuilder`, `NetworkModule` |
| Bootstrap → DynamicDestination | IMPLEMENTED / TESTED | `BootstrapMapper.kt` |
| DYNAMIC-07 fallback/recovery/observability | PARTIAL / PENDING | Discussion frozen; complete production implementation remains |
| Login → OTP integration | CONTRACT FROZEN / VERIFICATION PENDING | Backend-shaped contract recorded in Master State |
| Dashboard | PENDING | Must remain backend-driven; no hardcoded screen architecture |

## Latest Verification Record — 2026-10-04

Commands:

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
54 actionable tasks: 15 executed, 39 up-to-date
```

The previous `BootstrapRemoteDataSourceTest.nonSuccessfulHttpStatusIsMapped` failure is resolved.

## Login → OTP Status

### Login destination

```text
screenId      = partner_login
templateId    = tpl_7K2M9Q
templateType  = form_template
endpoint      = /api/v1/partner/screen/auth_login
method        = GET
authentication = NONE
```

### OTP destination

```text
screenId      = partner_otp
templateId    = tpl_P6X8N3
templateType  = form_template
endpoint      = /api/v1/partner/screen/auth_otp
method        = GET
authentication = NONE
```

### OTP actions

```text
POST /api/v1/partner/auth/send_otp
  phoneNumber = $context(authFlow.phoneNumber)
  deviceId    = $context(deviceId)
  responseMode = none

POST /api/v1/partner/auth/verify_otp
  challengeId = $response(data.challengeId)
  phoneNumber = $context(authFlow.phoneNumber)
  otp         = $binding(otp)
  deviceId    = $context(deviceId)
  responseMode = destination
```

## Remaining Work Register

| Priority | Work | State |
|---|---|---|
| 1 | Reconcile historical architecture wording with current `:network/:app/:webApp` topology | OPEN |
| 2 | Complete request metadata/auth integration through frozen runtime/network boundary | OPEN |
| 3 | Complete Dynamic state/reference/action behavior required by actual backend contract | IN PROGRESS |
| 4 | Complete DYNAMIC-07 fallback/recovery/observability implementation | OPEN |
| 5 | Login → Send OTP → OTP → Verify OTP integration tests | OPEN |
| 6 | Full backend-driven navigation/back/replace verification | OPEN |
| 7 | Android verification | OPEN |
| 8 | iOS/macOS-hosted verification | OPEN |
| 9 | Web/Wasm verification | OPEN |
| 10 | Literal plan-to-source compliance review | OPEN |
| 11 | Final Dynamic feature review and code freeze | OPEN |

## Explicitly Closed / Do Not Reopen

- BASE-ARCH-014 implementation.
- BASE-ARCH-017 implementation.
- DYNAMIC-01 through DYNAMIC-07 discussion decisions.
- Previously resolved Bootstrap HTTP-status test failure.

Reopening any frozen decision requires the documented reopening process and explicit owner direction.

## Trello

Authoritative board: **CB-Partner — Frontend**

Workflow:

```text
BACKLOG → TO DO → READY → IN PROGRESS → DONE
```

Trello tracks execution status; architecture and implementation contracts remain in repository documents.

## Recovery Algorithm

1. Read `AI_START_HERE.md`.
2. Read `14-PROJECT-MASTER-STATE.md`.
3. Read this Tracker.
4. Read Current Status.
5. Read the relevant frozen feature discussion.
6. Read the frozen implementation plan/status.
7. Inspect Git state.
8. Reconcile contradictions before continuing.
9. Determine exactly one next valid action.
10. Do not implement outside the current authorized feature plan.

## Documentation Authority

- Current cross-session state: `docs/project-management/14-PROJECT-MASTER-STATE.md`
- Current project status: `docs/project-management/04-CURRENT-STATUS.md`
- Base architecture: `docs/architecture/BASE-ARCHITECTURE.md`
- Current architecture reconciliation: `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
- Dynamic discussion: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
- Dynamic implementation plan: `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`
- Dynamic implementation status: `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`
- Gradle: `docs/build/GRADLE-SETUP-AND-WORKFLOW.md`

Historical chat is not an implementation source.
