# CB-Partner — Frontend Architecture

**Status:** ACTIVE / FROZEN BASE + CURRENT RECONCILIATION
**Scope:** Frontend repository only
**Last Updated:** 2026-10-04

## Purpose

This is the high-level architecture index. Detailed frozen Base Architecture decisions remain in `BASE-ARCHITECTURE.md`. The current implementation topology and reconciliation notes are in `ARCHITECTURE-CURRENT-STATE.md`. Feature behavior belongs in feature documentation.

## Frozen Technical Direction

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store-based MVI
- UDF
- No ViewModel
- Multi-module architecture
- Koin dependency injection
- Ktor Client
- kotlinx.serialization
- Kermit logging
- Navigation 3
- Gradle Convention Plugins

## Architecture Records

### Frozen Base Architecture

`docs/architecture/BASE-ARCHITECTURE.md`

BASE-ARCH-001 through BASE-ARCH-017 are decision-frozen. BASE-ARCH-014 and BASE-ARCH-017 are code-frozen after verification/review.

### Current Architecture Reconciliation

`docs/architecture/ARCHITECTURE-CURRENT-STATE.md`

This records the current source topology so the original base record is not confused with later implementation evolution. It does not silently reopen frozen decisions.

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
iosApp/ (native Xcode boundary, not Gradle)
```

The current branch does not register `:desktopApp`. Older Base Architecture wording contains the earlier Desktop foundation direction. Treat this as a documented reconciliation item; do not add/remove platform code without an explicit architecture decision.

## Current Runtime Shape

Bootstrap and Splash are one startup flow. Splash is the visible startup UI while Bootstrap executes the config/startup API.

```text
Platform Entry
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
RequestHandler / Ktor
  ↓
BootstrapResponseDto
  ↓
BootstrapMapper
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
Dynamic Use Cases
  ↓
DynamicRepository
  ↓
DynamicRepositoryImpl
  ↓
DynamicRemoteDataSource
  ↓
RequestHandler
  ↓
DynamicResponse
  ↓
DynamicRegistry / DynamicRenderer
```

The Bootstrap repository/remote-source segment is the selected Option B implementation target and is currently a documented gap until implemented.

## Bootstrap Boundary

Selected implementation refinement:

```text
ApplicationBootstrap        :domain startup contract
        ↓
BootstrapRepository         :domain repository contract
        ↓
BootstrapRepositoryImpl     :data
        ↓
BootstrapRemoteDataSource   :data transport source
        ↓
RequestHandler              :network
```

Actual Bootstrap API:

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

`startup.nextScreen` becomes the canonical `DynamicDestination`.

The old direct `DefaultApplicationBootstrap → RequestHandler` implementation is transitional and must be removed/reworked so there is one production request path.

## Dynamic Architecture

Dynamic is backend-driven. Login, OTP, Dashboard and future product screens are Dynamic destinations, not native feature screen classes.

Frozen Dynamic decisions are documented in:

`docs/features/dynamic/00-FEATURE-DISCUSSION.md`

The Dynamic runtime boundary is:

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

## Dynamic Use-Case Ownership

Dynamic use cases are feature orchestration, not a reason to create a second domain layer inside `:feature:dynamic`.

- `LoadDynamicScreenUseCase` coordinates screen loading through the domain repository contract.
- `ExecuteDynamicActionUseCase` coordinates feature-owned Dynamic action capabilities.

Current package naming under `feature/dynamic/.../domain/usecase/` is a cleanup target. Target package is `feature/dynamic/.../usecase/`. Do not move feature-owned action capabilities into top-level `:domain` merely for naming symmetry.

## Dependency Direction

```text
Feature → Domain
Feature → Core
Feature → Navigation

Data → Domain
Data → Core
Data → Network

Domain → Core

Application → application/shared modules
```

No feature directly depends on Data for business UI logic. Generic HTTP infrastructure belongs in `:network`.

## State Architecture

Feature state follows Pure Store + MVI/UDF. There is no ViewModel architecture.

Dynamic Store responsibilities include:

- Dynamic state;
- intents;
- loading;
- action/use-case coordination;
- Dynamic effects.

Dynamic Store does not own generic HTTP implementation, Navigation 3 primitives, or Compose rendering recursion.

## Code Quality Direction

Use the Shaadi project only as a clean-code reference for small responsibilities, explicit mapping, constructor injection and a small request boundary. Do not copy its Android/JVM-only architecture, ViewModel/Hilt structure or engine assumptions.

## Change Governance

A material architecture change requires:

```text
DISCUSS → RESEARCH → DECIDE → FREEZE → PLAN → IMPLEMENT → TEST → REVIEW
```

Option B Bootstrap is an implementation refinement within the frozen startup responsibility, not a reopening of the frozen startup decision.
