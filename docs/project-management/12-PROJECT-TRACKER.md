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
| Unified destination contract | DYNAMIC-08 FROZEN |
| Plan | PLAN FROZEN / IMPLEMENTATION AUTHORIZED |
| Latest JVM verification | BUILD SUCCESSFUL — 2026-10-04 |
| Blocker | No compile/test blocker; remaining work is implementation/runtime verification |
| Next action | Complete Bootstrap Option B, then verify shared DynamicDestination runtime |

## Dynamic Discussion / Reconciliation Register

| Record | State | Purpose |
|---|---|---|
| DYNAMIC-01 → DYNAMIC-07 | FROZEN | Complete Dynamic architecture discussion |
| DYNAMIC-08 | FROZEN | Unified four-field destination contract |
| `03-FINAL-RECONCILIATION-2026-10-04.md` | FROZEN | Bootstrap/navigation/error-model reconciliation |
| Bootstrap Option B | FROZEN / IMPLEMENTATION REQUIRED | Repository + remote-source layering |
| Canonical DynamicDestination | FROZEN | Shared destination for Bootstrap/API/action sources |
| Network exception taxonomy | FROZEN / IMPLEMENTATION REQUIRED | Precise transport/HTTP/protocol classification |

## Dynamic Implementation Register

| Area | State |
|---|---|
| `DynamicDestination` | IMPLEMENTED / VERIFIED |
| Navigation state | IMPLEMENTED |
| Dynamic container | IMPLEMENTED |
| Dynamic Store | IMPLEMENTED / FINAL REVIEW PENDING |
| Dynamic use cases | IMPLEMENTED / PACKAGE CLEANUP PENDING |
| Registry | IMPLEMENTED |
| Renderer | IMPLEMENTED |
| Initial definitions | IMPLEMENTED |
| Dynamic repository | IMPLEMENTED |
| Dynamic remote source | IMPLEMENTED |
| Dynamic DTO / mapper | IMPLEMENTED / JVM VERIFIED |
| Shared network boundary | IMPLEMENTED / JVM VERIFIED |
| DYNAMIC-08 destination mapping | IMPLEMENTED / JVM VERIFIED |
| Bootstrap API contract | KNOWN / DOCUMENTED |
| Bootstrap → DynamicDestination mapping | IMPLEMENTED / RUNTIME VERIFICATION PENDING |
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
Bootstrap startup destination ───┐
Dynamic API destination ─────────┼──→ DynamicDestination → DynamicNavigationState
Dynamic action destination ──────┘
```

Canonical destination:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

Request method/authentication are request metadata, not destination identity.

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

Compile verification:

```powershell
.\gradlew.bat `
  :domain:compileKotlinJvm `
  :data:compileKotlinJvm `
  :feature:splash:compileKotlinJvm `
  :feature:dynamic:compileKotlinJvm `
  :navigation:compileKotlinJvm `
  :app:compileKotlinJvm `
  --rerun-tasks
```

Result: **BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**.

JVM test verification:

```powershell
.\gradlew.bat `
  :core:jvmTest `
  :domain:jvmTest `
  :data:jvmTest `
  :navigation:jvmTest `
  :feature:splash:jvmTest `
  :feature:dynamic:jvmTest `
  :app:compileKotlinJvm `
  --rerun-tasks
```

Result: **BUILD SUCCESSFUL — 62 actionable tasks: 62 executed**.

Known Gradle warnings on Windows are non-fatal: iOS simulator tests require macOS and Android host-test configuration is not enabled for these commonTest sources.

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
- DYNAMIC-01 through DYNAMIC-08 discussion decisions.
- Bootstrap/Splash single-flow decision.
- Bootstrap Option B boundary.
- Canonical shared four-field `DynamicDestination` decision.
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
