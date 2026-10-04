# CB-Partner — Project Master State

**Status:** ACTIVE / RECOVERY SOURCE
**Last Updated:** 2026-10-04
**Scope:** Frontend repository only

## Purpose

Compact cross-session recovery source. It records frozen decisions, actual implementation, verification, remaining gaps, ownership, and the exact documents to follow. Historical chat is not an implementation source.

## 1. Current Project State

- Repository: `carbroz26/CB-Partner`
- Active implementation branch: `feature/dynamic-ui`
- Current feature: Dynamic UI vertical slice
- DYNAMIC-01 through DYNAMIC-07: **FROZEN**
- Dynamic implementation plan: **PLAN FROZEN / IMPLEMENTATION AUTHORIZED**
- Dynamic implementation: **IN PROGRESS**
- Latest owner JVM baseline: **BUILD SUCCESSFUL — 2026-10-04**
- BASE-ARCH-014: **CODE FROZEN**
- BASE-ARCH-017: **CODE FROZEN / VERIFIED / REVIEW ACCEPTED**
- No ViewModel.
- Pure Store + MVI/UDF.
- Backend-driven Dynamic navigation/rendering.
- Bootstrap Option B refinement: **SELECTED / REQUIRED IMPLEMENTATION**.

## 2. Frozen Base Architecture

```text
Kotlin Multiplatform
Compose Multiplatform
Clean Architecture
Pure Store / MVI / UDF
No ViewModel
Multi-module Gradle project
Convention plugins in build-logic
Koin DI
Ktor Client
kotlinx.serialization
Kermit logging
Navigation 3
```

BASE-ARCH-001 through BASE-ARCH-017 are decision-frozen. BASE-ARCH-014 and BASE-ARCH-017 are code-frozen/verified as recorded.

`BASE-ARCHITECTURE.md` remains the frozen historical/base record. `ARCHITECTURE-CURRENT-STATE.md` is the current implementation reconciliation. Do not silently rewrite a frozen decision.

## 3. Current Gradle / Module Topology

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
iosApp/ (native Xcode boundary; not a Gradle module)
```

Current branch does not register `:desktopApp`. Older Desktop wording remains a separate documentation reconciliation item.

## 4. Startup / Bootstrap — Final Target Boundary

Splash and Bootstrap are one startup flow. Splash is the visible startup UI; Bootstrap is the config/startup API execution performed while Splash is shown. They are not separate product flows.

```text
Platform entry
  ↓
App
  ↓
ApplicationContent
  ↓
SplashStore / SplashScreen
  ↓
ApplicationBootstrap                 :domain
  ↓
BootstrapRepository                 :domain
  ↓
BootstrapRepositoryImpl             :data
  ↓
BootstrapRemoteDataSource           :data
  ↓
RequestHandler / HttpClient          :network
  ↓
BootstrapResponseDto                :data
  ↓
BootstrapMapper                     :data
  ↓
BootstrapOutput                     :domain
  ↓
startup.nextScreen
  ↓
DynamicDestination
  ↓
DynamicNavigationState
```

**Option B is selected.** It refines the already-frozen startup responsibility; it does not reopen BASE-ARCH-012/013.

### Actual Bootstrap API

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

Response contains config plus:

```text
startup.authenticated
startup.nextScreen
```

`startup.nextScreen` maps directly to canonical `DynamicDestination`.

### Bootstrap current vs target

Current transitional path:

```text
ApplicationBootstrap → DefaultApplicationBootstrap → RequestHandler
```

Target:

```text
ApplicationBootstrap → BootstrapRepository → BootstrapRepositoryImpl → BootstrapRemoteDataSource → RequestHandler
```

The direct implementation must be removed/reworked so there is one production Bootstrap request path.

## 5. Canonical Dynamic Destination

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
├── endpoint
├── method
└── authentication
```

`templateType` selects rendering capability only. It never decides business navigation. The complete `DynamicDestination` is the navigation/back-stack entry.

## 6. Frozen Dynamic Architecture — DYNAMIC-01 → DYNAMIC-07

### DYNAMIC-01

One permanent Dynamic Container; backend destination/navigation stack; full `DynamicDestination` stack entry; no native Login/OTP/Dashboard screen classes.

### DYNAMIC-02

```text
Screen
└── exactly 1 Template
    └── 1..N Components
        ├── Elements
        └── Sections
            ├── Elements
            └── Groups
                └── Elements
```

Initial vocabulary: `stack_template`, `stack_component`, `stack_section`, `stack_group`, `text`, `image`.

### DYNAMIC-03

One public `DynamicRegistry`, category-specific internal registries, explicit registration, duplicate registration error, controlled unknown-type result, no reflection/scanning/plugin framework.

### DYNAMIC-04

Definition-owned Compose rendering; parent owns container/layout and child placement; child owns content/legal sizing; WRAP/FILL/FIXED/FRACTION with applicable constraints; constraint-based responsive behavior; Theme is screen-level; no giant property framework.

### DYNAMIC-05

Recursive `DynamicRenderer` resolves definitions through the registry. Parents delegate legal children through the renderer. Element is terminal. Unknown/illegal structures are controlled; no silent substitution.

### DYNAMIC-06

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

Request supports method, endpoint, authentication, validate, headers/query/body where supported, and `responseMode`. `destination` produces a destination; `none` completes without one. Request errors do not automatically navigate.

### DYNAMIC-07

```text
PARSE → RESOLVE → RENDER → DIAGNOSE → FALLBACK → RECOVER SAFELY
```

No duplicate backend validator, no guessing/substitution/downgrade, localized fallbacks, no crash, no implicit/infinite retry, safe structured diagnostics, no sensitive logging, minimal production observability.

## 7. Current Source Ownership

### Application

- `app/App.kt`
- `app/ApplicationContent.kt`

### Navigation

- `navigation/DynamicNavigationState.kt`
- `navigation/DynamicNavigationHost.kt`

### Splash / Bootstrap

Current:

- `domain/bootstrap/ApplicationBootstrap.kt`
- `domain/model/bootstrap/*`
- `data/bootstrap/DefaultApplicationBootstrap.kt` — transitional; must be reworked
- `data/bootstrap/BootstrapMapper.kt`

Required target:

- `domain/repository/BootstrapRepository.kt`
- `data/bootstrap/BootstrapRepositoryImpl.kt`
- `data/bootstrap/remote/BootstrapRemoteDataSource.kt`
- `data/bootstrap/model/*`
- `data/bootstrap/mapper/BootstrapMapper.kt`

### Dynamic feature

- `feature/dynamic/DynamicContainer.kt`
- `feature/dynamic/DynamicModule.kt`
- `feature/dynamic/store/DynamicStore.kt`
- `feature/dynamic/action/DynamicActionHandler.kt`
- `feature/dynamic/domain/usecase/LoadDynamicScreenUseCase.kt` — package cleanup pending
- `feature/dynamic/domain/usecase/ExecuteDynamicActionUseCase.kt` — package cleanup pending
- `feature/dynamic/registry/DynamicRegistry.kt`
- `feature/dynamic/renderer/DynamicRenderer.kt`
- initial Dynamic definitions under `sdui/`

### Domain

- `domain/model/dynamic/DynamicModels.kt`
- `domain/repository/DynamicRepository.kt`
- `domain/bootstrap/ApplicationBootstrap.kt`
- `domain/model/bootstrap/*`
- `domain/repository/BootstrapRepository.kt` — required

### Data / Network

Dynamic:

- `data/dynamic/DynamicRepositoryImpl.kt`
- `data/dynamic/remote/DynamicRemoteDataSource.kt`
- `data/dynamic/model/DynamicResponseDto.kt`
- `data/dynamic/mapper/DynamicResponseMapper.kt`

Network:

- `network/http/RequestHandler.kt`
- `network/http/HttpClientBuilder.kt`
- `network/http/DefaultError.kt`
- `network/NetworkException.kt`
- `network/NetworkResult.kt`
- `network/Response.kt`
- `network/Paged.kt`
- `network/di/NetworkModule.kt`

## 8. Dynamic Use-Case Ownership

Feature use cases are not automatically top-level domain use cases.

- `LoadDynamicScreenUseCase` is feature orchestration over the Dynamic repository contract.
- `ExecuteDynamicActionUseCase` coordinates feature-owned `DynamicActionHandler` capabilities.

Therefore they must **not** be moved into top-level `:domain` while they depend on feature-owned capabilities. The current `feature/dynamic/domain/usecase` package is a naming/ownership cleanup target; the target is `feature/dynamic/usecase`.

## 9. Login → OTP Contract

Login destination:

```text
screenId       = partner_login
templateId     = tpl_7K2M9Q
templateType   = form_template
endpoint       = /api/v1/partner/screen/auth_login
method         = GET
authentication  = NONE
```

OTP destination:

```text
screenId       = partner_otp
templateId     = tpl_P6X8N3
templateType   = form_template
endpoint       = /api/v1/partner/screen/auth_otp
method         = GET
authentication  = NONE
```

Actions:

```text
POST /api/v1/partner/auth/send_otp
  phoneNumber = $context(authFlow.phoneNumber)
  deviceId    = $context(deviceId)
responseMode = none

POST /api/v1/partner/auth/verify_otp
  challengeId = $response(data.challengeId)
  phoneNumber = $context(authFlow.phoneNumber)
  otp         = $binding(otp)
  deviceId    = $context(deviceId)
responseMode = destination
```

No Login/OTP-specific Store, renderer, or navigation branch is authorized.

## 10. Verification Baseline

Owner-provided 2026-10-04 commands:

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Result: **BUILD SUCCESSFUL — 54 actionable tasks: 15 executed, 39 up-to-date**.

This is only the JVM baseline. It does not certify Bootstrap Option B, full Dynamic runtime, DYNAMIC-07 production implementation, Login → OTP end-to-end behavior, or platform verification.

## 11. Full Gap Register

| Area | State | Required action |
|---|---|---|
| Base architecture 001–017 | FROZEN | Do not reopen |
| DYNAMIC-01 → DYNAMIC-07 | FROZEN | Do not reopen |
| Navigation foundation | IMPLEMENTED | Final runtime verification pending |
| Dynamic container | IMPLEMENTED | Final runtime verification pending |
| Dynamic Store | IMPLEMENTED / REVIEW PENDING | Verify literal Pure Store contract |
| Dynamic use cases | IMPLEMENTED / PACKAGE CLEANUP | Move to feature `usecase` package |
| Registry | IMPLEMENTED | Final compliance review |
| Renderer | IMPLEMENTED | Final compliance review |
| Dynamic repository | IMPLEMENTED | Runtime/integration verification |
| Dynamic remote source | IMPLEMENTED | Auth/request metadata verification |
| Dynamic mapper | IMPLEMENTED | Backend-shaped integration verification |
| Bootstrap API contract | KNOWN / DOCUMENTED | Keep as source of truth |
| Bootstrap repository | REQUIRED | Implement Option B |
| Bootstrap repository implementation | REQUIRED | Implement Option B |
| Bootstrap remote source | REQUIRED | Implement Option B |
| DefaultApplicationBootstrap | TRANSITIONAL | Remove/rework direct HTTP path |
| Bootstrap DI/tests | PENDING | Update after boundary change |
| Request authentication metadata | PENDING | Implement through frozen network boundary |
| Dynamic references/state/actions | PARTIAL | Complete frozen contract |
| DYNAMIC-07 fallback/recovery/observability | PARTIAL | Complete production implementation |
| Login → OTP integration | PENDING | Backend-shaped end-to-end tests |
| Navigation push/pop/replace/back | PENDING | Full runtime verification |
| Android | PENDING | Platform verification |
| iOS/macOS-hosted | PENDING | Platform verification where supported |
| Web/Wasm | PENDING | Platform verification |
| Literal plan-to-source review | PENDING | Complete before freeze |
| Final Dynamic review/freeze | PENDING | Final gate |

## 12. Forbidden / Do Not Reintroduce

- No ViewModel.
- No hardcoded Login/OTP/Dashboard branching.
- No navigation decisions based on `templateType`.
- No second Dynamic state owner for bound fields.
- No giant SDUI validator/compatibility engine.
- No reflection/package scanning for Dynamic registry.
- No silent type substitution/property downgrade.
- No generic manager/coordinator/router/factory framework without concrete responsibility.
- No fake backend/JSON to hide missing contracts.
- No new module merely because a class feels large.
- No changes to frozen architecture without the reopening process.

## 13. Recovery Order

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/04-CURRENT-STATUS.md`
4. `docs/project-management/12-PROJECT-TRACKER.md`
5. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
6. `docs/architecture/BASE-ARCHITECTURE.md` only for frozen base decisions
7. `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
8. `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`
9. `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`
10. Relevant source/tests only after state is understood.

## 14. Next Valid Action

**Implement Option B Bootstrap boundary first. Then continue every remaining gap in the ordered Dynamic implementation plan. Do not mark the feature complete because the JVM baseline is green.**
