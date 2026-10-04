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

The earlier `BootstrapRemoteDataSourceTest.nonSuccessfulHttpStatusIsMapped` failure is resolved. The MockEngine test client now uses `expectSuccess = true` so HTTP failures enter the expected mapping path.

Expected Windows warnings remain for iOS simulator tests and Android host-test configuration. They are non-blocking.

## Current Implementation State

### Bootstrap / Splash

**IMPLEMENTED / JVM VERIFIED**

```text
ApplicationContent
 → SplashStore
 → ApplicationBootstrap
 → RequestHandler
 → Bootstrap DTO
 → BootstrapMapper
 → BootstrapOutput
 → DynamicDestination
```

Canonical Bootstrap classes are recorded in:

`docs/project-management/14-PROJECT-MASTER-STATE.md`

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

See the Master State for the complete source/class map.

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

## Pending Work

1. Reconcile current implementation topology with older base-document wording (`:network/:app/:webApp` versus historical Desktop wording) without silently reopening frozen architecture.
2. Complete Dynamic request metadata/authentication integration through the frozen runtime/network boundary.
3. Complete required Dynamic state/reference/action behavior against the actual backend contract.
4. Complete DYNAMIC-07 fallback/recovery/observability implementation.
5. Add/finish Login → Send OTP → OTP → Verify OTP integration tests using backend-shaped fixtures.
6. Verify full backend-driven push/pop/back/replace behavior.
7. Run Android, iOS/macOS-hosted and Web/Wasm verification where supported.
8. Perform literal plan-to-source compliance review.
9. Final Dynamic review and feature freeze.

## Explicitly Not Done

- Full real-backend Login → OTP → Dashboard end-to-end verification.
- Production Dynamic observability implementation.
- Final Dynamic feature freeze.
- Any business-specific Dashboard/Booking/Payment architecture.

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

Continue Dynamic implementation/testing from the current green JVM baseline. Do not reopen frozen DYNAMIC-01 → DYNAMIC-07 decisions.
