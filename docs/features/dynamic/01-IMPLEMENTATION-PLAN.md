# CB-Partner Dynamic UI — Implementation Plan

**Status:** PLAN FROZEN — IMPLEMENTATION AUTHORIZED
**Documentation reconciliation:** 2026-10-04

This is the implementation source of truth after reconciliation with the frozen DYNAMIC-01 → DYNAMIC-07 decisions and the current backend Bootstrap/Login/OTP contracts. It does not reopen frozen Dynamic decisions.

## 1. Frozen Architecture

```text
Platform entry
  ↓
App
  ↓
ApplicationContent
  ↓
SplashStore / SplashScreen
  ↓
ApplicationBootstrap (domain startup contract)
  ↓
BootstrapRepository (domain repository contract)
  ↓
BootstrapRepositoryImpl (data)
  ↓
BootstrapRemoteDataSource (data)
  ↓
RequestHandler / Ktor HttpClient
  ↓
Bootstrap DTO → Mapper → BootstrapOutput
  ↓
BootstrapOutput.startup.nextScreen = DynamicDestination
  ↓
DynamicNavigationState
  ↓
DynamicContainer
  ↓
DynamicStore
  ↓
Dynamic use cases
  ↓
DynamicRepository
  ↓
DynamicRepositoryImpl
  ↓
DynamicRemoteDataSource
  ↓
RequestHandler / Ktor HttpClient
  ↓
DynamicResponseDto → DynamicResponseMapper → DynamicResponse
  ↓
DynamicRegistry / DynamicRenderer
```

Bootstrap and Splash are one startup flow. `Splash` is the visible startup UI; `Bootstrap` is the config/startup API execution performed while Splash is shown. They are not two independent product flows.

## 2. Clean Architecture Boundary

```text
:feature:splash  → :domain, :core, :navigation
:feature:dynamic → :domain, :core, :navigation
:data            → :domain, :core, :network
:domain          → :core
:network         → Ktor + Koin
```

Feature UI/Store does not perform HTTP directly.

`:network` owns generic Ktor infrastructure. `:data` owns DTOs, mappers, repository implementations and feature-specific remote sources. `:domain` owns canonical contracts/models and repository abstractions. Feature-level use cases remain feature orchestration when they depend on feature capabilities; they must not introduce a second Clean Architecture domain module.

## 3. Bootstrap Boundary — OPTION B FROZEN FOR IMPLEMENTATION

The previously implemented direct `DefaultApplicationBootstrap → RequestHandler` path is superseded by this refined implementation boundary:

```text
feature:splash
    ↓
ApplicationBootstrap
    ↓
BootstrapRepository
    ↓
data/BootstrapRepositoryImpl
    ↓
data/BootstrapRemoteDataSource
    ↓
network/RequestHandler
```

Responsibilities:

- `ApplicationBootstrap` — startup-facing domain contract used by Splash/application composition.
- `BootstrapRepository` — domain abstraction for obtaining the canonical bootstrap result.
- `BootstrapRepositoryImpl` — data orchestration; coordinates the remote source and mapper/domain conversion.
- `BootstrapRemoteDataSource` — transport-facing bootstrap API source; owns endpoint/method/request-header details and DTO acquisition only.
- `BootstrapMapper` — explicit DTO → domain mapping and validation.
- Bootstrap DTO/model classes — transport representation only; no Compose/UI/navigation implementation.

The existing frozen BASE-ARCH-012 startup responsibility remains unchanged: Splash executes the startup/bootstrap contract, Bootstrap determines startup state, and the resulting `DynamicDestination` enters the Dynamic navigation system. Option B only refines the Data boundary so Bootstrap follows the same repository/remote-source layering as Dynamic.

## 4. Actual Bootstrap API Contract

```text
GET /api/v1/partner/config/bootstrap
```

Headers:

```text
X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

Success response contains:

```text
status
code
message
data.config
  version
  maintenance
  update
  features
data.startup
  authenticated
  nextScreen
traceId
```

`nextScreen` is mapped to the canonical `DynamicDestination`:

```text
screenId
templateId
templateType
endpoint
method
authentication
```

The previously documented Bootstrap blocker stating that endpoint/method/authentication contract was unknown is obsolete and must not remain in active implementation documentation.

## 5. Dynamic Clean Architecture Boundary

```text
feature:dynamic
 ├── Store / UI / renderer / registry / action capability
 └── feature orchestration use cases
          ↓
       :domain
          ↓
 DynamicRepository
          ↑
       :data
 ├── DynamicRepositoryImpl
 ├── DynamicRemoteDataSource
 ├── Dynamic DTO/model
 └── DynamicResponseMapper
          ↓
       :network
```

`LoadDynamicScreenUseCase` may remain a feature-level use case because it is a thin orchestration operation over the domain repository.

`ExecuteDynamicActionUseCase` must remain feature-level because it coordinates feature-owned `DynamicActionHandler` capabilities. Moving it into top-level `:domain` would invert the dependency direction. Do not create `domain/usecase` inside `:domain` merely because a class is named UseCase.

Current package path `feature/dynamic/.../domain/usecase/` is a naming cleanup item. The target package is `feature/dynamic/usecase/` after implementation reconciliation; this is a package-organization change, not a change to the use-case responsibilities.

## 6. Shaadi Clean-Code Reference

The `LalitDev2305/Shaadi` project is a code-quality reference only:

- central HttpClient construction;
- small request boundary;
- small repository implementation;
- small use case;
- explicit mapper;
- constructor injection;
- minimal DI.

Do not copy Shaadi's Android/JVM-only Hilt, ViewModel, `Dispatchers.IO`, or CIO-only assumptions into KMP.

## 7. Implemented Source Map — Current State

### Application / Navigation

- `app/.../App.kt` — thin application entry/composition boundary.
- `app/.../ApplicationContent.kt` — Splash → Dynamic composition; creates navigation state, registry, Dynamic Store and effects.
- `navigation/.../DynamicNavigationState.kt` — root/push/replace/pop of complete `DynamicDestination` values.
- `navigation/.../DynamicNavigationHost.kt` — Navigation 3 UI boundary.

### Splash / Bootstrap — CURRENTLY PARTIAL, REFACTOR REQUIRED

Current classes that remain valid:

- `domain/bootstrap/ApplicationBootstrap.kt`
- `domain/model/bootstrap/BootstrapConfig.kt`
- `domain/model/bootstrap/BootstrapOutput.kt`
- `domain/model/bootstrap/StartupConfig.kt`
- `domain/model/bootstrap/BootstrapFailure.kt`
- `data/bootstrap/BootstrapMapper.kt`
- existing Bootstrap DTO/model definitions

Current direct implementation:

- `data/bootstrap/DefaultApplicationBootstrap.kt`

This direct implementation is not the final target. It must be refactored into:

```text
domain/bootstrap/ApplicationBootstrap.kt
domain/repository/BootstrapRepository.kt

data/bootstrap/BootstrapRepositoryImpl.kt
data/bootstrap/remote/BootstrapRemoteDataSource.kt
data/bootstrap/model/BootstrapResponseDto.kt
data/bootstrap/mapper/BootstrapMapper.kt
```

The exact DTO class names may be retained if already established; the boundary responsibilities above are mandatory.

### Dynamic feature

- `feature/dynamic/.../DynamicContainer.kt`
- `feature/dynamic/.../DynamicModule.kt`
- `feature/dynamic/.../store/DynamicStore.kt`
- `feature/dynamic/.../action/DynamicActionHandler.kt`
- `feature/dynamic/.../usecase/LoadDynamicScreenUseCase.kt`
- `feature/dynamic/.../usecase/ExecuteDynamicActionUseCase.kt`
- `feature/dynamic/.../registry/DynamicRegistry.kt`
- `feature/dynamic/.../renderer/DynamicRenderer.kt`

### Initial Dynamic definitions

- `sdui/template/StackTemplate.kt`
- `sdui/component/StackComponent.kt`
- `sdui/section/StackSection.kt`
- `sdui/group/StackGroup.kt`
- `sdui/element/TextElement.kt`
- `sdui/element/ImageElement.kt`

### Domain

- `domain/model/dynamic/DynamicModels.kt`
- `domain/repository/DynamicRepository.kt`
- `domain/repository/BootstrapRepository.kt` — to be added as part of the Bootstrap refinement.
- `domain/bootstrap/ApplicationBootstrap.kt`
- `domain/model/bootstrap/*`

### Data

Dynamic:

- `data/dynamic/DynamicRepositoryImpl.kt`
- `data/dynamic/remote/DynamicRemoteDataSource.kt`
- `data/dynamic/model/DynamicResponseDto.kt`
- `data/dynamic/mapper/DynamicResponseMapper.kt`

Bootstrap target:

- `data/bootstrap/BootstrapRepositoryImpl.kt`
- `data/bootstrap/remote/BootstrapRemoteDataSource.kt`
- `data/bootstrap/model/*`
- `data/bootstrap/mapper/BootstrapMapper.kt`

### Network

- `network/http/RequestHandler.kt`
- `network/http/HttpClientBuilder.kt`
- `network/http/DefaultError.kt`
- `network/NetworkException.kt`, `NetworkResult.kt`, `Response.kt`, `Paged.kt`
- `network/di/NetworkModule.kt`

## 8. Dynamic Vocabulary

Frozen initial vocabulary:

```text
stack_template
stack_component
stack_section
stack_group
text
image
```

No FormTemplate/InputElement/ButtonElement implementation is part of the current Dynamic runtime unless required by the actual fixed backend contract and deliberately added through the existing registry/definition model.

## 9. Frozen Action / Reference Vocabulary

Actions:

```text
request
navigate
present
dismiss
state
external_uri
sequence
```

References:

```text
$literal
$binding
$context
$response
```

Action execution remains separate from rendering.

## 10. Bootstrap → Login → OTP Contract

Bootstrap currently provides the first Dynamic destination. Known Login destination:

```text
screenId       = partner_login
templateId     = tpl_7K2M9Q
templateType   = form_template
endpoint       = /api/v1/partner/screen/auth_login
method         = GET
authentication  = NONE
```

Known OTP destination:

```text
screenId       = partner_otp
templateId     = tpl_P6X8N3
templateType   = form_template
endpoint       = /api/v1/partner/screen/auth_otp
method         = GET
authentication  = NONE
```

OTP actions:

```text
POST /api/v1/partner/auth/send_otp
  phoneNumber = $context(authFlow.phoneNumber)
  deviceId = $context(deviceId)
  responseMode = none

POST /api/v1/partner/auth/verify_otp
  challengeId = $response(data.challengeId)
  phoneNumber = $context(authFlow.phoneNumber)
  otp = $binding(otp)
  deviceId = $context(deviceId)
  responseMode = destination
```

These remain generic Dynamic actions. Do not create Login/OTP-specific Store classes or screen renderers.

## 11. Implementation State

| Area | State |
|---|---|
| Base architecture | FROZEN |
| Dynamic DYNAMIC-01 → DYNAMIC-07 discussion | FROZEN |
| Navigation foundation | IMPLEMENTED |
| Application composition | IMPLEMENTED |
| Dynamic container | IMPLEMENTED |
| Dynamic Store | IMPLEMENTED / REQUIRES FINAL CONTRACT REVIEW |
| Dynamic use cases | IMPLEMENTED / PACKAGE CLEANUP PENDING |
| Registry | IMPLEMENTED |
| Renderer | IMPLEMENTED |
| Initial definitions | IMPLEMENTED |
| Shared network module | IMPLEMENTED / JVM VERIFIED |
| Dynamic repository/remote/mapper | IMPLEMENTED / JVM VERIFIED |
| Bootstrap API contract | KNOWN / DOCUMENTED |
| Bootstrap → DynamicDestination mapping | IMPLEMENTED / TESTED |
| Bootstrap repository/remote-source layering | NOT YET IMPLEMENTED — REQUIRED |
| DYNAMIC-07 runtime boundary | DISCUSSION FROZEN / IMPLEMENTATION PARTIAL |
| Request metadata/authentication | PENDING |
| Full frozen action/reference behavior | PENDING |
| Login → OTP integration | PENDING |
| Platform verification | PENDING |
| Final Dynamic review/freeze | PENDING |

## 12. Latest Verification

2026-10-04 owner-provided verification:

```powershell
.\gradlew.bat :network:compileKotlinJvm
.\gradlew.bat :data:compileKotlinJvm
.\gradlew.bat :feature:splash:compileKotlinJvm
.\gradlew.bat :feature:dynamic:compileKotlinJvm
.\gradlew.bat :app:compileKotlinJvm

.\gradlew.bat :network:jvmTest :data:jvmTest :domain:jvmTest :feature:splash:jvmTest :feature:dynamic:jvmTest
```

Result:

```text
BUILD SUCCESSFUL
54 actionable tasks: 15 executed, 39 up-to-date
```

This proves the current source compiles/tests at the JVM baseline. It does not mean the architectural gaps listed in this plan are complete.

## 13. Remaining Implementation Sequence

1. Implement Option B Bootstrap layering without changing startup responsibility.
2. Update Bootstrap DI/tests and verify the complete Splash → Bootstrap → DynamicDestination path.
3. Clean the Dynamic use-case package naming without moving feature-specific orchestration into `:domain`.
4. Complete request metadata/authentication integration through the existing network/runtime boundary.
5. Complete Dynamic state/reference/action behavior required by the actual backend contract, including all frozen action kinds that are applicable.
6. Implement the frozen DYNAMIC-07 fallback/recovery/observability requirements.
7. Add backend-shaped Login → Send OTP → OTP → Verify OTP integration tests.
8. Verify destination push/pop/replace/back behavior and responseMode destination transitions.
9. Verify Android, iOS/macOS-hosted and Web/Wasm targets where supported.
10. Perform literal plan-to-source compliance review, including every remaining gap.
11. Run complete feature test/build verification.
12. Final Dynamic review and code freeze.

## 14. Definition of Done

```text
[ ] Option B Bootstrap repository/data-source boundary implemented
[ ] Splash and Bootstrap documented/implemented as one startup flow
[ ] Bootstrap API headers/endpoint/response mapping verified
[ ] one permanent DynamicContainer
[ ] full DynamicDestination navigation entries
[ ] no templateType-based business navigation
[ ] explicit registry with frozen vocabulary
[ ] recursive renderer boundary remains small
[ ] Pure MVI/UDF DynamicStore verified against frozen contract
[ ] no ViewModel
[ ] shared RequestHandler/network boundary
[ ] explicit DTO → domain mapping for Bootstrap and Dynamic
[ ] structured request errors
[ ] frozen references/actions implemented where applicable
[ ] request authentication metadata enforced
[ ] DYNAMIC-07 fallback/recovery/observability implemented
[ ] Login → OTP integration tests pass
[ ] Android verification pass
[ ] iOS/macOS-hosted verification pass where applicable
[ ] Web/Wasm verification pass
[ ] literal plan-to-source review pass
[ ] final Dynamic feature review accepted
[ ] feature code frozen
```
