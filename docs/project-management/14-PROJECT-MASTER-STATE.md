# CB-Partner — Project Master State

**Status:** ACTIVE / RECOVERY SOURCE  
**Last Updated:** 2026-10-04  
**Scope:** Frontend repository only

## 1. Current Project State

- Repository: `carbroz26/CB-Partner`
- Active branch: `feature/dynamic-ui`
- Current feature: Dynamic UI vertical slice
- DYNAMIC-01 → DYNAMIC-07: **FROZEN**
- DYNAMIC-08 unified destination contract: **FROZEN**
- Dynamic implementation plan: **PLAN FROZEN / IMPLEMENTATION AUTHORIZED**
- Dynamic implementation: **IN PROGRESS**
- Latest JVM compile/test verification: **BUILD SUCCESSFUL — 2026-10-04**
- BASE-ARCH-014: **CODE FROZEN**
- BASE-ARCH-017: **CODE FROZEN / VERIFIED / REVIEW ACCEPTED**
- No ViewModel.
- Pure Store + MVI/UDF.
- Backend-driven Dynamic navigation/rendering.
- Bootstrap Option B: **FROZEN / IMPLEMENTATION REQUIRED**
- Canonical four-field `DynamicDestination`: **FROZEN / JVM VERIFIED**
- Network exception taxonomy: **FROZEN / IMPLEMENTATION REQUIRED**

## 2. Frozen Base Architecture

Kotlin Multiplatform, Compose Multiplatform, Clean Architecture, Pure Store/MVI/UDF, no ViewModel, multi-module Gradle, convention plugins, Koin, Ktor Client, kotlinx.serialization, Kermit and Navigation 3 remain frozen as recorded by BASE-ARCH-001 → BASE-ARCH-017.

## 3. Startup / Bootstrap Boundary

Splash and Bootstrap are one startup flow.

```text
Platform entry
  ↓
App
  ↓
ApplicationContent
  ↓
Splash UI / Store
  ↓
ApplicationBootstrap                 :domain
  ↓
BootstrapRepository                 :domain
  ↓
BootstrapRepositoryImpl             :data
  ↓
BootstrapRemoteDataSource           :data
  ↓
RequestHandler / HttpClient         :network
  ↓
Bootstrap DTO
  ↓
BootstrapMapper
  ↓
BootstrapOutput                     :domain
  ↓
DynamicDestination
  ↓
DynamicNavigationState
```

### Bootstrap API

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

### Transitional code to remove/rework

```text
DefaultApplicationBootstrap → RequestHandler
```

There must be exactly one production Bootstrap HTTP path through Option B.

### Legacy domain contracts to remove

```text
StartupConfig
NextScreenConfig
standalone BootstrapConfig
```

`BootstrapOutput` is the canonical Bootstrap result and uses the canonical `DynamicDestination` for the startup destination.

## 4. Canonical Dynamic Destination

```text
Bootstrap startup destination ───┐
Dynamic API destination ─────────┼──→ DynamicDestination
Dynamic action destination ──────┘
```

Canonical fields:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

HTTP method and authentication are request metadata, not destination identity.

`templateType` selects rendering only; it never decides business navigation.

## 5. Dynamic Architecture State

Frozen DYNAMIC-01 → DYNAMIC-08 remains authoritative for:

- one permanent Dynamic Container;
- Screen → Template → Component → Section → Group → Element hierarchy;
- explicit category registries;
- definition-owned rendering;
- recursive renderer;
- Pure Store/MVI/UDF interaction;
- frozen actions and references;
- runtime validation/fallback/recovery/diagnostic/observability boundaries;
- one canonical four-field destination contract.

## 6. Dynamic Use-Case Ownership

`LoadDynamicScreenUseCase` and `ExecuteDynamicActionUseCase` are feature orchestration use cases.

They are not a reason to create a second domain layer and must not be moved into top-level `:domain` while they depend on Dynamic feature capabilities.

Current package cleanup target:

```text
feature/dynamic/.../usecase/
```

## 7. Network Exception Contract

Frozen implementation taxonomy:

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

The network layer must preserve the distinction between HTTP status, connection, timeout, serialization and protocol failures. `UnknownNetworkException` is the final fallback. Business/API outcomes remain separate from transport exceptions.

## 8. Current Source Ownership

### Application / Navigation

- `app/App.kt`
- `app/ApplicationContent.kt`
- `navigation/DynamicNavigationState.kt`
- `navigation/DynamicNavigationHost.kt`

### Bootstrap target

- `domain/bootstrap/ApplicationBootstrap.kt`
- `domain/repository/BootstrapRepository.kt`
- `data/bootstrap/BootstrapRepositoryImpl.kt`
- `data/bootstrap/remote/BootstrapRemoteDataSource.kt`
- `data/bootstrap/model/*`
- `data/bootstrap/mapper/BootstrapMapper.kt`

Legacy to remove/rework:

- `data/bootstrap/DefaultApplicationBootstrap.kt`
- `domain/model/bootstrap/StartupConfig.kt`
- `domain/model/bootstrap/NextScreenConfig.kt`
- `domain/model/bootstrap/BootstrapConfig.kt`

### Dynamic

- `feature/dynamic/DynamicContainer.kt`
- `feature/dynamic/DynamicModule.kt`
- `feature/dynamic/store/DynamicStore.kt`
- `feature/dynamic/action/DynamicActionHandler.kt`
- `feature/dynamic/usecase/LoadDynamicScreenUseCase.kt`
- `feature/dynamic/usecase/ExecuteDynamicActionUseCase.kt`
- `feature/dynamic/registry/DynamicRegistry.kt`
- `feature/dynamic/renderer/DynamicRenderer.kt`

### Data / Network

- Dynamic repository/remote/model/mapper classes.
- `network/http/RequestHandler.kt`
- `network/http/HttpClientBuilder.kt`
- `network/http/DefaultError.kt`
- `network/NetworkException.kt`
- `network/NetworkResult.kt`
- `network/Response.kt`
- `network/Paged.kt`
- `network/di/NetworkModule.kt`

## 9. Verification Baseline

Owner-provided 2026-10-04 verification passed after DYNAMIC-08 reconciliation.

Compile verification:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:navigation:compileKotlinJvm
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 43 actionable tasks: 43 executed**.

JVM verification:

```text
:core:jvmTest
:domain:jvmTest
:data:jvmTest
:navigation:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
:app:compileKotlinJvm
```

Result: **BUILD SUCCESSFUL — 62 actionable tasks: 62 executed**.

Known Windows-only warnings are non-fatal: iOS simulator tests require macOS and Android host-test configuration is not enabled for these commonTest sources.

## 10. Full Gap Register

| Area | State | Required action |
|---|---|---|
| BASE-ARCH-001 → 017 | FROZEN | Do not reopen |
| DYNAMIC-01 → 08 | FROZEN | Do not reopen |
| DYNAMIC-08 source alignment | VERIFIED | Keep four-field contract |
| Bootstrap Option B | FROZEN / OPEN IMPLEMENTATION | Implement completely |
| Bootstrap legacy domain models | OPEN | Remove/rework |
| Bootstrap DI/tests | OPEN | Update and verify |
| Canonical DynamicDestination runtime | OPEN | Verify Bootstrap/API/action paths |
| Dynamic use-case package | OPEN | Move to feature `usecase` |
| Request metadata/authentication | OPEN | Complete |
| Dynamic state/reference/action behavior | PARTIAL | Complete frozen contract |
| Network exception taxonomy | FROZEN / OPEN IMPLEMENTATION | Implement and test |
| DYNAMIC-07 | PARTIAL | Complete production behavior |
| Login → OTP integration | PENDING | Add backend-shaped tests |
| Navigation runtime | PENDING | Verify push/replace/pop/back |
| Android | PENDING | Verify |
| iOS/macOS-hosted | PENDING | Verify where supported |
| Web/Wasm | PENDING | Verify where supported |
| Literal plan-to-source review | PENDING | Complete |
| Final Dynamic freeze | PENDING | Final gate |

## 11. Recovery Documents

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/12-PROJECT-TRACKER.md`
4. `docs/project-management/04-CURRENT-STATUS.md`
5. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
6. `docs/dynamic/DYNAMIC-ARCHITECTURE-FROZEN.md`
7. `docs/dynamic/DYNAMIC-DISCUSSION-AND-DECISIONS.md`
8. `docs/dynamic/DYNAMIC-IMPLEMENTATION-PLAN-FROZEN.md`
9. `docs/dynamic/DYNAMIC-IMPLEMENTATION-STATUS.md`

## 12. Next Valid Action

**Start the next already-authorized implementation item: complete Bootstrap Option B, including legacy-domain cleanup, DI/test reconciliation, and runtime hand-off verification. Do not reopen frozen Dynamic architecture decisions.**
