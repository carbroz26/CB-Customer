# CB-Partner — Current Architecture Reconciliation

**Status:** ACTIVE / CURRENT IMPLEMENTATION MAP  
**Last Updated:** 2026-10-04

This document describes the current architecture exercised by the Dynamic vertical slice. Frozen base decisions remain in `BASE-ARCHITECTURE.md`; this document must not silently reopen them.

## Current Technical Direction

```text
Kotlin Multiplatform
Compose Multiplatform
Clean Architecture
Pure Store / MVI / UDF
No ViewModel
Koin DI
Ktor Client
kotlinx.serialization
Kermit
Navigation 3
Gradle Convention Plugins
```

## Current Dependency Direction

```text
:feature:splash  → :domain, :core, :navigation
:feature:dynamic → :domain, :core, :navigation
:data            → :domain, :core, :network
:domain          → :core
:network         → Ktor + Koin
```

Features do not depend directly on `:data`.

## Startup / Bootstrap — Final Boundary

Splash and Bootstrap are one startup flow. Splash is the visible startup UI; Bootstrap is the startup/config API execution while Splash is shown.

```text
ApplicationContent
  ↓
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
RequestHandler / HttpClient
  ↓
Bootstrap DTO
  ↓
BootstrapMapper
  ↓
BootstrapOutput
  ↓
DynamicDestination
  ↓
DynamicNavigationState
```

This is **Bootstrap Option B**, now frozen as the implementation boundary.

### Bootstrap API

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

### Legacy Bootstrap code/contracts

The following are transitional/legacy and must not remain as parallel architecture:

```text
DefaultApplicationBootstrap
StartupConfig
NextScreenConfig
standalone BootstrapConfig
```

`BootstrapOutput` is the canonical domain result. Its startup destination is the canonical `DynamicDestination`.

## Canonical DynamicDestination

Bootstrap `nextScreen`, Dynamic API `nextScreen`, and Dynamic action destination are three sources of the same destination concept.

```text
Bootstrap API nextScreen ───────┐
Dynamic API nextScreen ─────────┼──→ DynamicDestination → DynamicNavigationState
Dynamic action destination ─────┘
```

There is one destination type and one navigation/back-stack mechanism.

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
├── endpoint
├── method
└── authentication
```

`templateType` selects rendering only. It never decides business navigation.

## Dynamic Ownership

### `:feature:dynamic`

Owns Dynamic screen lifecycle, Store/MVI orchestration, registry, renderer, action capabilities and Dynamic definitions.

Feature use cases are orchestration classes:

- `LoadDynamicScreenUseCase`
- `ExecuteDynamicActionUseCase`

They must not be moved into top-level `:domain` while they coordinate feature-owned capabilities.

Target package:

```text
feature/dynamic/.../usecase/
```

The current `feature/dynamic/.../domain/usecase/` path is a package naming cleanup, not a second domain architecture.

### `:domain`

Owns canonical contracts/models and repository abstractions. It does not own Ktor, Compose UI or Data implementations.

### `:data`

Owns DTOs, mappers, repository implementations and feature-specific remote sources.

Bootstrap and Dynamic use the same repository/data-source classification:

```text
Repository implementation → RemoteDataSource → RequestHandler
```

### `:network`

Owns Ktor infrastructure, shared `HttpClient`, `RequestHandler`, generic network result/error contracts and network DI.

## Network Exception Contract

The network boundary uses this production-oriented classification:

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

- HTTP status failures are classified centrally.
- Connection failures are separate from HTTP failures.
- Timeout failures are separate from connection failures.
- Serialization/decode failures are separate from HTTP failures.
- Protocol/malformed-response failures are separate from HTTP status failures.
- `UnknownNetworkException` is only the final fallback.
- Business/API outcomes are not automatically converted into network exceptions.
- Existing equivalent error types must be reused rather than duplicated.

## Dynamic Frozen Boundary

DYNAMIC-01 → DYNAMIC-07 remain frozen in `docs/features/dynamic/00-FEATURE-DISCUSSION.md`.

The runtime boundary remains:

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

No duplicate backend validator, silent type substitution, templateType-driven business navigation, or business-specific Login/OTP/Dashboard screen architecture is permitted.

## Current Source Map

```text
app/
  .../App.kt
  .../ApplicationContent.kt

navigation/
  .../DynamicNavigationState.kt
  .../DynamicNavigationHost.kt

feature/dynamic/
  .../DynamicContainer.kt
  .../DynamicModule.kt
  .../store/DynamicStore.kt
  .../action/DynamicActionHandler.kt
  .../usecase/LoadDynamicScreenUseCase.kt
  .../usecase/ExecuteDynamicActionUseCase.kt
  .../registry/DynamicRegistry.kt
  .../renderer/DynamicRenderer.kt
  .../sdui/...

data/
  .../bootstrap/BootstrapRepositoryImpl.kt
  .../bootstrap/remote/BootstrapRemoteDataSource.kt
  .../bootstrap/model/*
  .../bootstrap/mapper/BootstrapMapper.kt
  .../dynamic/DynamicRepositoryImpl.kt
  .../dynamic/remote/DynamicRemoteDataSource.kt
  .../dynamic/model/DynamicResponseDto.kt
  .../dynamic/mapper/DynamicResponseMapper.kt

network/
  .../http/RequestHandler.kt
  .../http/HttpClientBuilder.kt
  .../http/DefaultError.kt
  .../NetworkException.kt
  .../NetworkResult.kt
  .../Response.kt
  .../Paged.kt
  .../di/NetworkModule.kt

domain/
  .../bootstrap/ApplicationBootstrap.kt
  .../repository/BootstrapRepository.kt
  .../model/dynamic/DynamicModels.kt
  .../repository/DynamicRepository.kt
```

The source map above is the **target after the authorized reconciliation**. The implementation status document records which target items are still pending.

## Final Reconciliation Record

The complete final reconciliation is recorded in:

`docs/features/dynamic/03-FINAL-RECONCILIATION-2026-10-04.md`

That record freezes:

1. Splash/Bootstrap as one flow.
2. Bootstrap Option B repository/data-source layering.
3. One canonical `DynamicDestination` for Bootstrap/API/action destinations.
4. Feature-level use-case ownership.
5. Production-oriented network exception classification.

## Verification Baseline

Owner-provided 2026-10-04 JVM compilation/tests passed:

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

Result: **BUILD SUCCESSFUL**.

This does not certify the remaining Bootstrap implementation, network exception implementation, Dynamic runtime gaps, Login → OTP integration, DYNAMIC-07 production implementation or platform verification.
