# CB-Partner Dynamic UI — Implementation Status

## Status

**IMPLEMENTATION IN PROGRESS — JVM BASELINE GREEN**

Discussion DYNAMIC-01 → DYNAMIC-07 is frozen. The complete implementation plan is frozen and authorized. This document records actual source/verification state only.

## 1. Discussion State

- DYNAMIC-01 — FROZEN
- DYNAMIC-02 — FROZEN
- DYNAMIC-03 — FROZEN
- DYNAMIC-04 — FROZEN
- DYNAMIC-05 — FROZEN
- DYNAMIC-06 — FROZEN
- DYNAMIC-07 — FROZEN

Canonical discussion: `00-FEATURE-DISCUSSION.md`

## 2. Completed Implementation

### Application / Navigation

1. `App.kt` is a thin application composition entry point.
2. `ApplicationContent.kt` composes Splash → Dynamic.
3. `DynamicNavigationState` owns the complete `DynamicDestination` back stack with root/push/replace/pop.
4. Dynamic rendering is hosted through one `DynamicContainer`.

### Dynamic Store / Use Cases

5. `DynamicStore` follows Pure MVI/UDF orchestration.
6. `DynamicStore` does not own Ktor or Navigation 3.
7. `LoadDynamicScreenUseCase` exists and is used for screen loading.
8. `ExecuteDynamicActionUseCase` exists and handles action/reference resolution/coordination.
9. Dynamic navigation and external URI are exposed as separate effects.
10. The Store no longer builds HTTP request transport details itself.

### Registry / Renderer

11. `DynamicRegistry` is restricted to the frozen initial vocabulary.
12. Duplicate registration is rejected.
13. `DynamicRenderer` is a small recursive rendering boundary.
14. `stack_template`, `stack_component`, `stack_section`, `stack_group`, `text`, and `image` implementations exist.
15. Removed obsolete `FormTemplate`, `InputElement`, and `ButtonElement` Dynamic implementations.

### Data / Network

16. Standalone `:network` module exists.
17. Shared `HttpClient`/`RequestHandler` infrastructure is in `:network`.
18. Dynamic data uses `RequestHandler` through `DynamicRemoteDataSource`.
19. Dynamic repository uses explicit DTO/domain mapping.
20. Transport headers are kept below the Dynamic/domain contract.
21. Bootstrap uses the shared `RequestHandler`.
22. Bootstrap mapper constructs the canonical `DynamicDestination`.
23. Duplicate Bootstrap `NextScreenConfig` destination modeling was removed.

### Bootstrap Integration

24. Bootstrap success data reaches `BootstrapOutput.startup.nextScreen` as `DynamicDestination`.
25. Bootstrap destination identity fields are preserved and tested.
26. Application composition can hand the Bootstrap destination into Dynamic navigation.

## 3. Latest Verification — 2026-10-04

Owner pulled `feature/dynamic-ui` and ran:

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

The earlier failure:

```text
BootstrapRemoteDataSourceTest.nonSuccessfulHttpStatusIsMapped
```

is resolved. The test MockEngine client is configured with `expectSuccess = true`, which makes HTTP failures follow the expected mapping path.

## 4. Current Code Map

### Application

- `app/.../App.kt`
- `app/.../ApplicationContent.kt`

### Navigation

- `navigation/.../DynamicNavigationState.kt`
- `navigation/.../DynamicNavigationHost.kt`

### Dynamic

- `feature/dynamic/.../DynamicContainer.kt`
- `feature/dynamic/.../DynamicModule.kt`
- `feature/dynamic/.../store/DynamicStore.kt`
- `feature/dynamic/.../action/DynamicActionHandler.kt`
- `feature/dynamic/.../domain/usecase/LoadDynamicScreenUseCase.kt`
- `feature/dynamic/.../domain/usecase/ExecuteDynamicActionUseCase.kt`
- `feature/dynamic/.../registry/DynamicRegistry.kt`
- `feature/dynamic/.../renderer/DynamicRenderer.kt`
- `feature/dynamic/.../sdui/template/StackTemplate.kt`
- `feature/dynamic/.../sdui/component/StackComponent.kt`
- `feature/dynamic/.../sdui/section/StackSection.kt`
- `feature/dynamic/.../sdui/group/StackGroup.kt`
- `feature/dynamic/.../sdui/element/TextElement.kt`
- `feature/dynamic/.../sdui/element/ImageElement.kt`

### Data

- `data/.../bootstrap/DefaultApplicationBootstrap.kt`
- `data/.../bootstrap/BootstrapMapper.kt`
- `data/.../dynamic/DynamicRepositoryImpl.kt`
- `data/.../dynamic/remote/DynamicRemoteDataSource.kt`
- `data/.../dynamic/model/DynamicResponseDto.kt`
- `data/.../dynamic/mapper/DynamicResponseMapper.kt`

### Network

- `network/.../http/RequestHandler.kt`
- `network/.../http/HttpClientBuilder.kt`
- `network/.../http/DefaultError.kt`
- `network/.../NetworkException.kt`
- `network/.../NetworkResult.kt`
- `network/.../Response.kt`
- `network/.../Paged.kt`
- `network/.../di/NetworkModule.kt`

### Domain

- `domain/.../model/dynamic/DynamicModels.kt`
- `domain/.../repository/DynamicRepository.kt`
- `domain/.../bootstrap/ApplicationBootstrap.kt`
- `domain/.../model/bootstrap/*`

## 5. Login → OTP Checkpoint

Known destination identities:

```text
partner_login
  templateId = tpl_7K2M9Q
  templateType = form_template
  GET /api/v1/partner/screen/auth_login

partner_otp
  templateId = tpl_P6X8N3
  templateType = form_template
  GET /api/v1/partner/screen/auth_otp
```

Known actions:

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

This is a generic Dynamic contract. No OTP-specific Store, renderer, or navigation branch is authorized.

## 6. Remaining Implementation Work

1. Complete request metadata/authentication integration through the frozen network/runtime boundary.
2. Complete Dynamic reference/state/action behavior required by the actual backend contract.
3. Implement the frozen DYNAMIC-07 fallback/recovery/observability boundary.
4. Add backend-shaped Login → Send OTP → OTP → Verify OTP integration tests.
5. Verify full backend-driven destination push/pop/replace/back behavior.
6. Run Android verification.
7. Run iOS/macOS-hosted verification.
8. Run Web/Wasm verification.
9. Perform literal plan-to-source compliance review.
10. Complete final Dynamic feature review.
11. Code-freeze Dynamic.

## 7. Explicitly Not Complete

- Real backend Login → OTP → Dashboard end-to-end verification.
- Full DYNAMIC-07 production observability implementation.
- Final fallback/error UX implementation.
- Full platform verification.
- Final feature review/freeze.

## 8. Deviations / Reconciliation

No Dynamic architecture decision has been intentionally changed.

Documentation reconciliation identifies one historical topology mismatch: older Base Architecture text describes Desktop while the current branch registers Web/Wasm. This is recorded in `docs/architecture/ARCHITECTURE-CURRENT-STATE.md` and must not be silently resolved by changing code.

Older SDUI review drafts may use names such as `NavigationStore`, `DynamicScreenStore`, `SduiDecoder`, `SduiSupportChecker`, and `SduiActionExecutor`. Those names are historical design vocabulary; current implementation names are authoritative unless a new decision explicitly changes them.

## 9. Next Action

**Continue implementation/testing from the green JVM baseline. Do not reopen DYNAMIC-01 → DYNAMIC-07.**
