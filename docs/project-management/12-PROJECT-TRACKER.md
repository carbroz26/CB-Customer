# CB-Partner — Project Tracker

**Status:** ACTIVE  
**Scope:** Frontend repository only  
**Last Updated:** 2026-10-06  
**Purpose:** Single execution ledger for cross-session recovery and work status.

## Current Project State

| Field | Current value |
|---|---|
| Phase | Dynamic UI vertical slice — final documentation freeze |
| Current module | `:feature:dynamic` + `:feature:splash` + supporting `:app/:navigation/:data/:network` boundaries |
| Current work item | DYNAMIC-UI-001 — Dynamic UI vertical slice |
| Workflow | DOCUMENTATION FREEZE / FINAL REVIEW |
| Branch | `feature/dynamic-ui` |
| Discussion | DYNAMIC-01 → DYNAMIC-07 FROZEN |
| Unified destination contract | DYNAMIC-08 FROZEN |
| Plan | PLAN FROZEN / IMPLEMENTATION AUTHORIZED |
| Latest JVM verification | BUILD SUCCESSFUL — 2026-10-06 |
| Blocker | No compile/test blocker; implementation/runtime verification complete; documentation reconciliation remains |
| Next action | Complete final Dynamic documentation freeze/reconciliation; do not change production behavior |

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
| Dynamic use cases | ALIGNED / `LoadDynamicScreenUseCase` retained; action execution uses `DynamicActionExecutor` |
| Registry | IMPLEMENTED |
| Renderer | IMPLEMENTED |
| Initial definitions | IMPLEMENTED |
| Dynamic repository | IMPLEMENTED |
| Dynamic remote source | IMPLEMENTED |
| Dynamic DTO / mapper | IMPLEMENTED / FIXTURE-RECONCILED / JVM VERIFIED |
| Shared network boundary | IMPLEMENTED / JVM VERIFIED |
| DYNAMIC-08 destination mapping | IMPLEMENTED / FIXTURE-RECONCILED / JVM VERIFIED |
| Bootstrap API contract | KNOWN / DOCUMENTED |
| Bootstrap → DynamicDestination mapping | IMPLEMENTED / CONTRACT VERIFIED |
| Bootstrap repository | Existing startup boundary remains separately tracked; not reopened by Dynamic freeze |
| Bootstrap repository implementation | Existing startup boundary remains separately tracked; not reopened by Dynamic freeze |
| Bootstrap remote source | Existing startup boundary remains separately tracked; not reopened by Dynamic freeze |
| `DefaultApplicationBootstrap` | Existing startup cleanup remains separately tracked; not part of final Dynamic contract freeze |
| `StartupConfig` / `NextScreenConfig` / standalone `BootstrapConfig` | LEGACY / MUST BE REMOVED FROM DOMAIN |
| Bootstrap DI/tests | REQUIRED |
| Network exception taxonomy | FROZEN / IMPLEMENTATION PENDING |
| DYNAMIC-07 fallback/recovery/observability | PARTIAL / PENDING |
| Login → OTP integration | CONTRACT FROZEN / JVM VERIFIED |
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

## Latest Verification Record — 2026-10-06

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

Result: **BUILD SUCCESSFUL — 52 actionable tasks: 26 executed, 26 up-to-date**.

Known Gradle warnings on Windows are non-fatal: iOS simulator tests require macOS and Android host-test configuration is not enabled for these commonTest sources.

## Remaining Work Register

| Priority | Work | State |
|---:|---|---|
| 1 | Implement BootstrapRepository + BootstrapRepositoryImpl + BootstrapRemoteDataSource | OPEN / NEXT |
| 2 | Remove/rework legacy Bootstrap domain models and direct DefaultApplicationBootstrap path | OPEN |
| 3 | Update Bootstrap DI and tests | OPEN |
| 4 | Verify Bootstrap → DynamicDestination runtime | OPEN |
| 5 | Keep `LoadDynamicScreenUseCase` in feature `usecase`; no action-execution use case | CLOSED |
| 6 | Complete request metadata/authentication | OPEN |
| 7 | Complete frozen Dynamic state/reference/action behavior | IN PROGRESS |
| 8 | Implement network exception taxonomy | OPEN |
| 9 | Complete DYNAMIC-07 fallback/recovery/observability | OPEN |
| 10 | Login → Send OTP → OTP → Verify OTP integration tests | VERIFIED |
| 11 | Full backend-driven navigation/back/replace verification | OPEN |
| 12 | Android verification | OPEN |
| 13 | iOS/macOS-hosted verification | OPEN |
| 14 | Web/Wasm verification | OPEN |
| 15 | Literal plan-to-source compliance review | OPEN |
| 16 | Final Dynamic review and documentation freeze | CURRENT |

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
- Dynamic discussion: `docs/dynamic/DYNAMIC-ARCHITECTURE-FROZEN.md`
- Final reconciliation: `docs/dynamic/DYNAMIC-DISCUSSION-AND-DECISIONS.md`
- Dynamic plan: `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN-FROZEN.md`
- Dynamic status: `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`
- Historical base decisions: `docs/architecture/BASE-ARCHITECTURE.md`

Historical chat is not an implementation source.
