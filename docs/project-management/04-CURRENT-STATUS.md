# CB-Partner — Current Status

**Status:** ACTIVE
**Last Updated:** 2026-10-04

## Current Phase

Phase 1 — Technical Foundation → Dynamic UI vertical slice implementation.

## Current Objective

Complete the backend-driven Dynamic UI runtime and verify the first real product flow through Login → Send OTP → OTP → Verify OTP without introducing business-specific frontend screen architecture.

## Frozen State

- BASE-ARCH-001 → BASE-ARCH-017: **DECISION_FROZEN**
- BASE-ARCH-014: **CODE_FROZEN**
- BASE-ARCH-017: **VERIFIED / REVIEW ACCEPTED / CODE_FROZEN**
- DYNAMIC-01 → DYNAMIC-07: **DISCUSSION FROZEN**
- Dynamic implementation plan: **PLAN_FROZEN / IMPLEMENTATION AUTHORIZED**
- Pure Store + MVI/UDF: frozen direction
- No ViewModel: frozen
- Backend-driven Dynamic navigation/rendering: frozen
- Bootstrap Option B refinement: **SELECTED FOR IMPLEMENTATION**

## Current Work Item

**DYNAMIC-UI-001 — Dynamic UI vertical slice**

Branch:

```text
feature/dynamic-ui
```

## Latest Verification — 2026-10-04

Owner-provided commands:

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

The earlier Bootstrap HTTP-status test failure is resolved. The green JVM result is a baseline only; it does not mean all architectural/runtime gaps are complete.

## Current Implementation State

### Bootstrap / Splash

**IMPLEMENTED BASELINE / ARCHITECTURAL REFINEMENT REQUIRED**

Splash and Bootstrap are one startup flow:

```text
ApplicationContent
 → SplashStore / SplashScreen
 → ApplicationBootstrap
 → BootstrapRepository
 → BootstrapRepositoryImpl
 → BootstrapRemoteDataSource
 → RequestHandler
 → Bootstrap DTO
 → BootstrapMapper
 → BootstrapOutput
 → DynamicDestination
```

The current source still contains the transitional direct `DefaultApplicationBootstrap → RequestHandler` path. Option B is selected and must now be implemented so Bootstrap follows the same repository + remote-source layering as Dynamic.

Actual Bootstrap contract:

```text
GET /api/v1/partner/config/bootstrap

X-CarBroz-Platform: ANDROID
X-CarBroz-App-Version: 1.0.0
X-CarBroz-Build-Number: 1
```

The response's `startup.nextScreen` becomes the canonical `DynamicDestination`.

### Dynamic

**IMPLEMENTATION IN PROGRESS / JVM GREEN BASELINE**

Implemented boundaries include:

- `DynamicDestination`
- `DynamicNavigationState`
- `DynamicContainer`
- `DynamicStore`
- `LoadDynamicScreenUseCase`
- `ExecuteDynamicActionUseCase`
- `DynamicRegistry`
- `DynamicRenderer`
- `stack_template`
- `stack_component`
- `stack_section`
- `stack_group`
- `text`
- `image`
- Dynamic repository/data source/DTO/mapper
- shared `:network` `RequestHandler` + Ktor infrastructure

Feature use cases are currently under `feature/dynamic/.../domain/usecase/`. They are feature orchestration use cases, not top-level domain use cases. Their package should be cleaned to `feature/dynamic/.../usecase/` without moving feature-specific capabilities into `:domain`.

See the Master State and Dynamic implementation plan for the complete source/class map.

## Login → OTP Checkpoint

Frozen backend-driven destination identities:

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

Frozen action contract already recorded:

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

The Dynamic runtime remains generic; no OTP-specific Store or screen class is authorized.

## Pending Work — No Gap Suppression

1. Implement Option B BootstrapRepository + BootstrapRepositoryImpl + BootstrapRemoteDataSource.
2. Update Bootstrap DI/tests and remove/rework the direct `DefaultApplicationBootstrap` HTTP path.
3. Verify complete Splash → Bootstrap → DynamicDestination runtime.
4. Clean Dynamic use-case package ownership.
5. Complete Dynamic request metadata/authentication integration.
6. Complete required Dynamic state/reference/action behavior.
7. Complete DYNAMIC-07 fallback/recovery/observability implementation.
8. Add/finish Login → Send OTP → OTP → Verify OTP integration tests using backend-shaped fixtures.
9. Verify full backend-driven push/pop/back/replace behavior.
10. Run Android, iOS/macOS-hosted and Web/Wasm verification where supported.
11. Perform literal plan-to-source compliance review.
12. Final Dynamic review and feature freeze.

## Explicitly Not Done

- Bootstrap Option B implementation.
- Full real-backend Login → OTP → Dashboard end-to-end verification.
- Production Dynamic observability implementation.
- Final fallback/error UX implementation.
- Full platform verification.
- Final Dynamic feature freeze.

## Recovery Documents

Read first:

1. `AI_START_HERE.md`
2. `docs/project-management/14-PROJECT-MASTER-STATE.md`
3. `docs/project-management/12-PROJECT-TRACKER.md`
4. `docs/architecture/ARCHITECTURE-CURRENT-STATE.md`
5. `docs/features/dynamic/00-FEATURE-DISCUSSION.md`
6. `docs/features/dynamic/01-IMPLEMENTATION-PLAN.md`
7. `docs/features/dynamic/02-IMPLEMENTATION-STATUS.md`

## Next Valid Action

**Implement Option B Bootstrap boundary first. Then continue the remaining Dynamic gaps in the ordered implementation plan. Do not reopen frozen DYNAMIC-01 → DYNAMIC-07 decisions.**
