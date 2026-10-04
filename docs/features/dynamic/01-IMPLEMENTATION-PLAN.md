# CB-Partner Dynamic UI — Implementation Plan

## Status

**PLAN FROZEN — IMPLEMENTATION AUTHORIZED**

This is the implementation contract. Implementation proceeds unit-by-unit; no new architecture discussion is introduced unless source inspection exposes a real conflict with a frozen decision.

### Strict implementation/code rule

- Every class has one clear responsibility.
- Code is minimal, explicit, readable, and production-clean.
- Do not add code only to make a flow work.
- Do not add managers, coordinators, routers, wrappers, factories, helpers, or abstractions without an independent responsibility.
- Existing code may be replaced when it violates the frozen boundary.
- After each unit: source inspection → implementation → compile → focused tests → diff inspection → next unit.
- Do not leave stale tests or dead code after a source change.

## 1. Frozen architecture

Supported platforms:

```text
Android
 iOS
 Web (Wasm)
```

There is no `desktopApp`.

Application flow:

```text
Platform entry
    ↓
App
    ↓
Application composition
    ↓
Navigation 3
    ↓
one Dynamic Container
```

`:navigation` is the only Navigation 3 boundary. `DynamicDestination` is the canonical navigation key and contains the complete backend destination:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

Navigation owns back stack/root/push/replace/pop and the NavDisplay host. It does not own business logic.

### Splash / Bootstrap

Splash and Bootstrap are one startup feature. Splash is the UI/runtime feature boundary; Bootstrap is the startup contract/data mechanism used by Splash. There is no separate `feature:bootstrap`.

```text
feature:splash
    ↓
SplashStore
    ↓
ApplicationBootstrap
    ↓
RemoteDataSource
    ↓
Network
```

Do not create separate startup flows for Splash and Bootstrap.

### Dynamic

There is one permanent `DynamicContainer`.

```text
DynamicDestination
    ↓
DynamicContainer
    ↓
DynamicStore
    ↓
LoadDynamicScreenUseCase
    ↓
DynamicRepository
    ↓
RemoteDataSource
```

`DynamicStore` is Pure MVI + UDF. It owns state, intents, use-case invocation, runtime bindings, and effects. It does not own Navigation 3, Ktor, rendering recursion, or external URI launching.

## 2. Shaadi code-quality reference

`LalitDev2305/Shaadi` is the clean-code reference. Adopt its useful implementation style, not its Android/JVM-specific architecture.

Required lessons:

```text
central HttpClient construction
small request boundary
small repository implementation
small use case
explicit mapper
constructor injection
minimal DI
```

The reference uses a central `ShaadiHttpClientBuilder`, a small `RequestHandler`, repositories that simply delegate, and explicit mappers. Do not copy CIO-only engine configuration, Hilt, `Dispatchers.IO`, ViewModel, or Android-only code into KMP.

## 3. Network/data boundary

Target:

```text
Network configuration
        ↓
one configured HttpClient
        ↓
one common request boundary
        ↓
RemoteDataSource
        ↓
repository
```

There must be one configured `HttpClient`. Repositories never construct one.

The common request boundary is intentionally small:

```text
get
post
put
delete
```

Ktor configuration belongs to network infrastructure. Dynamic features and Stores do not configure Ktor.

## 4. Domain/data boundary

Domain owns canonical contracts/models:

```text
DynamicDestination
DynamicAction
DynamicReference
DynamicRepository
Dynamic request/result models
ApplicationBootstrap
Bootstrap models
```

Data owns:

```text
DTOs
serialization
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

Mappers are explicit and small. No generic/reflection mapper. No mapper for trivial identity conversion.

## 5. Dynamic use cases

Only meaningful use cases exist:

```text
LoadDynamicScreenUseCase
ExecuteDynamicActionUseCase
```

No use case per action type and no generic workflow engine.

## 6. Dynamic registry/renderer

Frozen initial vocabulary:

```text
Template  → stack_template
Component → stack_component
Section   → stack_section
Group     → stack_group
Elements  → text, image
```

`DynamicRegistry` owns registration/lookup only. `DynamicRenderer` is small and recursive. No reflection, scanning, networking, navigation, or Store state in the registry/renderer.

## 7. Actions/references

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

No speculative response-history or action framework.

## 8. Implementation units

### Unit 1 — Navigation foundation

Minimal Navigation 3 boundary and complete destination stack operations.

**Status: implemented; continue verification only when a later change affects it.**

### Unit 2 — Application composition + Dynamic Container

Required flow:

```text
Platform entry → App → ApplicationContent → Navigation → one DynamicContainer
```

`App` is only the composition entry point. Runtime composition belongs in the application-composition boundary, not in the `App` entry function.

**Source finding:** `App.kt` previously contained Splash state handling, DynamicStore creation, Dynamic effect handling, registry creation, and DynamicNavigationHost wiring.

**Correction applied:** moved that runtime composition to `ApplicationContent.kt`; `App.kt` now only initializes the shared Koin composition and delegates to `ApplicationContent`.

**Test cleanup applied:**
- `DynamicStoreTest` now supplies the required `DynamicActionHandler` dependency.
- `SplashStoreTest` now uses canonical `DynamicDestination`; stale `NextScreenConfig` was removed.

**Status: implementation applied; local compile/test verification is the next gate.**

### Unit 3 — Network infrastructure

Refactor toward the Shaadi-style clean boundary:

```text
one configured HttpClient
one common request boundary
one RemoteDataSource
```

Inspect and simplify `NetworkModule`, `HttpClient` construction, `RemoteDataSource`, and Dynamic remote wrappers before implementation. Do not duplicate ownership.

**Status: next unfinished implementation unit.**

### Unit 4 — Repository/data cleanup

Make repositories as small as the Shaadi repository pattern. Remove unnecessary data-layer wrappers. Keep only meaningful DTO/domain mappers.

### Unit 5 — Dynamic use cases

Verify/refactor the two meaningful use cases and their dependencies.

### Unit 6 — Dynamic Store cleanup

Store only state/intent/effect/use-case coordination. No Ktor or Navigation 3.

### Unit 7 — Registry/renderer

Enforce the frozen vocabulary and recursive renderer boundary.

### Unit 8 — Action/reference integration

Connect the frozen action/reference contracts without speculative infrastructure.

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

## 9. Required verification gate

For every unit:

```text
source inspection
↓
responsibility/dependency review
↓
implementation
↓
compile affected modules
↓
focused JVM tests
↓
diff inspection
↓
update this plan
↓
next unit
```

No unrelated refactor is allowed during a unit.

## 10. Required tests

Navigation:

```text
root / push / replace / pop
complete destination preservation
refresh without new entry
DynamicDestination serialization
```

Store/use cases:

```text
loading/success/error transitions
Store invokes use cases
navigation is emitted, not performed
external URI is emitted separately
binding/context resolution
```

Network/data:

```text
GET/POST/PUT/DELETE
common request execution
error normalization
repository delegation
DTO → domain mapping
```

Integration:

```text
Bootstrap startup.nextScreen → navigation root
current destination → screen request
backend next destination → navigation operation
back → previous complete destination
```

## 11. Verification commands

```powershell
cd D:\CarBroz\CB-Partner
git status
git fetch origin
git pull --ff-only origin feature/dynamic-ui
git branch --show-current
git log -1 --oneline
git status
```

Do not add the existing untracked `kotlin-js-store/` unless explicitly required.

Focused compilation:

```powershell
.\gradlew.bat :navigation:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :domain:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm
.\gradlew.bat :webApp:compileKotlinWasmJs
```

Focused tests:

```powershell
.\gradlew.bat :navigation:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Windows cannot run iOS Simulator tests; the disabled-target warning is expected.

## 12. Definition of Done

```text
[ ] Android/iOS/Web only
[ ] no desktopApp
[ ] one Navigation 3 boundary
[ ] DynamicDestination is canonical navigation key
[ ] one permanent DynamicContainer
[ ] App is a thin composition entry point
[ ] Splash is the startup feature; Bootstrap is not a separate feature/module
[ ] one configured HttpClient
[ ] one common request boundary
[ ] one RemoteDataSource
[ ] repositories are small and explicit
[ ] DTO/domain mapping is explicit and minimal
[ ] domain does not depend on data
[ ] DynamicStore is Pure MVI/UDF
[ ] DynamicStore does not own Navigation 3 or Ktor
[ ] registry uses only frozen vocabulary
[ ] renderer is small and recursive
[ ] no templateType-based business navigation
[ ] no speculative manager/coordinator/router framework
[ ] focused tests pass
[ ] affected modules compile
[ ] platform composition compiles where host-supported
```

## 13. Final code-quality rule

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

If a class can be simpler without losing a required boundary, simplify it. If a new class has no independently meaningful responsibility, do not create it. If existing code is wrong, replace it instead of protecting it.
