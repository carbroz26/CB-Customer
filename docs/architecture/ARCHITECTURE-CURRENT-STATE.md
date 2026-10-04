# CB-Partner — Current Architecture Reconciliation

**Status:** ACTIVE / CURRENT IMPLEMENTATION MAP
**Last Updated:** 2026-10-04

## Purpose

`BASE-ARCHITECTURE.md` is the frozen historical/base decision record. This document records the current implementation topology so new sessions do not confuse the original foundation wording with the architecture now being exercised by the Dynamic vertical slice.

This document does **not** silently reopen or replace a frozen BASE-ARCH decision. If a material contradiction requires a decision change, it must be handled through the documented reopening process.

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

## Current Gradle Topology

```text
:core
:domain
:network
:data
:navigation
:app
:feature:splash
:feature:dynamic
:androidApp
:webApp
build-logic (included build)
```

`iosApp/` is the native Xcode application boundary and is intentionally not a Gradle project.

The current branch does not register a `:desktopApp` Gradle project. Older Base Architecture text contains the earlier Desktop foundation wording. Treat that as a reconciliation item, not as permission to add Desktop back or remove Web.

## Current Dependency Direction

```text
:feature:splash → :domain, :core, :navigation
:feature:dynamic → :domain, :core, :navigation
:data → :domain, :core, :network
:app → application modules including :network, :data, :navigation, features
:navigation → :core / domain contracts as required by current implementation
:network → Ktor + Koin
:domain → :core
```

No feature depends directly on `:data`.

## Runtime Composition — Target After Bootstrap Refinement

Bootstrap and Splash are one startup flow. Splash is the visible startup UI while Bootstrap executes the config/startup API.

```text
Platform boundary
  ↓
App
  ↓
ApplicationContent
  ↓
SplashStore / SplashScreen
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
BootstrapResponseDto
  ↓
BootstrapMapper
  ↓
BootstrapOutput
  ↓
BootstrapOutput.startup.nextScreen
  ↓
DynamicDestination
  ↓
DynamicNavigationState
  ↓
DynamicContainer
  ↓
DynamicStore
  ↓
LoadDynamicScreenUseCase / ExecuteDynamicActionUseCase
  ↓
DynamicRepository
  ↓
DynamicRepositoryImpl
  ↓
DynamicRemoteDataSource
  ↓
RequestHandler / HttpClient
  ↓
DynamicResponseDto
  ↓
DynamicResponseMapper
  ↓
DynamicResponse
  ↓
DynamicRegistry / DynamicRenderer
```

The Bootstrap target above is **required implementation work**. The current source still has the direct `DefaultApplicationBootstrap → RequestHandler` path and must be refactored to the target without changing the frozen startup responsibility.

## Current Ownership Rules

### `:app`

Owns application composition only. `App.kt` is intentionally thin. `ApplicationContent.kt` composes Splash, Dynamic navigation, registry, Store and effects.

### `:navigation`

Owns the navigation boundary and Dynamic back-stack mechanics. `DynamicDestination` is the canonical navigation key. Navigation must not infer business screens from `templateType`.

### `:feature:splash`

Owns startup UI/state. It does not own HTTP implementation. `ApplicationBootstrap` is the startup-facing domain contract. Bootstrap transport/repository implementation belongs in `:data`.

### `:feature:dynamic`

Owns the live Dynamic screen lifecycle, Store/MVI orchestration, registry, renderer, action capability and Dynamic definitions. Feature-level use cases remain feature orchestration when they coordinate feature capabilities. The feature does not own generic HTTP infrastructure or business-specific screen classes.

### `:domain`

Owns canonical Dynamic and Bootstrap contracts/models and repository abstractions. It does not depend on Data, Ktor or Compose UI.

### `:data`

Owns DTOs, serialization, mappers, repository implementations and feature-specific remote sources. Bootstrap and Dynamic use the same Data layering pattern:

```text
Repository implementation → RemoteDataSource → RequestHandler
```

### `:network`

Owns Ktor infrastructure, the configured shared `HttpClient`, `RequestHandler`, generic network result/error contracts and network DI. It is not a business repository layer.

## Bootstrap Architecture — Option B

Option B is now the selected implementation refinement within the frozen startup responsibility:

```text
ApplicationBootstrap (domain)
        ↓
BootstrapRepository (domain)
        ↓
BootstrapRepositoryImpl (data)
        ↓
BootstrapRemoteDataSource (data)
        ↓
RequestHandler (network)
```

Responsibilities:

- `ApplicationBootstrap`: startup-facing contract consumed by Splash/application composition.
- `BootstrapRepository`: domain repository abstraction.
- `BootstrapRepositoryImpl`: coordinates remote data and maps to the domain result.
- `BootstrapRemoteDataSource`: owns Bootstrap endpoint, HTTP method, request headers and transport DTO acquisition.
- `BootstrapMapper`: validates/maps DTO to canonical domain models.

The known endpoint is:

```text
GET /api/v1/partner/config/bootstrap
```

Required request headers currently documented/tested:

```text
X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

Success data includes `config` plus `startup.nextScreen`. The mapper converts `nextScreen` directly to the canonical `DynamicDestination`.

`DefaultApplicationBootstrap` is therefore a transitional implementation and must not remain as a second direct HTTP path after the refinement.

## Frozen Dynamic Boundary

The complete Dynamic architecture discussion DYNAMIC-01 through DYNAMIC-07 is frozen in:

`docs/features/dynamic/00-FEATURE-DISCUSSION.md`

The runtime boundary is:

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

The frontend does not duplicate backend SDUI structural validation and never silently repairs, substitutes or downgrades backend definitions.

## Dynamic Use-Case Ownership

Current implementation uses:

```text
feature/dynamic/.../domain/usecase/LoadDynamicScreenUseCase.kt
feature/dynamic/.../domain/usecase/ExecuteDynamicActionUseCase.kt
```

These are feature-level orchestration use cases, not a reason to create a second domain layer inside the feature.

- `LoadDynamicScreenUseCase` coordinates screen loading through `DynamicRepository`.
- `ExecuteDynamicActionUseCase` coordinates Dynamic action execution and feature-owned `DynamicActionHandler` capabilities.

Target package cleanup is:

```text
feature/dynamic/.../usecase/
```

They must not be moved into top-level `:domain` while they depend on feature-owned capabilities.

## Current Code Map

```text
app/
  src/commonMain/.../app/App.kt
  src/commonMain/.../app/ApplicationContent.kt

navigation/
  src/commonMain/.../navigation/DynamicNavigationState.kt
  src/commonMain/.../navigation/DynamicNavigationHost.kt

feature/dynamic/
  .../DynamicContainer.kt
  .../DynamicModule.kt
  .../store/DynamicStore.kt
  .../action/DynamicActionHandler.kt
  .../domain/usecase/LoadDynamicScreenUseCase.kt   [package cleanup pending]
  .../domain/usecase/ExecuteDynamicActionUseCase.kt [package cleanup pending]
  .../registry/DynamicRegistry.kt
  .../renderer/DynamicRenderer.kt
  .../sdui/template/StackTemplate.kt
  .../sdui/component/StackComponent.kt
  .../sdui/section/StackSection.kt
  .../sdui/group/StackGroup.kt
  .../sdui/element/TextElement.kt
  .../sdui/element/ImageElement.kt

data/
  .../bootstrap/DefaultApplicationBootstrap.kt       [transitional; replace]
  .../bootstrap/BootstrapMapper.kt
  .../bootstrap/BootstrapRepositoryImpl.kt            [required]
  .../bootstrap/remote/BootstrapRemoteDataSource.kt  [required]
  .../bootstrap/model/*
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
  .../model/bootstrap/*
  .../repository/BootstrapRepository.kt             [required]
  .../model/dynamic/DynamicModels.kt
  .../repository/DynamicRepository.kt
```

## Reconciliation Items

1. Historical base documents mention Desktop while current implementation registers Web/Wasm instead. This must be explicitly reconciled before treating platform support as permanently frozen.
2. Older Dynamic drafts mention `NavigationStore`, `DynamicScreenStore`, `SduiDecoder`, `SduiSupportChecker`, `SduiActionExecutor`, and other names. Those are historical design vocabulary. The current implementation uses `DynamicNavigationState`, `DynamicStore`, `DynamicRenderer`, `DynamicRegistry`, `LoadDynamicScreenUseCase`, and `ExecuteDynamicActionUseCase`.
3. Do not reintroduce historical class names unless the current frozen Dynamic contract requires a new responsibility and the change is deliberately decided.
4. DYNAMIC-07 is frozen as a runtime boundary, but its full production observability/fallback implementation is not yet complete.
5. Bootstrap's old direct Data implementation is now explicitly superseded by Option B and must be removed/reworked during implementation.
6. The actual Bootstrap API contract is known; any documentation still describing it as unknown is stale and must be updated rather than treated as a blocker.

## Verification Baseline

The latest owner-provided JVM verification on 2026-10-04 passed:

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

This is the current baseline only. It does not certify the remaining Bootstrap layering, Dynamic runtime gaps, Login → OTP flow, DYNAMIC-07 production implementation, or platform verification.
