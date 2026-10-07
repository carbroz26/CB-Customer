# CB-Partner — Architecture

**Status:** AUTHORITATIVE / CURRENT
**Scope:** Frontend architecture only

## 1. Frozen technical direction

- Kotlin Multiplatform
- Compose Multiplatform
- Clean Architecture
- Pure Store-based MVI/UDF
- No ViewModel
- Multi-module architecture
- Gradle convention plugins
- Koin
- Ktor Client
- kotlinx.serialization
- kotlinx.coroutines
- Kermit
- Navigation 3

Frozen foundation decisions are recorded in `BASE-ARCHITECTURE.md`.

## 2. Current module topology

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
build-logic/       included Gradle build
iosApp/            native Xcode application boundary
```

There is intentionally no Gradle `:iosApp` module.

## 3. Layer responsibilities

### :core
Generic technical foundations. No business, Store, Navigation or DI ownership.

### :domain
Domain contracts, models, repository interfaces and use-case contracts where appropriate. No Ktor or Compose implementation.

### :network
Shared Ktor infrastructure, HttpClient capability/configuration boundary, RequestHandler, generic network results/errors and network DI.

### :data
DTOs, remote data sources, mapping, repository implementations and concrete data-layer network/client construction.

### :navigation
Navigation 3 integration, canonical navigation state and stack ownership. Features may request navigation; they do not create competing stacks.

### :feature:splash
Startup presentation and Store orchestration while Bootstrap runs.

### :feature:dynamic
Backend-driven Dynamic screen lifecycle, Store, actions, registries and rendering.

### :app
Application composition and shared application root.

### Platform boundaries
`androidApp/`, `webApp/`, `desktopApp/` when present are platform application boundaries backed by their actual Gradle configuration. `iosApp/` is native Xcode.

## 4. Dependency direction

```text
:domain → :core
:data → :domain, :core, :network
:navigation → :core
:feature:splash → :domain, :core, :navigation
:feature:dynamic → :domain, :core, :navigation
:app → application composition / feature integration
```

Features do not depend directly on Data for business operations. Data implements Domain repository contracts.

## 5. Startup architecture

```text
Platform Entry
  ↓
App / ApplicationContent
  ↓
Splash Store + UI
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
BootstrapMapper
  ↓
BootstrapOutput
  ↓
DynamicDestination
  ↓
DynamicNavigationState
  ↓
DynamicContainer
```

Splash and Bootstrap are one startup flow.

## 6. Canonical DynamicDestination

Bootstrap, Dynamic API responses and Dynamic actions use one destination type:

```text
DynamicDestination
├── screenId
├── templateId
├── templateType
└── endpoint
```

`templateId` is opaque destination identity. `templateType` selects rendering only. Business navigation never derives identity from template type.

HTTP method/authentication are request metadata, not destination identity.

## 7. Dynamic architecture

Dynamic is backend-driven and generic.

The authoritative Dynamic documents define:

- Template → Component → Section → Group → Element → Content hierarchy;
- explicit registries;
- entry-scoped Store lifecycle;
- typed DynamicValue state;
- generic RuntimeState;
- reference resolution;
- action capabilities;
- navigation effects;
- rendering boundaries;
- validation/fallback/diagnostic behavior.

Dynamic references are:

```text
$binding
$context
$state
$literal
```

`$response` is forbidden.

No native Login/OTP/Dashboard architecture is introduced.

## 8. Store and UDF

```text
UI
 ↓
Intent
 ↓
Store
 ↓
Executor / Use-case boundary
 ↓
Message / Result
 ↓
Reducer
 ↓
State
 ↓
UI
```

The Store owns state orchestration, not rendering, raw HTTP, navigation-stack mutation, URI launching or business-specific infrastructure.

## 9. Network boundary

```text
UI
 ↓
Store
 ↓
Use Case / feature orchestration
 ↓
Repository
 ↓
Remote Data Source
 ↓
RequestHandler
 ↓
Ktor HttpClient
```

Network exceptions distinguish HTTP status, connection, timeout, serialization and protocol failures. Business/API outcomes are not automatically transport exceptions.

## 10. Platform boundaries

### Android
`androidApp/` is the Android application boundary.

### Web
`webApp/` is the Web/Wasm application boundary.

### iOS
`iosApp/` is a native Xcode application boundary. It is not a Gradle module.

### Desktop
Desktop remains a platform boundary only where the current Gradle topology exposes it. Documentation must not invent a module that is absent from the actual project.

## 11. Architecture protection

Do not introduce:

- ViewModel;
- a second Store/lifecycle architecture;
- a second navigation framework;
- a generic universal networking abstraction without a concrete need;
- business logic in composables;
- Dynamic-specific HTTP/navigation/cache infrastructure;
- modules created only for symmetry;
- dependencies without a real consumer.

## 12. Related authorities

- Frozen foundation: `docs/architecture/BASE-ARCHITECTURE.md`
- Runtime dependency contract: `docs/architecture/BASE-ARCH-015-DECISION-RECORD.md`
- Runtime implementation boundary: `docs/architecture/BASE-ARCH-017-DECISION-RECORD.md`
- Runtime implementation plan: `docs/architecture/BASE-ARCH-017-IMPLEMENTATION-PLAN.md`
- Dynamic: `docs/dynamic/`
- Startup: `docs/features/splash-bootstrap/SPLASH-BOOTSTRAP.md`
- API: `docs/api/API_CONTRACT.md`
- Build: `docs/build/GRADLE-SETUP-AND-WORKFLOW.md`
