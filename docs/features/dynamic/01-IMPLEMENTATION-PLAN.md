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

## 6. One DynamicDestination for the complete navigation flow

Bootstrap `nextScreen`, a Dynamic response `nextScreen`, and a Dynamic `navigate` action are not separate navigation concepts. They are inputs that resolve to the same canonical `DynamicDestination` and are handled by the same Dynamic navigation mechanism.

```text
Bootstrap response data.startup.nextScreen ──┐
Dynamic response data.nextScreen ────────────┼──→ DynamicDestination
Dynamic navigate action payload ─────────────┘            ↓
                                               DynamicNavigationState
```

There is only one destination model and one Dynamic navigation mechanism. `DynamicDestination` lives directly under the shared domain `model` package so Bootstrap, Dynamic and action handling use the same contract.

`templateType` selects rendering only and never decides business navigation.

## 7. Dynamic response data contract

`nextScreen` is part of the response `data` payload. It is not a sibling of `data` in the Dynamic API response envelope.

For a screen response, the domain shape is:

```text
DynamicResponse
  └── data: DynamicScreen
        ├── screenId
        ├── schemaVersion
        ├── targetApp
        ├── template
        ├── theme
        └── nextScreen: DynamicDestination?
```

`DynamicNextScreen` is not a separate domain model. `DynamicScreen.nextScreen` uses the shared `DynamicDestination` directly.

Arbitrary non-screen response data remains available through `DynamicResponse.dataPayload` for chained actions and response references.

## 8. Dynamic feature boundary

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

## 9. Network error contract

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

## 10. Current source map

### Application / Navigation

- `app/.../App.kt`
- `app/.../ApplicationContent.kt`
- `navigation/.../DynamicNavigationState.kt`
- `navigation/.../DynamicNavigationHost.kt`
- `domain/.../model/DynamicDestination.kt`

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

## 11. Frozen Dynamic vocabulary

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

## 12. Implementation sequence

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

11. Verify Bootstrap, Dynamic response and action destinations all resolve to `DynamicDestination`.
12. Verify Dynamic response `nextScreen` is read from `data`, not the response envelope root.
13. Verify push/replace/pop/back operate on the same destination type.
14. Verify no `templateType` business-navigation branching exists.

### Phase C — Dynamic cleanup and runtime completion

15. Move feature use cases from `domain/usecase` to `usecase`.
16. Complete request metadata/authentication integration.
17. Complete frozen Dynamic state/reference/action behavior.
18. Implement the frozen network exception taxonomy without duplicating existing types.
19. Complete DYNAMIC-07 parse/resolve/diagnose/fallback/recovery/observability behavior.

### Phase D — Product-flow verification

20. Add backend-shaped Login → Send OTP → OTP → Verify OTP tests.
21. Verify destination transitions and `responseMode` behavior.
22. Verify Dynamic rendering with the fixed backend contract.
23. Verify Android.
24. Verify iOS/macOS-hosted targets where supported.
25. Verify Web/Wasm where supported.

### Phase E — Final gate

26. Perform literal plan-to-source compliance review.
27. Resolve every remaining gap.
28. Run complete feature tests/builds.
29. Final Dynamic review.
30. Code-freeze Dynamic.

## 13. Definition of Done

- [ ] Bootstrap Option B fully implemented.
- [ ] No legacy Bootstrap domain duplicate models remain.
- [ ] Exactly one production Bootstrap HTTP path exists.
- [ ] Bootstrap DI/tests updated and green.
- [ ] Bootstrap nextScreen maps to canonical `DynamicDestination`.
- [ ] Dynamic response nextScreen is inside `data` and maps to canonical `DynamicDestination`.
- [ ] Dynamic navigate actions use the same `DynamicDestination`.
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

## 14. Verification baseline

Owner-provided 2026-10-04 JVM verification passed before this model cleanup:

```text
:domain:compileKotlinJvm
:data:compileKotlinJvm
:feature:splash:compileKotlinJvm
:feature:dynamic:compileKotlinJvm
:app:compileKotlinJvm

:data:jvmTest
:domain:jvmTest
:feature:splash:jvmTest
:feature:dynamic:jvmTest
```

The baseline does not certify this latest model refactor until the same verification is rerun.
