# CB-Partner Dynamic UI — Implementation Status

## Status

**IMPLEMENTATION IN PROGRESS — JVM BASELINE GREEN — BOOTSTRAP BOUNDARY REFINEMENT REQUIRED**

DYNAMIC-01 → DYNAMIC-07 discussion is frozen and the implementation plan is frozen/authorized. This document records the actual implementation state, including gaps that must not be hidden behind a green compile/test result.

## 1. Frozen Discussion State

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

5. `DynamicStore` exists as the feature MVI/UDF orchestration boundary.
6. `DynamicStore` does not own Ktor or Navigation 3.
7. `LoadDynamicScreenUseCase` exists and is used for screen loading.
8. `ExecuteDynamicActionUseCase` exists and handles feature action/reference coordination.
9. Dynamic navigation and external URI are exposed as separate effects.
10. The Store no longer builds HTTP transport details itself.

### Registry / Renderer

11. `DynamicRegistry` is restricted to the frozen initial vocabulary.
12. Duplicate registration is rejected.
13. `DynamicRenderer` is a small recursive rendering boundary.
14. `stack_template`, `stack_component`, `stack_section`, `stack_group`, `text`, and `image` implementations exist.
15. Obsolete `FormTemplate`, `InputElement`, and `ButtonElement` Dynamic implementations were removed from the current runtime.

### Dynamic Data / Network

16. Standalone `:network` module exists.
17. Shared `HttpClient`/`RequestHandler` infrastructure is in `:network`.
18. Dynamic data uses `RequestHandler` through `DynamicRemoteDataSource`.
19. Dynamic repository uses explicit DTO/domain mapping.
20. Transport headers remain below the Dynamic/domain contract.
21. Bootstrap currently uses the shared `RequestHandler`.
22. Bootstrap mapper constructs the canonical `DynamicDestination`.
23. Duplicate Bootstrap `NextScreenConfig` destination modeling was removed.

### Bootstrap Integration — CURRENTLY PARTIAL

24. Bootstrap success data reaches `BootstrapOutput.startup.nextScreen` as `DynamicDestination`.
25. Bootstrap destination identity fields are preserved and tested.
26. Application composition can hand the Bootstrap destination into Dynamic navigation.
27. The actual Bootstrap endpoint and required headers are now known and documented.
28. The JVM Bootstrap tests pass at the current baseline.

## 3. Bootstrap Gap — MUST BE IMPLEMENTED

The current implementation still has a direct:

```text
ApplicationBootstrap → DefaultApplicationBootstrap → RequestHandler
```

path.

This is no longer the target implementation. The selected implementation refinement is Option B:

```text
ApplicationBootstrap
    ↓
BootstrapRepository
    ↓
BootstrapRepositoryImpl
    ↓
BootstrapRemoteDataSource
    ↓
RequestHandler
```

Required responsibilities:

- `ApplicationBootstrap` remains the startup-facing domain contract.
- `BootstrapRepository` is a domain repository abstraction.
- `BootstrapRepositoryImpl` is the data orchestration implementation.
- `BootstrapRemoteDataSource` owns Bootstrap HTTP transport details.
- `BootstrapMapper` performs explicit DTO → domain mapping.

`DefaultApplicationBootstrap` must be removed/reworked so it does not remain a second direct HTTP path.

## 4. Dynamic Package Boundary Gap

Current use-case files are under:

```text
feature/dynamic/.../domain/usecase/
```

The use cases are not candidates for moving into top-level `:domain`:

- `LoadDynamicScreenUseCase` depends on the Dynamic repository contract.
- `ExecuteDynamicActionUseCase` coordinates the feature-owned `DynamicActionHandler` and therefore belongs to the feature orchestration layer.

Target package cleanup:

```text
feature/dynamic/.../usecase/
```

This is a package ownership clarification, not a new architecture layer.

## 5. Latest Verification — 2026-10-04

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

The earlier `BootstrapRemoteDataSourceTest.nonSuccessfulHttpStatusIsMapped` failure is resolved. This green result is a verification baseline only; it does not mark the Bootstrap architecture refinement or Dynamic feature complete.

## 6. Current Code Map

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
- `feature/dynamic/.../domain/usecase/LoadDynamicScreenUseCase.kt` — target package cleanup pending
- `feature/dynamic/.../domain/usecase/ExecuteDynamicActionUseCase.kt` — target package cleanup pending
- `feature/dynamic/.../registry/DynamicRegistry.kt`
- `feature/dynamic/.../renderer/DynamicRenderer.kt`
- `feature/dynamic/.../sdui/template/StackTemplate.kt`
- `feature/dynamic/.../sdui/component/StackComponent.kt`
- `feature/dynamic/.../sdui/section/StackSection.kt`
- `feature/dynamic/.../sdui/group/StackGroup.kt`
- `feature/dynamic/.../sdui/element/TextElement.kt`
- `feature/dynamic/.../sdui/element/ImageElement.kt`

### Data — current and target Bootstrap structure

Current:

- `data/.../bootstrap/DefaultApplicationBootstrap.kt`
- `data/.../bootstrap/BootstrapMapper.kt`

Target:

- `data/.../bootstrap/BootstrapRepositoryImpl.kt`
- `data/.../bootstrap/remote/BootstrapRemoteDataSource.kt`
- `data/.../bootstrap/model/*`
- `data/.../bootstrap/mapper/BootstrapMapper.kt`

Dynamic:

- `data/.../dynamic/DynamicRepositoryImpl.kt`
- `data/.../dynamic/remote/DynamicRemoteDataSource.kt`
- `data/.../dynamic/model/DynamicResponseDto.kt`
- `data/.../dynamic/mapper/DynamicResponseMapper.kt`

### Domain

- `domain/.../bootstrap/ApplicationBootstrap.kt`
- `domain/.../model/bootstrap/*`
- `domain/.../repository/BootstrapRepository.kt` — to be added
- `domain/.../model/dynamic/DynamicModels.kt`
- `domain/.../repository/DynamicRepository.kt`

## 7. Login → OTP Checkpoint

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

This remains generic Dynamic behavior. No OTP-specific Store, renderer, or navigation branch is authorized.

## 8. Remaining Implementation Work — No Gap Suppression

1. Implement Option B Bootstrap repository/remote-source layering.
2. Update Bootstrap DI and tests so only one production Bootstrap request path remains.
3. Verify the full Splash → Bootstrap → DynamicDestination runtime path.
4. Move Dynamic use cases to the feature-level `usecase` package without moving them to top-level `:domain`.
5. Complete request metadata/authentication integration.
6. Complete all required Dynamic reference/state/action behavior from the frozen DYNAMIC-06 contract.
7. Complete DYNAMIC-07 fallback/recovery/observability behavior.
8. Add backend-shaped Login → Send OTP → OTP → Verify OTP integration tests.
9. Verify destination push/pop/replace/back behavior.
10. Verify Android.
11. Verify iOS/macOS-hosted targets where supported.
12. Verify Web/Wasm.
13. Perform literal plan-to-source compliance review.
14. Complete final Dynamic feature review.
15. Code-freeze Dynamic.

## 9. Explicitly Not Complete

- Option B Bootstrap repository/remote-source layering.
- Final Bootstrap DI/test cleanup after the layering change.
- Real backend Login → OTP → Dashboard end-to-end verification.
- Full DYNAMIC-07 production observability implementation.
- Final fallback/error UX implementation.
- Full platform verification.
- Final Dynamic feature review/freeze.

## 10. Deviations / Reconciliation

No frozen DYNAMIC-01 → DYNAMIC-07 decision is being reopened.

The Bootstrap implementation is being refined under the already-frozen startup responsibility using Option B. This is now an explicit implementation requirement rather than an optional cleanup.

Historical base-document platform wording remains a separate reconciliation item and is not mixed into this Dynamic implementation change.

## 11. Next Action

**Implement Option B Bootstrap boundary first. Then continue the remaining Dynamic gaps in the ordered implementation plan. Do not mark the feature complete merely because the JVM baseline is green.**
