# CB-Partner Dynamic UI — Implementation Plan

**Status:** PLAN FROZEN — IMPLEMENTATION AUTHORIZED
**Documentation reconciliation:** 2026-10-04

This update reconciles the frozen plan with the source and verification that actually exist on `feature/dynamic-ui`. It does not change DYNAMIC-01 → DYNAMIC-07 decisions.

## 1. Frozen Architecture

```text
Platform entry
  ↓
App
  ↓
ApplicationContent
  ↓
Navigation 3 boundary
  ↓
DynamicNavigationState
  ↓
DynamicContainer
  ↓
DynamicStore
  ↓
LoadDynamicScreenUseCase / ExecuteDynamicActionUseCase
  ↓
DynamicRepository
  ↓
DynamicRemoteDataSource
  ↓
Network RequestHandler
  ↓
Ktor HttpClient
```

One `DynamicDestination` is the canonical navigation/back-stack entry:

```text
screenId
 templateId
templateType
 endpoint
 method
authentication
```

`templateType` selects rendering capability only. It never decides business navigation.

## 2. Clean Architecture Boundary

```text
:feature:dynamic → :domain, :core, :navigation
:feature:splash  → :domain, :core, :navigation
:data            → :domain, :core, :network
:domain          → :core
:network         → Ktor + Koin
```

Feature UI/Store does not perform HTTP directly.

`:network` owns generic Ktor infrastructure. `:data` owns DTOs, mappers, repositories and feature-specific remote sources. Domain owns canonical contracts/models.

## 3. Shaadi Clean-Code Reference

The `LalitDev2305/Shaadi` project is a code-quality reference only. The useful lessons are:

- central HttpClient construction;
- small request boundary;
- small repository implementation;
- small use case;
- explicit mapper;
- constructor injection;
- minimal DI.

Do not copy Shaadi's Android/JVM-only Hilt, ViewModel, `Dispatchers.IO`, or CIO-only assumptions into KMP.

## 4. Implemented Source Map

### Application

- `app/src/commonMain/.../app/App.kt` — thin application entry/composition boundary.
- `app/src/commonMain/.../app/ApplicationContent.kt` — Splash → Dynamic composition; creates navigation state, registry, Dynamic Store and effects.

### Navigation

- `navigation/.../DynamicNavigationState.kt` — root/push/replace/pop of complete `DynamicDestination` values.
- `navigation/.../DynamicNavigationHost.kt` — Navigation 3 UI boundary.

### Dynamic feature

- `feature/dynamic/.../DynamicContainer.kt` — current Dynamic destination container.
- `feature/dynamic/.../DynamicModule.kt` — Dynamic DI.
- `feature/dynamic/.../store/DynamicStore.kt` — Pure MVI/UDF state/intents/effects and use-case orchestration.
- `feature/dynamic/.../action/DynamicActionHandler.kt` — action capability boundary.
- `feature/dynamic/.../domain/usecase/LoadDynamicScreenUseCase.kt` — screen loading use case.
- `feature/dynamic/.../domain/usecase/ExecuteDynamicActionUseCase.kt` — action/reference resolution and action coordination.
- `feature/dynamic/.../registry/DynamicRegistry.kt` — explicit category registration/lookup.
- `feature/dynamic/.../renderer/DynamicRenderer.kt` — recursive definition resolution/rendering.

### Initial Dynamic definitions

- `sdui/template/StackTemplate.kt`
- `sdui/component/StackComponent.kt`
- `sdui/section/StackSection.kt`
- `sdui/group/StackGroup.kt`
- `sdui/element/TextElement.kt`
- `sdui/element/ImageElement.kt`

### Domain

- `domain/model/dynamic/DynamicModels.kt` — Dynamic destination/action/reference/response contracts.
- `domain/repository/DynamicRepository.kt` — Dynamic repository contract.
- `domain/bootstrap/ApplicationBootstrap.kt` — bootstrap contract.
- `domain/model/bootstrap/*` — bootstrap domain contracts/failures.

### Data

- `data/bootstrap/DefaultApplicationBootstrap.kt` — bootstrap request/orchestration and failure mapping.
- `data/bootstrap/BootstrapMapper.kt` — bootstrap DTO → domain, including canonical `DynamicDestination`.
- `data/dynamic/DynamicRepositoryImpl.kt` — Dynamic repository implementation.
- `data/dynamic/remote/DynamicRemoteDataSource.kt` — Dynamic HTTP source.
- `data/dynamic/model/DynamicResponseDto.kt` — Dynamic transport DTO.
- `data/dynamic/mapper/DynamicResponseMapper.kt` — explicit DTO → domain mapper.

### Network

- `network/http/RequestHandler.kt` — shared request boundary.
- `network/http/HttpClientBuilder.kt` — shared Ktor client construction.
- `network/http/DefaultError.kt` — transport error model.
- `network/NetworkException.kt`, `NetworkResult.kt`, `Response.kt`, `Paged.kt` — generic network contracts.
- `network/di/NetworkModule.kt` — network DI and shared client/request handler.

## 5. Implemented Dynamic Vocabulary

```text
stack_template
stack_component
stack_section
stack_group
text
image
```

No FormTemplate/InputElement/ButtonElement implementation is retained in the current Dynamic runtime.

## 6. Implemented Action Direction

Frozen action vocabulary:

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

Action execution remains separate from rendering.

## 7. Bootstrap → Login → OTP Contract

Bootstrap currently maps `startup.nextScreen` to `DynamicDestination`.

Known Login destination:

```text
screenId      = partner_login
templateId    = tpl_7K2M9Q
templateType  = form_template
endpoint      = /api/v1/partner/screen/auth_login
method        = GET
authentication = NONE
```

Known OTP destination:

```text
screenId      = partner_otp
templateId    = tpl_P6X8N3
templateType  = form_template
endpoint      = /api/v1/partner/screen/auth_otp
method        = GET
authentication = NONE
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

## 8. Implementation State

| Area | State |
|---|---|
| Navigation foundation | IMPLEMENTED |
| Application composition | IMPLEMENTED |
| Dynamic container | IMPLEMENTED |
| Dynamic Store | IMPLEMENTED |
| Dynamic use cases | IMPLEMENTED |
| Registry | IMPLEMENTED |
| Renderer | IMPLEMENTED |
| Initial definitions | IMPLEMENTED |
| Shared network module | IMPLEMENTED |
| Bootstrap → DynamicDestination | IMPLEMENTED / TESTED |
| Request error mapping | IMPLEMENTED / TESTED |
| DYNAMIC-07 runtime boundary | DISCUSSION FROZEN / IMPLEMENTATION PARTIAL |
| Full Login → OTP integration | PENDING |
| Full platform verification | PENDING |
| Final feature review/freeze | PENDING |

## 9. Latest Verification

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

The earlier non-success HTTP mapping test is now green.

## 10. Remaining Implementation Sequence

Continue without new unit-level approval gates:

1. Complete request metadata/authentication integration using the existing network/runtime boundary.
2. Complete Dynamic reference/state/action behavior required by the actual backend contract.
3. Implement the frozen DYNAMIC-07 runtime fallback/recovery/observability requirements.
4. Add backend-shaped Login → Send OTP → OTP → Verify OTP integration tests.
5. Verify full destination push/pop/replace/back behavior.
6. Verify Android, iOS/macOS-hosted and Web/Wasm targets where supported.
7. Perform literal plan-to-source compliance review.
8. Run complete feature test/build verification.
9. Review the complete Dynamic implementation.
10. Freeze the feature.

## 11. Definition of Done

```text
[ ] Frozen DYNAMIC-01 → DYNAMIC-07 contract fully implemented
[ ] one permanent DynamicContainer
[ ] full DynamicDestination navigation entries
[ ] no templateType-based business navigation
[ ] explicit registry with frozen vocabulary
[ ] recursive renderer boundary remains small
[ ] Pure MVI/UDF DynamicStore
[ ] no ViewModel
[ ] shared RequestHandler/network boundary
[ ] explicit DTO → domain mapping
[ ] structured request errors
[ ] frozen references/actions implemented
[ ] DYNAMIC-07 fallback/recovery/observability implemented
[ ] Login → OTP integration tests pass
[ ] Android verification pass
[ ] iOS/macOS-hosted verification pass where applicable
[ ] Web/Wasm verification pass
[ ] literal plan-to-source review pass
[ ] final Dynamic feature review accepted
[ ] feature code frozen
```
