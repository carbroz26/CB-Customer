# CB-Partner Dynamic UI — Implementation Plan

**Status:** PLAN FROZEN — IMPLEMENTATION AUTHORIZED  
**Documentation reconciliation:** 2026-10-04

This document is the implementation source of truth after reconciliation with frozen DYNAMIC-01 → DYNAMIC-07 decisions and the final Bootstrap/Dynamic boundary record in `03-FINAL-RECONCILIATION-2026-10-04.md`.

## 1. Frozen runtime architecture

```text
Platform entry
  ↓
App
  ↓
ApplicationContent
  ↓
Splash UI / Splash Store
  ↓
ApplicationBootstrap
  ↓
BootstrapRepository
  ↓
BootstrapRepositoryImpl
  ↓
BootstrapRemoteDataSource
  ↓
RequestHandler / Ktor HttpClient
  ↓
Bootstrap DTO → BootstrapMapper → BootstrapOutput
  ↓
DynamicDestination
  ↓
DynamicNavigationState
  ↓
DynamicContainer
  ↓
DynamicStore
  ↓
Dynamic feature use cases
  ↓
DynamicRepository
  ↓
DynamicRepositoryImpl
  ↓
DynamicRemoteDataSource
  ↓
RequestHandler / Ktor HttpClient
  ↓
Dynamic DTO → DynamicResponseMapper → DynamicResponse
  ↓
DynamicRegistry / DynamicRenderer
```

Splash and Bootstrap are one startup flow. Splash is the visible startup UI; Bootstrap is the config/startup API execution while Splash is shown.

## 2. Clean Architecture ownership

```text
:feature:splash  → :domain, :core, :navigation
:feature:dynamic → :domain, :core, :navigation
:data            → :domain, :core, :network
:domain          → :core
:network         → Ktor + Koin
```

Features do not perform HTTP directly. `:data` owns DTOs, mappers, repository implementations and feature-specific remote sources. `:domain` owns canonical contracts/models and repository abstractions. `:network` owns generic Ktor infrastructure.

## 3. Bootstrap Option B — REQUIRED

```text
ApplicationBootstrap       :domain
        ↓
BootstrapRepository        :domain
        ↓
BootstrapRepositoryImpl    :data
        ↓
BootstrapRemoteDataSource  :data
        ↓
RequestHandler             :network
```

Responsibilities:

- `ApplicationBootstrap`: startup-facing domain contract.
- `BootstrapRepository`: domain abstraction.
- `BootstrapRepositoryImpl`: data orchestration.
- `BootstrapRemoteDataSource`: endpoint, method, required request headers and transport acquisition.
- `BootstrapMapper`: explicit DTO → domain mapping.

`DefaultApplicationBootstrap` is transitional and must be removed/reworked so there is exactly one production Bootstrap HTTP path.

## 4. Bootstrap domain model cleanup

The following legacy domain contracts must not remain as parallel contracts:

- `StartupConfig`
- `NextScreenConfig`
- standalone `BootstrapConfig`

`ApplicationBootstrap` returns the canonical `BootstrapOutput`. Bootstrap configuration/state is represented by that output, and the startup destination is the canonical `DynamicDestination`.

Transport DTO names may mirror the backend JSON and are not domain contracts.

The destination shape is:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

## 5. Bootstrap API contract

```text
GET /api/v1/partner/config/bootstrap
```

Headers:

```text
X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

`startup.nextScreen` is mapped directly to `DynamicDestination`.

## 6. One DynamicDestination for all navigation sources

Bootstrap `nextScreen`, a Dynamic API `nextScreen`, and a Dynamic action destination are three sources of the same destination concept, not three navigation systems.

```text
Bootstrap API nextScreen ───────┐
Dynamic API nextScreen ─────────┼──→ DynamicDestination → DynamicNavigationState
Dynamic action destination ─────┘
```

There are no Bootstrap-specific or action-specific destination models. `templateType` selects rendering only and never decides business navigation.

## 7. Dynamic feature boundary

```text
feature:dynamic
 ├── Store / UI / renderer / registry / action capability
 └── feature orchestration use cases
          ↓
       :domain
          ↓
 DynamicRepository
          ↑
       :data
 ├── DynamicRepositoryImpl
 ├── DynamicRemoteDataSource
 ├── Dynamic DTO/model
 └── DynamicResponseMapper
```

`LoadDynamicScreenUseCase` and `ExecuteDynamicActionUseCase` are feature orchestration use cases. They must not be moved into top-level `:domain` while they depend on Dynamic feature capabilities.

Current package `feature/dynamic/.../domain/usecase/` is a naming/ownership cleanup target. Move to `feature/dynamic/.../usecase/` without creating a second domain layer.

## 8. Network error contract

The network boundary must classify actual failures precisely:

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

Rules:

- HTTP status errors are classified centrally.
- Connection, timeout, serialization and protocol failures remain distinct.
- `UnknownNetworkException` is only the final fallback.
- Business/API outcomes are not automatically converted into transport exceptions.
- Do not duplicate an equivalent existing network error type.

## 9. Current source map

### Application / Navigation

- `app/.../App.kt`
- `app/.../ApplicationContent.kt`
- `navigation/.../DynamicNavigationState.kt`
- `navigation/.../DynamicNavigationHost.kt`

### Splash / Bootstrap

Target:

```text
domain/bootstrap/ApplicationBootstrap.kt
domain/repository/BootstrapRepository.kt

data/bootstrap/BootstrapRepositoryImpl.kt
data/bootstrap/remote/BootstrapRemoteDataSource.kt
data/bootstrap/model/*
data/bootstrap/mapper/BootstrapMapper.kt
```

Legacy to remove/rework:

```text
data/bootstrap/DefaultApplicationBootstrap.kt
domain/model/bootstrap/StartupConfig.kt
domain/model/bootstrap/NextScreenConfig.kt
domain/model/bootstrap/BootstrapConfig.kt
```

### Dynamic

- `feature/dynamic/.../DynamicContainer.kt`
- `feature/dynamic/.../DynamicModule.kt`
- `feature/dynamic/.../store/DynamicStore.kt`
- `feature/dynamic/.../action/DynamicActionHandler.kt`
- `feature/dynamic/.../usecase/LoadDynamicScreenUseCase.kt`
- `feature/dynamic/.../usecase/ExecuteDynamicActionUseCase.kt`
- `feature/dynamic/.../registry/DynamicRegistry.kt`
- `feature/dynamic/.../renderer/DynamicRenderer.kt`

### Data

- `data/dynamic/DynamicRepositoryImpl.kt`
- `data/dynamic/remote/DynamicRemoteDataSource.kt`
- `data/dynamic/model/DynamicResponseDto.kt`
- `data/dynamic/mapper/DynamicResponseMapper.kt`
- Bootstrap files listed above.

### Network

- `network/http/RequestHandler.kt`
- `network/http/HttpClientBuilder.kt`
- `network/http/DefaultError.kt`
- `network/NetworkException.kt`
- `network/NetworkResult.kt`
- `network/Response.kt`
- `network/Paged.kt`
- `network/di/NetworkModule.kt`

## 10. Frozen Dynamic vocabulary

Initial definitions:

```text
stack_template
stack_component
stack_section
stack_group
text
image
```

Frozen actions:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

Frozen references:

```text
$literal
$binding
$context
$response
```

Do not create Login/OTP/Dashboard-specific screen architecture.

## 11. Implementation sequence

### Phase A — Bootstrap reconciliation

1. Remove/rework legacy `StartupConfig`, `NextScreenConfig` and standalone `BootstrapConfig` domain contracts.
2. Implement `BootstrapRepository` domain contract.
3. Implement `BootstrapRepositoryImpl`.
4. Implement `BootstrapRemoteDataSource`.
5. Move DTOs into the Bootstrap transport/model boundary.
6. Keep `BootstrapMapper` explicit and map `nextScreen` to `DynamicDestination`.
7. Remove the direct `DefaultApplicationBootstrap → RequestHandler` production path.
8. Update Bootstrap DI.
9. Update Bootstrap unit tests.
10. Verify one Bootstrap request path only.

### Phase B — Shared destination/navigation

11. Verify Bootstrap, Dynamic API and action destinations all use `DynamicDestination`.
12. Verify push/replace/pop/back operate on the same destination type.
13. Verify no `templateType` business-navigation branching exists.

### Phase C — Dynamic cleanup and runtime completion

14. Move feature use cases from `domain/usecase` to `usecase`.
15. Complete request metadata/authentication integration.
16. Complete frozen Dynamic state/reference/action behavior.
17. Implement the frozen network exception taxonomy without duplicating existing types.
18. Complete DYNAMIC-07 parse/resolve/diagnose/fallback/recovery/observability behavior.

### Phase D — Product-flow verification

19. Add backend-shaped Login → Send OTP → OTP → Verify OTP tests.
20. Verify destination transitions and `responseMode` behavior.
21. Verify Dynamic rendering with the fixed backend contract.
22. Verify Android.
23. Verify iOS/macOS-hosted targets where supported.
24. Verify Web/Wasm where supported.

### Phase E — Final gate

25. Perform literal plan-to-source compliance review.
26. Resolve every remaining gap.
27. Run complete feature tests/builds.
28. Final Dynamic review.
29. Code-freeze Dynamic.

## 12. Definition of Done

- [ ] Bootstrap Option B fully implemented.
- [ ] No legacy Bootstrap domain duplicate models remain.
- [ ] Exactly one production Bootstrap HTTP path exists.
- [ ] Bootstrap DI/tests updated and green.
- [ ] Bootstrap nextScreen maps to canonical `DynamicDestination`.
- [ ] Dynamic API/action destinations use the same `DynamicDestination`.
- [ ] No templateType-based business navigation.
- [ ] Dynamic use cases are in the feature-level `usecase` package.
- [ ] Network exceptions are precisely classified.
- [ ] Frozen Dynamic actions/references/state behavior complete.
- [ ] DYNAMIC-07 production fallback/recovery/observability complete.
- [ ] Login → OTP integration tests pass.
- [ ] Navigation push/replace/pop/back verified.
- [ ] Android verification passes.
- [ ] iOS/macOS-hosted verification passes where applicable.
- [ ] Web/Wasm verification passes where applicable.
- [ ] Literal plan-to-source review passes.
- [ ] Final Dynamic review accepted.
- [ ] Dynamic code frozen.

## 13. Verification baseline

Owner-provided 2026-10-04 JVM verification passed:

```text
:network:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:app:compileKotlinJvm

:network:jvmTest
:data:jvmTest
:domain:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
```

`BUILD SUCCESSFUL` is only the current baseline. It does not certify the remaining architectural/runtime gaps.
