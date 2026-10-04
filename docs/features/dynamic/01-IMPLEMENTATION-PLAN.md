# CB-Partner Dynamic UI — Implementation Plan

## Status

**PLAN FROZEN — IMPLEMENTATION AUTHORIZED**

This document is the implementation contract for the current Dynamic refactor.

### Strict code rule

Every class must have one clear responsibility. Code must be minimal, explicit, readable, and production-clean. Do not add code merely to make a flow work. Do not add managers, coordinators, wrappers, factories, helpers, or abstractions without a concrete independent responsibility. Existing code is not protected merely because it exists; replace incorrect architecture when required.

After every implementation unit: inspect changed files → verify responsibility/dependencies/call sites/lifecycle → compile affected modules → run focused tests → inspect diff → continue. If a frozen decision conflicts with source, STOP → inspect → report → decide → update this plan → continue.

## 1. Platform and composition

Supported application platforms:

```text
Android
 iOS
 Web (Wasm)
```

There is no `desktopApp`.

Platform entry points are thin. Shared application composition owns the common runtime.

```text
Platform entry
    ↓
Shared App
    ↓
Application Composition
    ↓
Navigation 3
    ↓
One Dynamic Container
```

`App` composes the application. It must not contain Dynamic business logic, repository calls, HTTP construction, action execution, or Login/OTP/Dashboard branches.

### Splash / Bootstrap

Splash and Bootstrap are one startup feature, not two modules or two DI systems.

```text
feature:splash
    ↓
SplashStore
    ↓
Bootstrap use case/repository
    ↓
RemoteDataSource
    ↓
Network
```

Bootstrap is the startup mechanism/contracts used by Splash. Splash is the startup feature/UI boundary. There is no `feature:bootstrap` and no `bootstrapModule`.

Use Bootstrap terminology for contracts/implementations such as `BootstrapOutput` and `DefaultApplicationBootstrap`; use Splash terminology for feature/store/UI classes.

## 2. Navigation boundary

`:navigation` is the single shared Navigation 3 boundary. No second navigation abstraction/framework is allowed.

It owns only:

```text
navigation state/back stack
DynamicDestination NavKey integration
setRoot(destination)
push(destination)
replace(destination)
pop()
NavDisplay host
```

Do not create `NavigationManager`, `NavigationCoordinator`, `NavigationRouter`, or equivalent wrappers unless a concrete responsibility cannot be expressed by the minimal navigation boundary.

`DynamicDestination` is the canonical navigation key and stores the complete backend destination:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

Bootstrap produces startup information; it does not mutate Navigation 3. Application composition installs the root destination through the navigation boundary.

DynamicStore emits navigation effects/results. It does not mutate the back stack and does not import Navigation 3 APIs.

## 3. Dynamic Container

There is exactly one permanent shared Dynamic Container.

```text
Dynamic Container ≈ stable Activity-like host
Template         ≈ replaceable Fragment-like content
```

This is only a mental model; implementation is shared Compose Multiplatform UI, not Android Activities/Fragments.

The container remains stable while the current destination/template changes. It does not own navigation, call Ktor/repositories, execute actions, or contain business-screen branches.

## 4. Dynamic Store

`DynamicStore` is Pure MVI + UDF.

It owns:

```text
screen state
loading/error/success state
runtime/binding state
intent handling
use-case invocation
state emission
effects
```

It does not own:

```text
Ktor/HTTP
request serialization
common request metadata
navigation stack / Navigation 3
external URI launching
renderer recursion / registry lookup
business-screen branching
response-history subsystem
```

No ViewModel.

## 5. Clean-code reference: Shaadi

`LalitDev2305/Shaadi` is the code-quality reference. We adopt its useful style, not its Android/JVM-specific architecture.

The important lessons are:

```text
central HttpClient construction
small request boundary
small repository implementation
small use case
explicit mapping
constructor-injected dependencies
minimal DI
```

Shaadi's `ShaadiHttpClientBuilder` centralizes Ktor configuration; `RequestHandler` exposes small HTTP operations; the repository delegates to that boundary; its mapper keeps conversion explicit and local. fileciteturn427file0 fileciteturn429file0 fileciteturn430file0 fileciteturn434file0

Do not literally copy CIO-only engines, `Dispatchers.IO`, Hilt, ViewModel, or Android-only code. CB-Partner must retain KMP and the frozen `BASE-ARCH-017` platform engine strategy.

## 6. Network architecture

Target:

```text
network configuration
        ↓
one configured HttpClient
        ↓
one common request execution boundary
        ↓
RemoteDataSource
        ↓
repository
```

The configured `HttpClient` is created once by network/application infrastructure. Repositories never create another client.

The common request boundary should be small and explicit, equivalent in cleanliness to:

```text
get
post
put
delete
```

A single internal execution function may centralize common request/error handling. Do not duplicate Ktor configuration or exception parsing across repositories.

KMP engine selection follows the already-frozen Base Architecture decision. Do not copy Shaadi's CIO-only engine.

Ktor concerns belong to network infrastructure, not Store, use cases, or repositories.

## 7. RemoteDataSource

`RemoteDataSource` is the application's common request boundary.

```text
repository
    ↓
RemoteDataSource
    ↓
configured HttpClient
```

It owns request execution concerns only. It does not decide Dynamic actions, templates, navigation, or Store state.

## 8. Repository style

Repositories must be as small as the Shaadi repository pattern: constructor-injected dependencies, one explicit operation per domain capability, delegate the request, return the appropriate result.

```text
DynamicRepositoryImpl
    ↓
RemoteDataSource
    ↓
backend
```

Repository must not render, navigate, mutate Store, create HttpClient, or become a workflow engine.

## 9. Domain / Data boundary

Domain owns:

```text
DynamicDestination
DynamicAction
DynamicReference
DynamicRepository
Dynamic request/result models
Bootstrap contracts
```

Domain must not depend on Ktor, Compose, Koin, or Data DTOs.

Data owns:

```text
DTOs / serialization models
repository implementations
RemoteDataSource usage
DTO → domain mapping
network infrastructure
```

Dependency direction:

```text
Data → Domain
Feature → Domain
Domain ✗→ Data
```

Never import a concrete Data repository from Domain.

## 10. Mapper style

Use explicit, small DTO → domain mappers only where a real boundary exists.

Follow the useful Shaadi style: an obvious public mapping operation and small private nested mapping functions. No reflection-based generic mapper and no mapping mixed with networking, rendering, navigation, or Store state. fileciteturn434file0

Do not create a mapper for a trivial identity conversion.

## 11. Result / error boundary

Normalize Ktor/HTTP failures once at the network/data boundary:

```text
Ktor / HTTP failure
      ↓
network result/error
      ↓
RemoteDataSource
      ↓
repository/domain result
      ↓
use case
      ↓
Store state/effect
```

Do not duplicate HTTP exception parsing in every repository. Do not build a large error framework without a concrete requirement.

## 12. Dynamic flow

Bootstrap:

```text
SplashStore
  ↓
Bootstrap
  ↓
BootstrapOutput
  ↓
startup.nextScreen
  ↓
DynamicDestination
  ↓
Navigation.setRoot()
```

Current Dynamic screen:

```text
DynamicDestination
  ↓
LoadDynamicScreenUseCase
  ↓
DynamicRepository
  ↓
RemoteDataSource
  ↓
backend
  ↓
Dynamic screen domain result
  ↓
Dynamic Container
  ↓
DynamicRenderer
```

Backend-provided next destination:

```text
nextScreen
  ↓
DynamicDestination
  ↓
navigation effect/result
  ↓
Navigation boundary
```

Frontend never derives navigation from `templateType`.

## 13. Use cases

Only meaningful use cases are allowed.

`LoadDynamicScreenUseCase`:

```text
DynamicDestination → DynamicRepository → Dynamic screen result
```

`ExecuteDynamicActionUseCase`:

```text
Dynamic action + resolved runtime input
        ↓
action execution boundary
        ↓
result/effect for Store
```

No use case per action type. No generic workflow engine.

## 14. Registry / Renderer

Initial vocabulary is exactly:

```text
Template  → stack_template
Component → stack_component
Section   → stack_section
Group     → stack_group
Elements  → text, image
```

No speculative `form_template`, `input`, `button`, `row_*`, icon, divider, spacer, or other definitions.

`DynamicRegistry` owns registration and lookup only. No reflection, scanning, plugin framework, networking, navigation, or Store state.

`DynamicRenderer` remains small and recursive:

```text
DynamicRenderer
    ↓
registry lookup
    ↓
registered definition
    ↓
definition renders Compose structure
    ↓
children delegate back to DynamicRenderer
```

## 15. Actions / references

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
$binding
$context
$response
$literal
```

Common request metadata is supplied by common request infrastructure. `$binding` represents Dynamic runtime/input values.

No speculative action infrastructure or response-history subsystem.

## 16. Ownership

```text
:navigation
└── Navigation 3 boundary

:feature:splash
└── Splash startup runtime + Bootstrap usage

:feature:dynamic
├── DynamicContainer
├── DynamicStore
├── usecase/
├── renderer/
├── registry/
└── sdui/

data
├── network infrastructure
├── RemoteDataSource
├── repository implementations
├── DTOs
└── mappers

domain
├── canonical contracts
└── domain models
```

Reuse/refactor equivalent existing classes. Never create duplicate ownership.

## 17. Implementation units

### Unit 1 — Navigation foundation

Verify/refactor the minimal Navigation 3 boundary and complete destination stack operations.

### Unit 2 — Application composition + Dynamic Container

Establish:

```text
Platform entry → App → Navigation → one Dynamic Container
```

Remove Dynamic navigation/business orchestration from platform entry points and App.

### Unit 3 — Network infrastructure

Refactor network construction/execution toward the clean Shaadi-style boundary while preserving KMP and `BASE-ARCH-017`:

```text
one configured HttpClient
one request execution boundary
one RemoteDataSource
```

### Unit 4 — Repository/data cleanup

Make repository implementations small and explicit. Add only meaningful DTO/domain mappers.

### Unit 5 — Dynamic use cases

Implement/refactor the two meaningful Dynamic use cases and inject them into the Store.

### Unit 6 — Dynamic Store cleanup

Store only state/intent/effect/use-case coordination. Remove navigation mechanics and direct Ktor access.

### Unit 7 — Registry/renderer

Enforce frozen vocabulary and recursive renderer boundary.

### Unit 8 — Action/reference integration

Connect frozen actions/references without speculative infrastructure.

### Unit 9 — End-to-end flow

Verify:

```text
Bootstrap
 ↓
startup.nextScreen
 ↓
Navigation root
 ↓
one Dynamic Container
 ↓
Dynamic screen request
 ↓
JSON
 ↓
registry/renderer
 ↓
action
 ↓
next DynamicDestination
 ↓
push/replace
```

## 18. Per-unit gate

Every unit must pass:

```text
source inspection
↓
responsibility/dependency review
↓
compile affected modules
↓
focused JVM tests
↓
diff inspection
↓
next unit
```

No unrelated refactor is allowed during a unit.

## 19. Required tests

Navigation:

```text
root / push / replace / pop
complete destination preservation
refresh without new entry
DynamicDestination serialization
```

Store/use cases:

```text
Store invokes use cases
loading/success/error transitions
navigation emitted, not performed
external URI emitted separately
generic binding resolution
```

Network/data:

```text
GET/POST/PUT/DELETE
common request execution
error normalization
repository delegation
DTO → domain mapping
```

Registry/renderer:

```text
initial vocabulary
unsupported definition handling
duplicate registration handling
backend-order rendering
```

Integration:

```text
Bootstrap startup.nextScreen → navigation root
current destination → screen request
backend next destination → navigation operation
back → previous complete destination
```

## 20. Verification commands

```powershell
cd D:\CarBroz\CB-Partner
git status
git fetch origin
git pull --ff-only origin feature/dynamic-ui
git branch --show-current
git log -1 --oneline
git status
```

Do not add the existing untracked `kotlin-js-store/` unless explicitly determined to be required.

Focused compilation:

```powershell
.\gradlew.bat :navigation:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :domain:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
```

Focused tests:

```powershell
.\gradlew.bat :navigation:jvmTest :data:jvmTest :domain:jvmTest :feature:dynamic:jvmTest
```

When App/Web composition changes, also compile the affected `:app` and `:webApp` targets.

Windows cannot run iOS Simulator tests; the known disabled-target warning is expected.

## 21. Definition of Done

```text
[ ] Android/iOS/Web only
[ ] no desktopApp
[ ] one Navigation 3 boundary
[ ] DynamicDestination is canonical navigation key
[ ] one permanent Dynamic Container
[ ] DynamicStore is Pure MVI/UDF
[ ] DynamicStore does not own navigation
[ ] DynamicStore does not call Ktor
[ ] Splash is the startup feature; no separate bootstrap module
[ ] one configured HttpClient
[ ] one common request execution boundary
[ ] one RemoteDataSource
[ ] repositories are small and explicit
[ ] DTO/domain mapping is explicit and minimal
[ ] domain does not depend on data
[ ] registry contains only frozen vocabulary
[ ] renderer is small and recursive
[ ] no templateType-based business navigation
[ ] no speculative manager/coordinator/router framework
[ ] focused tests pass
[ ] affected modules compile
[ ] platform composition compiles where host-supported
```

## 22. Final code-quality rule

The standard is **Shaadi-level cleanliness adapted to CB-Partner**:

```text
one responsibility
small public API
constructor-injected dependencies
obvious data flow
no hidden work
no unnecessary abstraction
no duplicate ownership
```

If a class can be simpler without losing a required architectural boundary, simplify it. If a new class has no independently meaningful responsibility, do not create it. If existing code is wrong, replace it rather than protecting it.
