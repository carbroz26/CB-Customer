# CB-Partner — Base Architecture Decision Record

**Status:** DOCUMENTED — DECISION-FROZEN RECORD THROUGH BASE-ARCH-017  
**Scope:** Frontend repository only  
**Purpose:** Authoritative durable record of the Base Architecture research and decisions completed through BASE-ARCH-017. This document is implementation input. Implementation must follow the frozen decisions and must not reinterpret them from historical chat.

> **Important:** BASE-ARCH-001–017 are decision-frozen. BASE-ARCH-001–003 are frozen foundation decisions; BASE-ARCH-004–012 build on those foundation decisions. Exact implementation APIs, versions, and other items explicitly marked deferred remain open until the applicable implementation-planning stage.

---

## 1. Authority and Implementation Use

The implementation phase must use this document together with:

1. `AI_START_HERE.md`
2. `docs/project-management/04-PROJECT-MASTER-STATE.md`
3. `docs/project-management/04-PROJECT-MASTER-STATE.md`
4. the approved implementation plan created after Base Architecture is complete
5. the relevant implementation-status document
6. actual Git/Trello state

Implementation MUST NOT rely on historical conversation as an architecture source.

If implementation reveals a conflict with a frozen decision:

**STOP → REPORT → DISCUSS → DECIDE → FREEZE → UPDATE PLAN → IMPLEMENT**

No silent architecture change is permitted.

---

# BASE-ARCH-001 — Project and Gradle Module Structure

**State:** DECISION_FROZEN  
**Freeze state:** Frozen as the foundational project/module-structure decision.

## Research focus

- Large-project Kotlin Multiplatform / Compose Multiplatform structure.
- Current JetBrains/Kotlin project structure.
- Platform entry-point separation.
- Gradle multi-project organization.
- Convention plugins / `build-logic`.

## Research conclusion

The project should use a scalable multi-module Kotlin Multiplatform structure with explicit platform application entry points and shared architecture modules. Current JetBrains/Kotlin guidance supports separating shared code from Android, iOS and desktop entry points rather than blindly adopting an old single shared `composeApp` structure.

Gradle convention plugins belong in an included `build-logic` build for scalable build conventions.

## Frozen foundation decision

The researched structure is accepted and frozen as the foundation for the later Base Architecture decisions. Implementation must use this structure unless a later approved architecture change explicitly reopens it.

## Direction carried forward

The repository structure below identifies **platform application boundaries and shared architecture areas**. A platform application boundary is not required to be a Gradle module; its build-system representation is platform-specific.

Platform boundary rule:
- `androidApp/` → Android application boundary implemented as Gradle project `:androidApp`.
- `desktopApp/` → Desktop application boundary implemented as Gradle project `:desktopApp`.
- `iosApp/` → iOS application boundary owned by Xcode; it is **not** a Gradle project and must not be represented as `:iosApp`.

```text
CB-Partner/
├── androidApp/
├── iosApp/
├── desktopApp/
├── core/
├── domain/
├── data/
├── navigation/
├── feature/
│   ├── splash/
│   └── dynamic/
├── build-logic/
├── gradle/
└── docs/
```

No additional top-level `shared/` module is required merely to contain the architecture modules above.

---

# BASE-ARCH-002 — Module Responsibilities and Dependency Graph

**State:** DECISION_FROZEN  
**Freeze state:** Frozen as the foundational module-responsibility and dependency-boundary decision.

## Frozen foundation decision

The researched responsibility model and dependency direction are accepted and frozen. Later architecture units may refine contracts within these boundaries but must not reverse the layer direction without an explicit architecture reopening.

## Responsibility model

- Platform application boundaries: thin platform-specific entry points. Android and Desktop are Gradle application projects; iOS is an Xcode application boundary.
- Core: shared technical foundation.
- Domain: generic domain/business abstractions and logic; no current business-specific feature architecture.
- Data: generic data-access implementations.
- Feature: application-facing UI and Pure Store/presentation.
- Navigation: application navigation.
- DI: composition root/object graph; currently not a standalone Gradle module.
- Build logic: Gradle conventions in the included build.

## Dependency direction carried forward

```text
Feature → Domain
Feature → Core
Feature → Navigation

Data → Domain
Data → Core

Domain → Core

Navigation → Core

DI → Core
DI → Domain
DI → Data
DI → Navigation
DI → Feature

Platform Apps → shared/application modules
```

## Forbidden dependencies

```text
Feature -X-> Data
Domain -X-> Data
Domain -X-> Feature
Data -X-> Feature
Core -X-> Domain/Data/Feature/DI
Domain -X-> DI
Data -X-> DI
Feature -X-> DI
Domain -X-> Ktor/Compose UI/platform APIs
```

## Business-scope exclusion

Do not create base-architecture modules/folders for authentication, booking, payment, dashboard, OTP or other business capabilities at this stage. Those future capabilities must not dictate the base architecture.

---

# BASE-ARCH-003 — Gradle Module Granularity

**State:** DECISION_FROZEN  
**Freeze state:** Frozen as the foundational module-granularity decision.

## Frozen foundation decision

The researched module granularity rules and initial module set are accepted and frozen. New modules require concrete ownership/build/dependency/feature justification and must not be introduced speculatively.

## Direction carried forward

Initial Gradle modules:

```text
:core
:domain
:data
:navigation
:feature:splash
:feature:dynamic
:androidApp
:desktopApp
```

`build-logic/` is a separate included Gradle build.

`iosApp/` is intentionally **not** a Gradle module. It is the native iOS/Xcode application boundary corresponding to the iOS platform. Application-boundary symmetry is required across Android, Desktop and iOS; Gradle-module symmetry is not.

## Module-granularity rules

- Avoid module explosion.
- `:core`, `:domain`, and `:data` initially remain single modules with packages.
- `:feature:splash` and `:feature:dynamic` are justified feature boundaries.
- Use `implementation` by default.
- Use `api` only when a public API genuinely requires it.
- Do not create `:di`, `:store`, `:mvi`, or `:presentation-core` modules without a later concrete boundary justification.
- Module boundaries must represent real ownership, dependency control, build concerns, platform separation, or feature isolation.

---

# BASE-ARCH-004 — Core Architecture and Technical Infrastructure

**State:** DECISION_FROZEN

## Accepted

- Single `:core` Gradle module.
- Package-level organization:
  - `common/`
  - `coroutine/`
  - `error/`
  - `logging/`
  - `network/`
  - `serialization/`
  - `time/`
- Ktor Client for networking foundation.
- `kotlinx.serialization` for serialization.
- `kotlinx.coroutines` for coroutine infrastructure.
- Kermit for logging.
- Kotlin `Result<T>` as the generic success/failure primitive.
- Technical error handling in Core; business errors outside Core.
- Compose Multiplatform Resources as the selected resource technology.
- Resources are generally owned by the consuming feature/module rather than centralized in Core.
- Prefer `kotlin.time`; use `kotlinx-datetime` only where needed.
- `kotlinx.collections.immutable` remains optional.
- `expect/actual` only for genuine platform-specific behavior.
- `implementation` by default; `api` only when public API requires it.

## Rejected

- Arrow as the baseline.
- Custom universal `AppResult`.
- Loading state inside `Result<T>`.
- Custom universal date/time abstraction.
- Giant utility framework.
- Separate Gradle module per Core package.
- Mandatory abstractions such as `NetworkClient`, `DispatcherProvider`, or `AppLogger` without a concrete need.

## Deferred

- Exact JSON configuration.
- Exact technical error hierarchy.
- Exact library/plugin versions for implementation planning.

## Core non-responsibilities

Core does not contain business-specific models, repositories, use cases, Feature UI, Store/MVI implementation, Navigation, DI, authentication, booking, payment, SDUI, dynamic JSON, or business-specific architecture.

---

# BASE-ARCH-005 — Navigation and Application Composition

**State:** DECISION_FROZEN

## Accepted

- Compose Multiplatform Navigation 3 is the primary navigation direction.
- Single `:navigation` module.
- Feature-owned destinations.
- Application-owned back stack.
- Store state and navigation state remain separate.
- UI → Store → controlled navigation flow.
- DI constructs dependencies.
- Application Root composes the application.
- Navigation owns navigation state.
- Platform apps remain thin entry points.
- Deep-link capability is accepted as a capability; exact routes are out of scope.
- Back handling belongs to the navigation system.

## Rejected as primary

- Decompose.
- Circuit.
- Voyager.
- Custom navigation framework.

These remain reference material where useful; they are not the selected primary architecture.

## Navigation responsibilities

`:navigation` owns:

- application navigation contracts/infrastructure
- destination integration
- back-stack integration
- navigation host/root orchestration
- destination registration
- serialization integration where required
- platform navigation behavior

It does NOT own:

- Feature presentation state
- Store logic
- business/domain logic
- repositories
- networking
- SDUI
- business models

## Deferred

- Exact Navigation 3 APIs.
- Exact destination-registration mechanism.
- Exact command/bridge API.
- Exact dependency version.

---

# BASE-ARCH-006 — Dependency Injection and Application Composition

**State:** DECISION_FROZEN

## Accepted

1. Koin is the primary DI technology.
2. Koin Compiler Plugin is accepted; exact versions remain implementation-planning work.
3. Constructor injection is the primary class design.
4. Application Composition Root is the central composition boundary.
5. Long-lived application infrastructure may be application scoped.
6. Store lifetime is NOT automatically singleton.
7. Feature-specific scopes are not automatic.
8. Platform dependencies are supplied through appropriate platform boundaries.
9. Unit tests should use constructor-based replacement and should not require Koin.
10. DI graph verification is separate from ordinary unit tests.
11. DI remains a composition mechanism, not an architectural/business layer.
12. Logical DI definitions may remain module-local with application-level assembly.

## Rejected

- Metro as primary.
- kotlin-inject as primary.
- Kodein as primary.
- A standalone `:di` Gradle module for now.
- Classes calling Koin `get()`/`inject()` internally as the normal dependency mechanism.
- One giant DI/god module.

## Lifecycle rule

DI does not own:

- Store state
- navigation state
- business logic
- UI state
- feature lifecycle

---

# BASE-ARCH-007 — Feature and Pure Store Architecture

**State:** DECISION_FROZEN

## Frozen Store model

```text
UI
 ↓
Intent
 ↓
Store
 ↓
Executor
 ↓
Message
 ↓
Reducer
 ↓
State
 ↓
UI
```

The Store is the feature's presentation-state and behavior boundary.

## Accepted

- Pure Store.
- MVI/UDF.
- MVIKotlin as the Store implementation/reference technology.
- CB-Partner owns its architecture; MVIKotlin surrounding architecture is not adopted wholesale.
- Intent.
- State.
- Message.
- Pure Reducer.
- Executor for asynchronous side effects.
- Effect as the project vocabulary for navigation/one-off output; MVIKotlin Label maps conceptually to Effect.
- Navigation outside Store State.
- Feature/application composition owns Store lifecycle.
- Store lifecycle: Created → Started/Active → Disposed.
- Store must not be recreated by Compose recomposition.
- Feature-level Stores.
- `feature:splash`.
- `feature:dynamic`.
- Feature → Domain/Core/Navigation.
- Reducer testing.
- Executor/Store testing.
- UI testing separately.

## Rejected

- ViewModel.
- Decompose lifecycle as a second lifecycle framework.
- Global Store.
- Store singleton by default.
- Store-to-Store communication as the default architecture.
- `:store`, `:mvi`, or `:presentation-core` modules.
- Feature → Data.
- UI → Data.
- Reducer → Data/network.
- Reducer side effects.
- Core → Store implementations.
- Koin as a requirement inside Store unit tests.

## Frozen conceptual flow

```text
                         FEATURE
                            │
             ┌──────────────┴──────────────┐
             │                             │
            UI                           STORE
             │                             │
          Intent ─────────────────────────►│
             │                       ┌─────┴─────┐
             │                       │           │
             │                   Executor      Reducer
             │                       │           ▲
             │                       ▼           │
             │                    Message ───────┘
             │                                   │
             ◄──────────────────── State ────────┘
             │
             └────── Effect ──────► Navigation
```

## Deferred

- Exact Store API.
- Exact StateFlow exposure/observation strategy.

---

# BASE-ARCH-008 — Application Bootstrap and Startup Flow

**State:** DECISION_FROZEN

## Frozen composition

```text
Platform App
    ↓
Application Root
    ↓
DI
    ↓
Navigation
    ↓
Splash
    ↓
application bootstrap
```

The scope intentionally excludes authentication, booking, payment, SDUI internals, dynamic JSON, and business-specific startup architecture.

---

# BASE-ARCH-009 — Application Root and Platform Composition Boundary

**State:** DECISION_FROZEN

## Frozen principles

- Every supported platform has an explicit application boundary.
- A platform application boundary is not necessarily a Gradle module.
- Android: `androidApp/` is a thin Gradle application entry point represented by `:androidApp`.
- Desktop: `desktopApp/` is a thin Gradle application entry point represented by `:desktopApp`.
- iOS: `iosApp/` is a thin native Xcode application entry point and is not represented as `:iosApp`.
- Platform applications are thin entry points.
- Application Root is the shared application composition boundary.
- Application Root composes DI, Navigation and Features.
- Application Root contains no business logic.
- Platform-specific APIs must not leak into shared architecture layers.
- No separate `:app` module is required for the current architecture.
- Shared application composition is kept separate from platform entry-point mechanics.

---

# BASE-ARCH-010 — Application Startup Lifecycle and Initialization Ordering

**State:** DECISION_FROZEN

## Frozen ordering

```text
Platform entry point
    ↓
Application Root
    ↓
initialize DI / dependency graph
    ↓
initialize application navigation
    ↓
create/start Splash Store
    ↓
perform application bootstrap
    ↓
success → controlled navigation onward
```

Startup initialization must not be scattered arbitrarily across composables or platform modules.

The architecture does not define business-specific authentication, booking, payment, or SDUI initialization here.

---

# BASE-ARCH-011 — Application Startup Failure, Retry and Recovery Boundary

**State:** DECISION_FROZEN

## Frozen rules

- Startup/bootstrap failure belongs to Splash.
- Splash Store owns startup failure state.
- Retry is initiated through Intent.
- Retry creates a new bootstrap attempt.
- At most one bootstrap attempt is active for a Store.
- Retry does not restart DI, Application Root, Navigation, or the application.
- Normal coroutine cancellation is not presented as a user-facing failure.
- Store disposal cancels owned bootstrap work.
- Only the current active attempt may update current Store state.
- Structured concurrency is required.
- Manual retry is the default.
- Automatic retry is not yet frozen.
- Exact timeout, retry count and backoff are deferred.
- Business-specific error classification is out of scope.

---

# BASE-ARCH-012 — Application Startup State and Bootstrap Contract

**State:** DECISION_FROZEN

## Frozen contract

Bootstrap has a Domain-facing abstraction.

```text
Splash Store
    ↓
Domain bootstrap contract
    ↓
Data implementation
    ↓
Core Network
    ↓
Result<BootstrapOutput>
    ↓
Executor
    ↓
Message
    ↓
Reducer
    ↓
State / Effect
    ↓
UI / Navigation
```

The illustrative contract discussed was:

```kotlin
interface ApplicationBootstrap {
    suspend operator fun invoke(): Result<BootstrapData>
}
```

The exact interface name/API is NOT frozen. The responsibility is frozen.

## Accepted

- Domain-facing bootstrap abstraction.
- Data implements the bootstrap abstraction.
- Kotlin `Result<T>` as operation outcome.
- Loading belongs to Splash State.
- Bootstrap output is UI-independent.
- Feature maps bootstrap outcome to presentation State/Effect.
- Failure is represented in Splash State.
- Retry is initiated through Intent.
- One active bootstrap attempt.
- Bootstrap is cancellable.
- Store owns bootstrap lifecycle.
- Structured concurrency.
- Bootstrap success may produce an Effect without requiring persistent success state.

## Rejected

- Splash depending directly on Data.
- Custom universal `AppResult`.
- Loading encoded inside `Result<T>`.
- Bootstrap returning Compose/UI objects.
- Bootstrap knowing Navigation.
- Bootstrap returning navigation commands.
- Bootstrap knowing SDUI internals.
- Bootstrap defining dynamic JSON.
- Universal error hierarchy in Core.
- Success requiring persistent UI state.
- Detached/global bootstrap coroutine.

## Frozen conceptual startup state

```text
StartupState
├── Loading
└── Failure
```

Success may transition through an internal message and then Effect → Navigation rather than remaining as persistent Splash UI state.

## Deferred

- Exact `BootstrapOutput`/`BootstrapData` fields.
- Exact Domain interface/API.
- Exact error hierarchy and error mapping.
- Exact timeout.
- Automatic retry.
- Retry count/backoff.
- Backend/API contract.
- Authentication/session persistence.
- Business-specific startup behavior.

## Explicitly out of scope

- Authentication
- OTP
- Booking
- Payment
- Dashboard
- SDUI internals
- Dynamic JSON
- business-specific repositories/use cases
- business-specific error models

---

# BASE-ARCH-013 — Application Bootstrap Data Flow and Layer Ownership

**State:** DECISION_FROZEN

## Decision

**Option A — Direct ApplicationBootstrap contract** is selected and frozen.

The initial application bootstrap operation will use a direct Domain-facing ApplicationBootstrap contract. No separate bootstrap use-case and repository abstraction pair is introduced for the initial bootstrap operation.

## Frozen ownership model

```text
Splash Store
    ↓
Domain: ApplicationBootstrap
    ↓
Data: ApplicationBootstrap implementation
    ↓
Data: Remote Data Source
    ↓
Core: Network infrastructure
```

### Splash Store owns
- Bootstrap attempt lifecycle.
- Loading and failure presentation state.
- Retry Intent.
- Cancellation through Store lifecycle.
- Mapping operation outcomes into Store Messages/State/Effects.

### Domain owns
- The Domain-facing ApplicationBootstrap contract.
- The Domain/application-facing bootstrap output contract.

The exact interface name and API remain implementation-planning details.

### Data owns
- The implementation of ApplicationBootstrap.
- Remote data access.
- Transport DTOs.
- Transport-to-domain/application mapping.

### Core owns
- Generic Ktor/network infrastructure.
- Serialization infrastructure.
- Generic technical networking concerns.

### Navigation owns
- Navigation state.
- Back-stack/navigation execution.

Bootstrap itself must not know Navigation.

## Frozen data-flow boundary

```text
Backend response
      ↓
Data DTO
      ↓
Data mapping
      ↓
Domain/Application model
      ↓
ApplicationBootstrap result
      ↓
Splash Store
      ↓
Presentation State / Effect
```

Transport DTOs and backend JSON must not leak into Feature/Store presentation contracts.

## Architectural rationale

Option A is selected because bootstrap is an application-startup operation rather than a generic domain resource repository. A direct Domain contract preserves dependency inversion and testability while avoiding speculative use-case/repository ceremony for a single operation.

This does NOT reject use cases or repositories from CB-Partner generally. They may be introduced later when a concrete domain responsibility justifies them.

The frozen complexity-control rule remains:

> Introduce an abstraction when its responsibility is justified; do not introduce layers merely because a conventional architecture diagram contains them.

No new Gradle module is created by this decision.

## Explicitly rejected for the initial bootstrap

- BootstrapUseCase → BootstrapRepository as a mandatory two-abstraction chain.
- BootstrapRepository as the only Domain abstraction.
- Splash depending directly on Data implementations.
- Splash accessing Ktor or remote data sources.
- Transport DTOs in Feature/Store state.
- Bootstrap knowing Navigation.
- Business-specific repositories/use cases.
- SDUI internals or dynamic JSON design.

## Deferred

- Exact ApplicationBootstrap interface/API.
- Exact BootstrapOutput fields.
- Exact DTO structure.
- Exact backend endpoint/API contract.
- Exact error hierarchy/mapping.
- Timeout and retry policy.
- Authentication/session persistence.
- Business-specific bootstrap behavior.

## Implementation authorization

This decision freezes ownership only. It does NOT authorize application source implementation.

Implementation requires the separately frozen implementation plan defined by the project operating process.

---
---

# BASE-ARCH-014 — Minimum Gradle/KMP/Compose/Build-Logic Foundation

**State:** DECISION_FROZEN  
**Freeze state:** Frozen as the first Phase 1 Technical Foundation decision.

## Decision

The project will establish the minimum reproducible Gradle/Kotlin Multiplatform/Compose Multiplatform foundation required to operationalize the already-frozen CB-Partner architecture.

The foundation uses an included build-logic build with a minimal, type-oriented convention-plugin strategy. Build logic centralizes repeated build mechanics; it does not become a second application-architecture framework.

## Frozen foundation

### 1. Included build

build-logic/ is an included Gradle build used for convention plugins.

### 2. Convention strategy

Use the smallest practical set of type-oriented conventions based on genuine project/build responsibilities.

Initial convention categories:
- KMP shared-library convention.
- Compose Multiplatform convention for modules that actually use Compose.
- Android application convention for androidApp.
- Desktop application convention for desktopApp.

No convention is created solely because a library is used. Library-specific conventions for Ktor, Koin, Kermit, serialization, coroutines, Navigation, Store/MVI, bootstrap, SDUI, or other technologies are not part of the initial foundation unless a later concrete repeated build concern justifies one.

### 3. Module-to-convention direction

The initial intended mapping is:

| Module | KMP shared convention | Compose convention | Application convention |
|---|---:|---:|---|
| :core | Yes | No | No |
| :domain | Yes | No | No |
| :data | Yes | No | No |
| :navigation | Yes | Only if actually required | No |
| :feature:splash | Yes | Yes | No |
| :feature:dynamic | Yes | Yes | No |
| :androidApp | No | As required | Android |
| :iosApp | Platform entry boundary | As required | Dedicated iOS configuration |
| :desktopApp | No | As required | Desktop |

The exact implementation details and plugin IDs remain implementation-planning work.

### 4. Dependency/version management

Use gradle/libs.versions.toml as the central version catalog for dependency and plugin declarations.

The version catalog owns dependency/plugin coordinates and version declarations. Convention plugins own build configuration. These responsibilities must not be conflated.

The version catalog does not by itself constitute a complete dependency-resolution enforcement strategy; additional enforcement is deferred unless a concrete need requires it.

### 5. KMP/platform configuration

Use the current Kotlin Multiplatform/Android Gradle Plugin model rather than legacy KMP/Android configuration patterns.

Shared modules may target Android, iOS and Desktop/JVM where their dependency graph requires those targets. A module is not required to target every platform merely for symmetry.

Prefer the standard/default KMP source-set hierarchy. Custom intermediate source sets require a concrete shared-code/dependency justification.

### 6. Compose boundary

Compose configuration is a capability of modules that actually contain/use Compose UI.

Core, Domain and Data remain free of Compose dependencies unless a later explicitly justified requirement changes their responsibilities.

Do not create separate Compose conventions for individual features; one shared Compose convention is sufficient for the initial foundation.

### 7. Testing/build baseline

The foundation will provide only the common test/build configuration that is genuinely repeated across modules.

The build baseline must support reliable compilation and applicable test execution, but it does not define the complete future integration/platform testing strategy.

### 8. Complexity-control rules

The initial foundation must NOT introduce:
- a standalone build architecture module;
- common, shared, platform, store, mvi, presentation-core, or di modules;
- a large centralized build framework;
- one convention plugin per library/technology;
- speculative custom KMP source sets;
- speculative platform abstractions.

Existing BASE-ARCH-001 through BASE-ARCH-013 remain unchanged.

## Explicitly deferred

BASE-ARCH-014 does not decide:
- exact Gradle/Kotlin/Compose/library/plugin versions;
- exact convention plugin IDs/names and implementation classes;
- exact build-logic internal source layout;
- exact Android application configuration;
- exact iOS/Xcode integration details;
- exact Desktop packaging/distribution configuration;
- exact Store API or StateFlow exposure;
- exact Ktor/network implementation;
- exact Koin module organization;
- exact bootstrap API/output/DTO;
- fake JSON structure;
- complete testing/integration strategy;
- SDUI internals or dynamic JSON;
- business-specific architecture.

These must be resolved only in the appropriate later planning/research units.

## Implementation consequence

BASE-ARCH-014 freezes the build/project foundation direction only. It does not authorize application source implementation.

Before source implementation begins, the project operating process still requires a separately created and explicitly frozen implementation plan containing exact modules, plugin IDs, versions, convention implementation, dependency declarations, build tasks, verification commands, acceptance criteria, and prohibited changes.

## Scope boundary

The next implementation objective may use a minimal fake bootstrap response as an architecture-validation vertical slice after the foundation is established, but fake JSON is not part of BASE-ARCH-014 and must not become an architectural contract.

# BASE-ARCH-017 — Runtime Infrastructure Implementation Boundary

**State:** DECISION_FROZEN  
**Owner approval:** 2026-09-30

BASE-ARCH-017 freezes the implementation boundary for the runtime technical infrastructure selected by BASE-ARCH-015 and BASE-ARCH-016. It does not re-decide those technologies and does not authorize production source implementation.

## Frozen boundary

```text
Core
├── coroutine infrastructure
├── serialization infrastructure
├── logging infrastructure
└── generic network infrastructure

Data
└── concrete data-layer network/client construction

Navigation
└── Navigation 3 runtime integration

Application Composition
└── runtime infrastructure assembly
```

## Frozen decisions

1. **Distinct architecture unit:** BASE-ARCH-017 is a separate architecture unit for the runtime implementation boundary.
2. **Koin Compiler Plugin:** may be used where concrete implementation requires compiler-assisted validation/integration; it is not a blanket requirement for every Koin module. Any `:data` application must be justified by the implementation plan.
3. **Ktor HttpClient ownership:** Core owns generic networking capability/infrastructure; Data may own concrete client construction/composition required by Data implementation, without owning generic networking infrastructure.
4. **Ktor engine/client configuration:** engine selection, client configuration, serialization installation, timeouts, logging, response handling, retry behavior, and platform-specific engine wiring remain implementation-plan details.
5. **Navigation 3:** remains frozen as the selected navigation technology; 017 only defines its runtime integration boundary.

## Relationship and non-goals

BASE-ARCH-015 remains authoritative for runtime dependency/technical-infrastructure contracts. BASE-ARCH-016 remains authoritative for Koin composition/DI rules. BASE-ARCH-017 only defines the boundary between those frozen contracts and concrete runtime wiring.

No new Gradle module, `:di` module, ViewModel, global Store, business-specific architecture, SDUI internals, dynamic JSON, backend contract, or other business architecture is introduced by this decision.

## Implementation authorization

This decision is architecture-frozen only. It does **not** authorize implementation. A separately created and explicitly frozen implementation plan is required before source changes begin.

A pre-existing `feature/base-arch-017-runtime-infrastructure` branch contains implementation commits created before this decision freeze. Those changes are not retroactively authorized. They must be reconciled against BASE-ARCH-015, BASE-ARCH-016, and BASE-ARCH-017 during PLAN before merge or continued implementation.

---

# 16. Cross-Architecture Rules Frozen Through 017

The following rules must be preserved by later architecture and implementation work. BASE-ARCH-001–003 are part of this frozen foundation:

### Layer direction

```text
Platform Apps
    ↓
Application Root
    ↓
Feature / Navigation / DI composition

Feature → Domain
Feature → Core
Feature → Navigation

Data → Domain
Data → Core

Domain → Core

Navigation → Core

DI → Core, Domain, Data, Feature, Navigation
```

### Presentation

- Pure Store.
- MVI/UDF.
- No ViewModel.
- Reducer is pure.
- Executor owns asynchronous side effects.
- Navigation is outside Store State.
- Store lifecycle is explicit and composition-owned.

### Startup

- Platform entry point is thin.
- Application Root composes.
- DI initializes dependencies.
- Navigation initializes navigation.
- Splash owns bootstrap presentation state.
- Bootstrap is Domain-facing and UI-independent.
- Data implements bootstrap.
- Core provides technical infrastructure.
- Bootstrap cancellation follows Store lifecycle.
- Retry is a Splash Intent.

### Complexity control

- No speculative abstractions.
- No module explosion.
- No global Store.
- No god DI module.
- No business-specific architecture in Base Architecture.
- Deferred details remain deferred until the correct planning stage.

---

# 17. Explicit Base-Architecture Non-Goals

The following are intentionally NOT designed by BASE-ARCH-001–012:

- Authentication architecture.
- OTP architecture.
- Booking architecture.
- Payment architecture.
- Dashboard architecture.
- Business-specific repositories/use cases.
- Business-specific data sources.
- SDUI internals.
- Dynamic JSON schema.
- Template/component/section/group/element implementation.
- SDUI registry implementation.
- Server-driven action schema.
- Specific backend API contracts.
- Business-specific session/persistence model.

`feature:dynamic` is currently only the application-facing Dynamic boundary. The separate SDUI module and its internals will be designed later.

---

# 18. Deferred-Decision Register

The following items must NOT be silently invented during implementation:

- Exact Navigation 3 API/registration/bridge.
- Exact Store API.
- Exact StateFlow exposure strategy.
- Exact Koin/Kotlin/plugin versions.
- Exact bootstrap API/model.
- Exact BootstrapOutput fields.
- Exact technical/business error mapping.
- Timeout policy.
- Automatic retry policy.
- Retry count/backoff.
- Backend API contract.
- Persistence/session restoration.
- SDUI internals.

When one becomes necessary, create the appropriate research/decision/plan unit before implementation depends on it.

---

# 19. Implementation Contract

When Base Architecture is eventually complete, the implementation plan must translate this record into:

1. exact modules to create;
2. exact module dependencies;
3. exact package responsibilities;
4. exact library/plugin versions;
5. exact public interfaces;
6. exact Store contracts;
7. exact Application Root composition;
8. exact DI construction;
9. exact Navigation integration;
10. exact bootstrap implementation;
11. exact testing requirements;
12. exact implementation sequence;
13. acceptance criteria;
14. prohibited changes.

Until that implementation plan is explicitly frozen, **BASE-ARCH documentation is not authorization to write application source code.**

---

# 20. Current Status

```text
BASE-ARCH-001 → DECISION_FROZEN
BASE-ARCH-002 → DECISION_FROZEN
BASE-ARCH-003 → DECISION_FROZEN
BASE-ARCH-004 → DECISION_FROZEN
BASE-ARCH-005 → DECISION_FROZEN
BASE-ARCH-006 → DECISION_FROZEN
BASE-ARCH-007 → DECISION_FROZEN
BASE-ARCH-008 → DECISION_FROZEN
BASE-ARCH-009 → DECISION_FROZEN
BASE-ARCH-010 → DECISION_FROZEN
BASE-ARCH-011 → DECISION_FROZEN
BASE-ARCH-012 → DECISION_FROZEN
BASE-ARCH-013 → DECISION_FROZEN
BASE-ARCH-014 → DECISION_FROZEN
BASE-ARCH-015 → DECISION_FROZEN
BASE-ARCH-016 → DECISION_FROZEN
BASE-ARCH-017 → DECISION_FROZEN
```

**Current architecture unit:** BASE-ARCH-017 → IMPLEMENTING

**Implementation authorization:** GRANTED 2026-09-30.

**Current implementation branch:** `feature/base-arch-017-runtime-infrastructure-implementation`

**Current workflow stage:** RECONCILED IMPLEMENTATION COMPLETE → VERIFICATION PENDING.

The frozen BASE-ARCH-017 implementation plan is being executed without scope expansion. Merge/code-freeze remain later gates.


---

# BASE-ARCH-014-06 — Android KMP Namespace Convention

**State:** DECISION_FROZEN  
**Owner approval:** 2026-09-23

The Android namespace for every KMP shared-library module is owned centrally by `KmpConventionPlugin` under the 014-06 platform boundary.

## Frozen convention

```text
:core → com.carbroz.cbpartner.core
:domain → com.carbroz.cbpartner.domain
:data → com.carbroz.cbpartner.data
:navigation → com.carbroz.cbpartner.navigation
:feature:splash → com.carbroz.cbpartner.feature.splash
:feature:dynamic → com.carbroz.cbpartner.feature.dynamic
```

The namespace is derived deterministically from the Gradle module path using the base `com.carbroz.cbpartner`. Individual KMP module build files must not duplicate namespace configuration. The Android application namespace remains `com.carbroz.cbpartner.android`.

This decision changes no module boundaries, dependency direction, target matrix, or other BASE-ARCH-014 decisions.


# BASE-ARCH-017 PLAN FREEZE — 2026-09-30

**State:** PLAN_FROZEN

The implementation plan for BASE-ARCH-017 is now frozen:

`docs/architecture/BASE-ARCH-017-IMPLEMENTATION-PLAN.md`

The frozen plan resolves the four outstanding PLAN-stage decisions:
- `:data` does not apply the Koin Compiler Plugin for BASE-ARCH-017.
- Data owns concrete application `HttpClient` construction; initial configuration remains generic and minimal.
- Ktor `ktor-client-engine-defaults` is the multiplatform engine strategy; no project-owned `expect/actual` engine abstraction is introduced.
- The pre-existing 017 implementation branch is reconciled by resulting implementation state rather than blindly preserving its pre-freeze commit history.

The plan specifies exact dependency scope, Koin usage, Ktor ownership/configuration, Navigation 3 integration, tests, verification commands, acceptance criteria, prohibited changes, and branch reconciliation.

**Implementation authorization:** GRANTED 2026-09-30.

Implementation is complete on `feature/base-arch-017-runtime-infrastructure-implementation`. Verification, review, code freeze, and merge remain separate later gates.
