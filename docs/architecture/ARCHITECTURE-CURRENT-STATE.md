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

## Runtime Composition

```text
Platform boundary
  ↓
App
  ↓
ApplicationContent
  ↓
SplashStore
  ↓
ApplicationBootstrap
  ↓
RequestHandler / HttpClient
  ↓
BootstrapOutput.startup.nextScreen
  ↓
DynamicNavigationState
  ↓
DynamicContainer
  ↓
DynamicStore
  ↓
LoadDynamicScreenUseCase
  ↓
DynamicRepository
  ↓
DynamicRemoteDataSource
  ↓
RequestHandler
  ↓
DynamicResponseDto
  ↓
DynamicResponseMapper
  ↓
DynamicResponse
  ↓
DynamicRegistry / DynamicRenderer
```

## Current Ownership Rules

### `:app`

Owns application composition only. `App.kt` is intentionally thin. `ApplicationContent.kt` composes Splash, Dynamic navigation, registry, Store and effects.

### `:navigation`

Owns the navigation boundary and Dynamic back-stack mechanics. `DynamicDestination` is the canonical navigation key. Navigation must not infer business screens from `templateType`.

### `:feature:splash`

Owns startup UI/state. It does not own HTTP implementation. Bootstrap is a domain contract implemented in Data.

### `:feature:dynamic`

Owns the live Dynamic screen lifecycle, Store/MVI orchestration, registry, renderer and Dynamic definitions. It does not own generic HTTP infrastructure or business-specific screen classes.

### `:domain`

Owns canonical Dynamic and Bootstrap contracts/models and repository/use-case-facing abstractions. It does not depend on Data, Ktor or Compose UI.

### `:data`

Owns DTOs, serialization, mappers, repositories and feature-specific remote sources. It consumes the shared `RequestHandler` from `:network`.

### `:network`

Owns Ktor infrastructure, the configured shared `HttpClient`, `RequestHandler`, generic network result/error contracts and network DI. It is not a business repository layer.

## Frozen Dynamic Boundary

The complete Dynamic architecture discussion DYNAMIC-01 through DYNAMIC-07 is frozen in:

`docs/features/dynamic/00-FEATURE-DISCUSSION.md`

The runtime boundary is:

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

The frontend does not duplicate backend SDUI structural validation and never silently repairs, substitutes or downgrades backend definitions.

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
  .../domain/usecase/LoadDynamicScreenUseCase.kt
  .../domain/usecase/ExecuteDynamicActionUseCase.kt
  .../registry/DynamicRegistry.kt
  .../renderer/DynamicRenderer.kt
  .../sdui/template/StackTemplate.kt
  .../sdui/component/StackComponent.kt
  .../sdui/section/StackSection.kt
  .../sdui/group/StackGroup.kt
  .../sdui/element/TextElement.kt
  .../sdui/element/ImageElement.kt

data/
  .../bootstrap/DefaultApplicationBootstrap.kt
  .../bootstrap/BootstrapMapper.kt
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
  .../model/dynamic/DynamicModels.kt
  .../repository/DynamicRepository.kt
```

## Reconciliation Items

1. Historical base documents mention Desktop while current implementation registers Web/Wasm instead. This must be explicitly reconciled before treating platform support as permanently frozen.
2. Older Dynamic drafts mention `NavigationStore`, `DynamicScreenStore`, `SduiDecoder`, `SduiSupportChecker`, `SduiActionExecutor`, and other names. Those are historical design vocabulary. The current implementation uses `DynamicNavigationState`, `DynamicStore`, `DynamicRenderer`, `DynamicRegistry`, `LoadDynamicScreenUseCase`, and `ExecuteDynamicActionUseCase`.
3. Do not reintroduce the historical class names unless the current frozen Dynamic contract requires a new responsibility and the change is deliberately decided.
4. `DYNAMIC-07` is frozen as a runtime boundary, but its full production observability/fallback implementation is not yet complete.

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
