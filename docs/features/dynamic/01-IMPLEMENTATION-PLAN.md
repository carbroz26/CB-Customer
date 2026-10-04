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
Network RequestHandler
    ↓
Ktor HttpClient
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
DynamicRemoteDataSource
    ↓
Network RequestHandler
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
:network
    ├── NetworkException
    ├── NetworkResult
    ├── Paged
    ├── Response
    └── http
        ├── DefaultError
        ├── RequestHandler
        └── HttpClientBuilder
             ↓
          one HttpClient
             ↓
:data remote sources
             ↓
repository
```

`:network` is the standalone Ktor infrastructure module. Ktor client construction and generic request/error handling do not belong inside `:data`.

`RequestHandler` follows the Shaadi request-boundary pattern for GET/POST/PUT/DELETE, with only the CB-Partner-required request headers/status preservation added. `HttpClientBuilder` follows the same small builder pattern while remaining multiplatform by using the KMP/default Ktor engine instead of a CIO-only engine.

`data` consumes `RequestHandler`; feature-specific remote sources remain in `data`. There is no generic `RemoteDataSource` wrapper and no `NetworkConfig` in `data`.

## Domain/data boundary

Domain owns canonical contracts/models: `DynamicDestination`, actions/references, repositories, Dynamic request/result models, `ApplicationBootstrap`, and Bootstrap models.

Data owns DTOs, serialization, repository implementations, feature-specific remote sources, explicit DTO→domain mapping, and composition of domain/data flows.

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

## Unit 3 — Standalone Network Infrastructure

Target:

```text
:network
    ↓
HttpClientBuilder
    ↓
one HttpClient
    ↓
RequestHandler
    ↓
feature-specific data remote source
    ↓
repository / bootstrap
```

### Source inspection / Shaadi comparison

The previous implementation placed Ktor infrastructure inside `:data`:

```text
data/network/KtorClientBuilder
 data/network/NetworkConfig
 data/network/RemoteDataSource
 data/di/NetworkModule
```

Shaadi instead isolates generic network contracts and HTTP request infrastructure in a dedicated `network` module, with `http/DefaultError`, `http/RequestHandler`, and the HTTP client builder. Its outer network package contains `NetworkException`, `NetworkResult`, `Paged`, and `Response`.

CB-Partner now follows that clean responsibility boundary while adapting the implementation for KMP.

### Corrections implemented

1. Added standalone `:network` Gradle module.
2. Added `NetworkException`, `NetworkResult`, `Paged`, and `Response` under `com.carbroz.cbpartner.network`.
3. Added `http/DefaultError`, `http/RequestHandler`, and `http/HttpClientBuilder`.
4. Added Ktor content negotiation, JSON serialization, auth, logging, mock, and default-engine dependencies required by the network boundary.
5. Added `koin.core` to the standalone `:network` module because `networkModule` owns the network DI definitions.
6. Added `networkModule` inside `:network`; it creates the single configured `HttpClient` and the single `RequestHandler`.
7. Removed `data/di/NetworkModule`.
8. Removed `data/network/KtorClientBuilder`, `NetworkConfig`, and the generic `RemoteDataSource` wrapper.
9. Updated `App.kt` and `app/build.gradle.kts` so application composition consumes `:network` directly.
10. Updated `DefaultApplicationBootstrap` to use the shared `RequestHandler` while preserving bootstrap headers, JSON parsing, cancellation behavior, and HTTP failure mapping.
11. Updated `DynamicRemoteDataSource` to use `RequestHandler` directly and return `NetworkResult<String>`.
12. Updated `DynamicRepositoryImpl` to consume `NetworkResult` directly; no generic transport wrapper remains.
13. Removed the obsolete `RemoteDataSourceTest` and updated bootstrap transport tests to inject `RequestHandler`.
14. Added focused `RequestHandlerTest` coverage for successful and failed requests.
15. Preserved HTTP status information in `NetworkException` because Bootstrap has a required `BootstrapFailure.Http(statusCode)` contract.
16. Kept the network classes small and constructor-injected; no manager/coordinator/factory/helper layer was introduced.

### Current source dependency graph

```text
app
 ├── network
 ├── data
 ├── navigation
 └── features

network
 └── Ktor + Koin

data
 ├── network
 ├── domain
 └── core

feature:dynamic
 └── domain

feature:splash
 └── domain

domain
 └── core
```

### Verification gate

Implementation is applied on `feature/dynamic-ui`.

The latest local verification exposed one compile-only dependency omission in the new standalone network module: `NetworkModule.kt` uses Koin DSL but `network/build.gradle.kts` did not declare `libs.koin.core`. That required correction has now been applied.

Required commands after pulling the correction:

```powershell
cd D:\CarBroz\CB-Partner
git fetch origin
git pull --ff-only origin feature/dynamic-ui

.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

**Status: IMPLEMENTATION APPLIED / FINAL LOCAL COMPILE-TEST GATE REQUIRED**

Do not mark Unit 3 VERIFIED until the above commands pass.

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

.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :navigation:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :domain:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm
.\gradlew.bat :webApp:compileKotlinWasmJs

.\gradlew.bat :network:jvmTest :navigation:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
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
[ ] standalone :network module
[ ] one configured HttpClient
[ ] one common RequestHandler boundary
[ ] no generic RemoteDataSource wrapper
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
