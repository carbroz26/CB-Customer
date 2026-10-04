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

```text
Platform Entry
  ↓
App
  ↓
ApplicationContent
  ↓
SplashStore
  ↓
ApplicationBootstrap
  ↓
RequestHandler / Ktor
  ↓
BootstrapOutput.startup.nextScreen
  ↓
DynamicNavigationState
  ↓
DynamicContainer
  ↓
DynamicStore
  ↓
Use Cases
  ↓
DynamicRepository
  ↓
DynamicRemoteDataSource
  ↓
RequestHandler
  ↓
DynamicResponse
  ↓
DynamicRegistry / DynamicRenderer
```

## Dynamic Architecture

Dynamic is backend-driven. Login, OTP, Dashboard and future product screens are Dynamic destinations, not native feature screen classes.

Frozen Dynamic decisions are documented in:

`docs/features/dynamic/00-FEATURE-DISCUSSION.md`

The Dynamic runtime boundary is:

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

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

No silent architecture changes.
