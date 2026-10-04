# CB-Partner — Project Tracker

**Status:** ACTIVE  
**Scope:** Frontend repository only  
**Last Updated:** 2026-10-04  
**Purpose:** Single execution ledger for cross-session recovery and work status.

## Current Project State

| Field | Current value |
|---|---|
| Phase | Dynamic UI vertical slice implementation |
| Current module | `:feature:dynamic` + `:feature:splash` + supporting `:app/:navigation/:data/:network` boundaries |
| Current work item | DYNAMIC-UI-001 — Dynamic UI vertical slice |
| Workflow | IMPLEMENTATION IN PROGRESS |
| Branch | `feature/dynamic-ui` |
| Discussion | DYNAMIC-01 → DYNAMIC-07 FROZEN |
| Plan | PLAN FROZEN / IMPLEMENTATION AUTHORIZED |
| Latest JVM verification | BUILD SUCCESSFUL — 2026-10-04 |
| Blocker | No compile/test blocker; architecture/runtime gaps remain explicitly tracked |
| Next action | Implement Bootstrap Option B completely, then verify shared DynamicDestination runtime |

## Dynamic Discussion / Reconciliation Register

| Record | State | Purpose |
|---|---|---|
| DYNAMIC-01 → DYNAMIC-07 | FROZEN | Complete Dynamic architecture discussion |
| `03-FINAL-RECONCILIATION-2026-10-04.md` | FROZEN | Final Bootstrap/navigation/error-model reconciliation |
| Bootstrap Option B | FROZEN / IMPLEMENTATION REQUIRED | Repository + remote-source layering |
| Canonical DynamicDestination | FROZEN | Shared destination for Bootstrap/API/action sources |
| Network exception taxonomy | FROZEN / IMPLEMENTATION REQUIRED | Precise transport/HTTP/protocol classification |

## Dynamic Implementation Register

| Area | State |
|---|---|
| `DynamicDestination` | IMPLEMENTED |
| Navigation state | IMPLEMENTED |
| Dynamic container | IMPLEMENTED |
| Dynamic Store | IMPLEMENTED / FINAL REVIEW PENDING |
| Dynamic use cases | IMPLEMENTED / PACKAGE CLEANUP PENDING |
| Registry | IMPLEMENTED |
| Renderer | IMPLEMENTED |
| Initial definitions | IMPLEMENTED |
| Dynamic repository | IMPLEMENTED |
| Dynamic remote source | IMPLEMENTED |
| Dynamic DTO / mapper | IMPLEMENTED |
| Shared network boundary | IMPLEMENTED / JVM VERIFIED |
| Bootstrap API contract | KNOWN / DOCUMENTED |
| Bootstrap → DynamicDestination mapping | BASELINE IMPLEMENTED / RUNTIME VERIFICATION PENDING |
| Bootstrap repository | REQUIRED / NEXT |
| Bootstrap repository implementation | REQUIRED / NEXT |
| Bootstrap remote source | REQUIRED / NEXT |
| `DefaultApplicationBootstrap` | TRANSITIONAL / MUST BE REMOVED OR REWORKED |
| `StartupConfig` / `NextScreenConfig` / standalone `BootstrapConfig` | LEGACY / MUST BE REMOVED FROM DOMAIN |
| Bootstrap DI/tests | REQUIRED |
| Network exception taxonomy | FROZEN / IMPLEMENTATION PENDING |
| DYNAMIC-07 fallback/recovery/observability | PARTIAL / PENDING |
| Login → OTP integration | CONTRACT FROZEN / VERIFICATION PENDING |
| Dashboard | PENDING / BACKEND-DRIVEN |

## Bootstrap / Splash Contract

Splash and Bootstrap are one startup flow:

```text
Splash UI / Store
  ↓
ApplicationBootstrap
  ↓
BootstrapRepository
  ↓
BootstrapRepositoryImpl
  ↓
BootstrapRemoteDataSource
  ↓
RequestHandler
  ↓
BootstrapMapper
  ↓
BootstrapOutput
  ↓
DynamicDestination
```

Actual request:

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

## Canonical Navigation Contract

```text
Bootstrap API nextScreen ───────┐
Dynamic API nextScreen ─────────┼──→ DynamicDestination → DynamicNavigationState
Dynamic action destination ─────┘
```

One destination type, one navigation mechanism, one back-stack representation.

No `BootstrapDestination`, `NextScreenConfig`, or action-specific destination model.

## Network Exception Contract

```text
BadRequestException
UnauthorizedException
ForbiddenException
NotFoundException
ConflictException
ValidationException
RateLimitException
ServerException
NetworkConnectionException
TimeoutException
SerializationException
ProtocolException
UnknownNetworkException
```

HTTP, connection, timeout, serialization and protocol failures must remain distinguishable. Business/API outcomes are not automatically transport exceptions.

## Latest Verification Record — 2026-10-04

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Result: **BUILD SUCCESSFUL — 54 actionable tasks: 15 executed, 39 up-to-date**.

## Remaining Work Register

| Priority | Work | State |
|---:|---|---|
| 1 | Implement BootstrapRepository + BootstrapRepositoryImpl + BootstrapRemoteDataSource | OPEN / NEXT |
| 2 | Remove/rework legacy Bootstrap domain models and direct DefaultApplicationBootstrap path | OPEN |
| 3 | Update Bootstrap DI and tests | OPEN |
| 4 | Verify Bootstrap → DynamicDestination runtime | OPEN |
| 5 | Move Dynamic feature use cases to feature `usecase` package | OPEN |
| 6 | Complete request metadata/authentication | OPEN |
| 7 | Complete frozen Dynamic state/reference/action behavior | IN PROGRESS |
| 8 | Implement network exception taxonomy | OPEN |
| 9 | Complete DYNAMIC-07 fallback/recovery/observability | OPEN |
| 10 | Login → Send OTP → OTP → Verify OTP integration tests | OPEN |
| 11 | Full backend-driven navigation/back/replace verification | OPEN |
| 12 | Android verification | OPEN |
| 13 | iOS/macOS-hosted verification | OPEN |
| 14 | Web/Wasm verification | OPEN |
| 15 | Literal plan-to-source compliance review | OPEN |
| 16 | Final Dynamic review and code freeze | OPEN |

## Explicitly Closed / Do Not Reopen

- BASE-ARCH-014 implementation.
- BASE-ARCH-017 implementation.
- DYNAMIC-01 through DYNAMIC-07 discussion decisions.
- Bootstrap/Splash single-flow decision.
- Bootstrap Option B boundary.
- Canonical shared `DynamicDestination` decision.
- Network exception classification decision.

## Documentation Authority

- Cross-session state: `docs/project-management/14-PROJECT-MASTER-STATE.md`
- Current status: `docs/project-management/04-CURRENT-STATUS.md`
- Current architecture: `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
- Dynamic discussion: `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
- Final reconciliation: `docs/features/dynamic/03-FINAL-RECONCILIATION-2026-10-04.md`
- Dynamic plan: `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`
- Dynamic status: `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`
- Historical base decisions: `docs/architecture/BASE-ARCHITECTURE.md`

Historical chat is not an implementation source.
