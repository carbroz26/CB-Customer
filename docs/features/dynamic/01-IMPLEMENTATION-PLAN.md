# CB-Partner Dynamic UI — Implementation Plan

## Status

**PLAN FROZEN — IMPLEMENTATION AUTHORIZED**

Implementation is executed unit-by-unit. No architecture discussion unless actual source exposes a conflict with a frozen decision.

## Strict implementation/code rule

- Every class has one clear responsibility.
- Code must be minimal, explicit, readable, and production-clean.
- Do not add code only to make a flow work.
- No unnecessary manager/coordinator/router/wrapper/factory/helper/abstraction.
- Existing code may be replaced when it violates the frozen boundary.
- After every unit: inspect → implement → compile → focused tests → inspect diff → update this plan → continue.
- Never leave stale tests or dead code after a source change.

## Frozen architecture

Supported platforms: Android, iOS, Web/Wasm. No desktop app.

```text
Platform entry
    ↓
App
    ↓
ApplicationContent
    ↓
Navigation 3
    ↓
one DynamicContainer
```

`:navigation` is the only Navigation 3 boundary. `DynamicDestination` is the canonical navigation key and preserves the complete backend destination (`screenId`, `templateId`, `templateType`, `endpoint`, `method`, `authentication`).

Navigation owns root/push/replace/pop and NavDisplay, not business logic.

### Splash / Bootstrap

Splash and Bootstrap are one startup feature. Splash is the feature/runtime boundary; Bootstrap is its startup contract/data mechanism. There is no separate bootstrap feature/module.

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

### Dynamic

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

DynamicStore is Pure MVI/UDF. It owns state, intents, use-case invocation, runtime bindings, and effects. It does not own Navigation 3, Ktor, rendering recursion, or external URI launching.

## Shaadi clean-code reference

`LalitDev2305/Shaadi` is the implementation-quality reference, not an architecture to copy wholesale.

Adopt these lessons:

```text
central HttpClient construction
small request boundary
small repository implementation
small use case
explicit mapper
constructor injection
minimal DI
```

Do not copy Shaadi's Android/JVM-only Hilt, ViewModel, `Dispatchers.IO`, or CIO-only engine assumptions into KMP.

## Network/data boundary

```text
NetworkConfig
    ↓
one configured HttpClient
    ↓
one common RemoteDataSource
    ↓
repository
```

One configured HttpClient. Repositories never construct clients. Ktor configuration belongs to network infrastructure. Dynamic feature/Store code never configures Ktor.

## Domain/data boundary

Domain owns canonical contracts/models: `DynamicDestination`, actions/references, repositories, Dynamic request/result models, `ApplicationBootstrap`, and Bootstrap models.

Data owns DTOs, serialization, repository implementations, RemoteDataSource usage, explicit DTO→domain mapping, and network infrastructure.

```text
Data → Domain
Feature → Domain
Domain ✗→ Data
```

No reflection/generic mapper. No mapper for trivial identity conversion.

# Implementation units

## Unit 1 — Navigation foundation

Minimal Navigation 3 boundary and complete destination stack operations.

**Status: COMPLETE / IMPLEMENTED**

## Unit 2 — Application composition + Dynamic Container

Required flow:

```text
Platform entry → App → ApplicationContent → Navigation → one DynamicContainer
```

Correction applied: runtime Splash/Dynamic composition was removed from `App.kt` and moved to `ApplicationContent.kt`. `App.kt` is now only the composition entry point.

Test cleanup applied:
- `DynamicStoreTest` supplies `DynamicActionHandler`.
- `SplashStoreTest` uses canonical `DynamicDestination`; stale `NextScreenConfig` removed.

**Status: COMPLETE / IMPLEMENTED / VERIFIED**

Local verification reported:
- `:feature:splash:compileKotlinJvm` PASS
- `:feature:dynamic:compileKotlinJvm` PASS
- `:app:compileKotlinJvm` PASS
- `:navigation:jvmTest` PASS
- `:data:jvmTest` PASS
- `:domain:jvmTest` PASS
- `:feature:splash:jvmTest` PASS
- `:feature:dynamic:jvmTest` PASS

## Unit 3 — Network infrastructure

Target:

```text
NetworkConfig
    ↓
KtorClientBuilder
    ↓
one HttpClient
    ↓
RemoteDataSource
    ↓
DynamicRemoteDataSource
    ↓
repository
```

Source inspection compared CB-Partner with Shaadi's `ShaadiHttpClientBuilder` and `RequestHandler`.

### Findings

- CB-Partner had `NetworkModule` constructing a bare `HttpClient()`.
- `RemoteDataSource` owned URL resolution and used `bodyAsText()`; this triggered the Ktor internal-API compilation failure seen previously.
- `DynamicRemoteDataSource` is already a thin Dynamic-specific boundary and should not own Ktor configuration.
- `DynamicRepositoryImpl` already follows the desired small flow: remote source → DTO → mapper → domain.

### Corrections applied

1. Added `KtorClientBuilder` as the single client-construction boundary.
2. `NetworkModule` now constructs exactly one `HttpClient` through `KtorClientBuilder` and injects it into `RemoteDataSource`.
3. `RemoteDataSource` no longer owns `NetworkConfig` or URL concatenation; base URL ownership belongs to the client builder.
4. Replaced Ktor `bodyAsText()` usage with the public `body<String>()` API.
5. Updated `RemoteDataSourceTest` to match the simplified constructor and request boundary.
6. No change was made to `DynamicRemoteDataSource` because its current responsibility is already minimal and correct.

**Status: IMPLEMENTATION APPLIED — LOCAL COMPILE/TEST GATE PENDING**

Required verification:

```powershell
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm
.\gradlew.bat :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Do not continue to Unit 4 until this gate passes.

## Unit 4 — Repository/data cleanup

Make repositories as small as the Shaadi repository pattern. Remove only unnecessary data wrappers. Keep meaningful DTO/domain mappers.

**Status: PENDING**

## Unit 5 — Dynamic use cases

Verify/refactor the two meaningful use cases and their dependencies.

**Status: PENDING**

## Unit 6 — Dynamic Store cleanup

Store only state/intent/effect/use-case coordination. No Ktor or Navigation 3.

**Status: PENDING**

## Unit 7 — Registry/renderer

Enforce frozen vocabulary and small recursive renderer boundary.

**Status: PENDING**

## Unit 8 — Action/reference integration

Connect frozen action/reference contracts without speculative infrastructure.

**Status: PENDING**

## Unit 9 — End-to-end flow

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

**Status: PENDING**

# Verification gate

Every unit must follow exactly:

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

No unrelated refactor during a unit.

# Verification commands

```powershell
cd D:\CarBroz\CB-Partner
git fetch origin
git pull --ff-only origin feature/dynamic-ui

.\gradlew.bat :navigation:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :domain:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm
.\gradlew.bat :webApp:compileKotlinWasmJs

.\gradlew.bat :navigation:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Windows iOS Simulator disabled-target warnings are expected.

Do not add the existing untracked `kotlin-js-store/` unless explicitly required.

# Definition of Done

```text
[ ] Android/iOS/Web only
[ ] no desktopApp
[ ] one Navigation 3 boundary
[ ] DynamicDestination is canonical navigation key
[ ] one permanent DynamicContainer
[ ] App is thin
[ ] Splash is the startup feature; no separate bootstrap feature/module
[ ] one configured HttpClient
[ ] one common request boundary
[ ] one RemoteDataSource
[ ] repositories small and explicit
[ ] DTO/domain mapping explicit and minimal
[ ] domain does not depend on data
[ ] DynamicStore is Pure MVI/UDF
[ ] DynamicStore does not own Navigation 3 or Ktor
[ ] registry uses frozen vocabulary
[ ] renderer is small and recursive
[ ] no templateType-based business navigation
[ ] no speculative manager/coordinator/router framework
[ ] focused tests pass
[ ] affected modules compile
[ ] platform composition compiles where host-supported
```

# Final code-quality rule

**Shaadi-level cleanliness adapted to CB-Partner:**

```text
one responsibility
small public API
constructor-injected dependencies
obvious data flow
no hidden work
no unnecessary abstraction
no duplicate ownership
```

If a class can be simpler without losing a required boundary, simplify it. If a new class has no independent responsibility, do not create it. If existing code is wrong, replace it instead of protecting it.
